package it.grational.url

import static java.net.URLEncoder.encode

final class StructuredURL implements URLConvertible {
	private final String protocol
	private final String username
	private final String password
	private final String authority
	private final String path
	private final String qstring

	StructuredURL(Map params) {
		if ( params.origin ) {
			String[] splitted = params.origin.split('://')
			this.protocol = splitted.first()
			this.authority = splitted.last()
		} else {
			this.protocol = params.protocol ?: {
				throw new IllegalArgumentException (
					"[${this.class.simpleName}] Invalid protocol parameter"
				)
			}()
			this.authority = params.authority ?: {
				throw new IllegalArgumentException (
					"[${this.class.simpleName}] Invalid authority parameter"
				)
			}()
		}

		this.username = params.username ?: ''
		this.password = params.password ?: ''

		this.path = params.path ?: ''

		this.qstring = params.qparams?.inject('') { s, k, v ->
			def key = k ?: {
				throw new IllegalArgumentException (
					"[${this.class.simpleName}] Invalid qparam key '${k}'"
				)
			}()
			def value = (v == null) ? '' : v
			"${s}${s ? '&' : '?'}${key}=${value}"
		} ?: ''
	}

	@Override
	URL toURL() {
		this.toString().toURL()
	}

	@Override
	URI toURI() {
		this.toString().toURI()
	}

	@Override
	String toString() {
		String result = "${protocol}://"
		if (username) {
			String encodedUsername = encode(username, 'UTF-8')
			String encodedPassword = encode(password ?: '', 'UTF-8')
			result += "${encodedUsername}:${encodedPassword}@"
		}
		result += authority
		if (path)
			result += path.startsWith('/') ? path : "/${path}"
		result += qstring

		return result
	}

}
