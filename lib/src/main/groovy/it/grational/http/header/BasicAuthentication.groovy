package it.grational.http.header

abstract class BasicAuthentication implements Header {
	protected String username
	protected String password

	abstract String name()

	@Override
	String value() {
		String encodedCredentials = "${username}:${password}".bytes.encodeBase64().toString()
		return "Basic ${encodedCredentials}"
	}

	@Override
	String toString() {
		"${this.name()}: ${this.value()}"
	}
}
