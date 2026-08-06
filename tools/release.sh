#!/usr/bin/env bash
set -euo pipefail

readonly prefix=v
readonly main_branch=master
readonly username=grational

usage() {
	echo "Usage: ${0##*/} VERSION" >&2
	exit 1
}

fail() {
	echo "$1" >&2
	exit 1
}

platform_password() {
	local remote_platform
	local remote_url

	remote_url="$(git remote get-url origin)"
	remote_platform="${remote_url#*://}"
	remote_platform="${remote_platform#*@}"
	remote_platform="${remote_platform%%[/:]*}"

	bw list items --search "$remote_platform" \
		| jq -er 'first(.[] | .fields[]? | select(.name == "token") | .value)'
}

release() {
	local version="$1"

	HTTP_DECO_RELEASE_USERNAME="$username" \
		HTTP_DECO_RELEASE_PASSWORD="$password" \
		./gradlew --no-daemon :lib:release "-Prelease.version=$version"
}

restore_branch() {
	local exit_status=$?

	trap - EXIT
	if [[ -d $(git rev-parse --git-path rebase-merge) || -d $(git rev-parse --git-path rebase-apply) ]]; then
		git rebase --abort || true
	fi
	git switch "$original_branch" >/dev/null || true
	unset password
	exit "$exit_status"
}

(( $# == 1 )) || usage

version="${1#"$prefix"}"
[[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || usage

[[ -z $(git status --porcelain) ]] || fail 'The worktree must be clean before releasing'

original_branch="$(git branch --show-current)"
readonly original_branch
[[ -n $original_branch ]] || fail 'Cannot release from a detached HEAD'
trap restore_branch EXIT

for command in bw git jq; do
	command -v "$command" >/dev/null || fail "Required command not found: $command"
done

declare -a branches=(
	"$main_branch"
	'groovy-4'
	'java-8-groovy-3'
)

for branch in "${branches[@]}"; do
	git show-ref --verify --quiet "refs/heads/$branch" \
		|| fail "Required local branch not found: $branch"
done

declare -a postfixes=(
	''
	'-g4'
	'-j8g3'
)

for postfix in "${postfixes[@]}"; do
	tag="${prefix}${version}${postfix}"
	git show-ref --verify --quiet "refs/tags/$tag" \
		&& fail "Local tag already exists: $tag"
	[[ -z $(git ls-remote --tags origin "refs/tags/$tag") ]] \
		|| fail "Remote tag already exists: $tag"
done

password="$(platform_password)"
[[ -n $password ]] || fail 'Bitwarden returned an empty platform token'

git switch "$main_branch"
release "$version"

branch=groovy-4
git switch "$branch"
git rebase "$main_branch"
remote_tip="$(git ls-remote --heads origin "refs/heads/$branch" | cut -f1)"
if [[ -n $remote_tip ]]; then
	git push --force-with-lease="$branch:$remote_tip" origin "$branch:$branch"
else
	git push --set-upstream origin "$branch:$branch"
fi
release "$version-g4"

branch=java-8-groovy-3
git switch "$branch"
git push origin "$branch:$branch"
release "$version-j8g3"
