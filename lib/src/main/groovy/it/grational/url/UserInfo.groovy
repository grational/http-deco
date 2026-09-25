package it.grational.url

import static java.net.URLEncoder.encode
import static java.net.URLDecoder.decode

final class UserInfo {
	private final String username
	private final String password

	UserInfo(String username, String password) {
		this.username = username
		this.password = password
	}

	/**
	 * Decode form-encoded credentials once; '+' represents a space.
	 */
	static UserInfo encoded(String username, String password) {
		new UserInfo(decode(username, 'UTF-8'), decode(password, 'UTF-8'))
	}

	@Override
	String toString() {
		"${encode(username, 'UTF-8')}:${encode(password, 'UTF-8')}"
	}
}
