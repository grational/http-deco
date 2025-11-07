package it.grational.url

import java.util.regex.Matcher

final class BasicAuthURL implements AuthURL {
	private final String  checker =
		$/(https?)://(?:([^:]+):([^@]+)@)?([^:/]+)(?::([1-9][0-9]{0,4}))?(?:/(.*))?/$
	private final String  protocol
	private final String  username
	private final String  password
	private final String  host
	private final Integer port
	private final String  path

	BasicAuthURL(String url) {
		if ( ! ( url ==~ checker ) )
			throw new IllegalArgumentException("[${this.class.simpleName}] Invalid URL string '${url}'")
		else {
			Matcher m = (url =~ checker); m.find()
			this.protocol = m.group(1)
			this.username = m.group(2)
			this.password = m.group(3)
			this.host     = m.group(4)
			this.port     = m.group(5) as Integer
			this.path     = m.group(6)
		}
	}

	Boolean secure() {
		return ( this.protocol == 'https' )
	}

	String host() {
		return this.host
	}

	Integer port() {
		return ( this.port ) ?: ( this.secure() ) ? 443 : 80
	}

	String path() {
		return this.path
	}

	@Override
	Boolean auth() {
		return ( this.username && this.password )
	}

	@Override
	String username() {
		if ( ! this.auth() )
			throw new UnsupportedOperationException("Cannot return the URL username, the URL string doesn't contain any auth credentials")
		return this.username
	}

	@Override
	String password() {
		if ( ! this.auth() )
			throw new UnsupportedOperationException("Cannot return the URL password, the URL string doesn't contain any auth credentials")
		return this.password
	}

	@Override
	String header() {
		String encodedBasicAuthentication = "${this.username}:${this.password}".bytes.encodeBase64().toString()
		return "Basic ${encodedBasicAuthentication}"
	}

	@Override
	URL toURL() {
		this.noAuthForm().toURL()
	}

	@Override
	URI toURI() {
		this.noAuthForm().toURI()
	}

	private String noAuthForm() {
		String url
		return ( this.port ) ? "${this.protocol}://${this.host}:${this.port}/${this.path}" : "${this.protocol}://${this.host}/${this.path}"
	}

	@Override
	String toString() {
		String result = "${this.protocol}://"
		if ( this.auth() ) result += "${this.username}:${this.password}@"
		result += this.host
		if ( this.port ) result += ":${this.port}"
		result += "/${this.path}"

		return result
	}
}
// vim: ft=groovy:fdm=indent
