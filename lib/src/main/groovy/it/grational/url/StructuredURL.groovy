package it.grational.url

final class StructuredURL implements URLConvertible {
	private final String protocol
	private final UserInfo userInfo
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

		if (params.containsKey('userInfo') && (params.containsKey('username') || params.containsKey('password'))) {
			throw new IllegalArgumentException (
				"[${this.class.simpleName}] Use either userInfo or username/password parameters"
			)
		}
		this.userInfo = params.userInfo ?: (params.username ? new UserInfo (
			params.username as String,
			(params.password ?: '') as String
		) : null)

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
		if (userInfo) {
			result += "${userInfo}@"
		}
		result += authority
		if (path)
			result += path.startsWith('/') ? path : "/${path}"
		result += qstring

		return result
	}

}
