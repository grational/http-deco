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

# branch, version suffix and whether to rebase onto master
targets=(
	'groovy-5:g5:true'
	'groovy-4:g4:true'
	'java-8-groovy-3:j8g3:false'
)

check_target() {
	local branch="$1" tag="${prefix}${version}$2"
	git show-ref --verify --quiet "refs/heads/$branch" || fail "Missing local branch: $branch"
	if git show-ref --verify --quiet "refs/tags/$tag"; then
		fail "Tag already exists: $tag"
	fi
}

git fetch --prune --tags origin
check_target "$main_branch" ''
for target in "${targets[@]}"; do
	IFS=: read -r branch suffix _ <<< "$target"
	check_target "$branch" "-$suffix"
	if git show-ref --verify --quiet "refs/remotes/origin/$branch"; then
		git merge-base --is-ancestor "origin/$branch" "$branch" || fail "Local branch is behind or diverged: $branch"
	fi
done
[[ $(git rev-parse "$main_branch") == $(git rev-parse "origin/$main_branch") ]] || fail "Publish $main_branch before releasing"

password="$(platform_password)"
[[ -n $password ]] || fail 'Bitwarden returned an empty platform token'

git switch "$main_branch"
release "$version"

for target in "${targets[@]}"; do
	IFS=: read -r branch suffix rebase <<< "$target"
	git switch "$branch"
	if [[ $rebase == true ]]; then
		old_tip=$(git rev-parse --verify "refs/remotes/origin/$branch" 2>/dev/null || true)
		git rebase "$main_branch"
		git push --force-with-lease="refs/heads/$branch:$old_tip" -u origin "$branch"
	else
		git push origin "$branch"
	fi
	release "$version-$suffix"
done
