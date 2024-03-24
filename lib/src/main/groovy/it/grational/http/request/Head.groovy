package it.grational.http.request

class Head extends StandardRequest {

	/**
	 * Secondary Constructor
	 * @param URL the URL to connect to
	 */
	Head(URL url) {
		this(url,[:],null)
	}

	/**
	 * Primary Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param params the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * https://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 * @param proxy: an instance of the java.net.Proxy class or its local subtypes HttpProxy, HttpAuthProxy
	 */
	Head (
		URL url,
		Map params,
		Proxy proxy
	) {
		this.method = 'HEAD'
		this.url = url
		this.parameters = params ?: [:]
		this.proxy = proxy
	}

	/**
	 * Secondary Map Constructor
	 * <p>
	 * @param params containing:
	 * - url: the URL to connect to
	 * - headers: custom headers to be sent with the request in Map format
	 * - connectTimeout: milliseconds to wait before closing the outgoing connection
	 * - readTimeout: milliseconds to wait before reading the response
	 * - parameters: the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * https://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 * - proxy: an instance of the java.net.Proxy class or its local subtypes HttpProxy, HttpAuthProxy
	 */
	Head(Map params) {
		this (
			params.url,
			(params.parameters ?: [:]) << [
				connectTimeout: params.connectTimeout,
				readTimeout: params.readTimeout,
				headers: params.headers,
				cookies: params.cookies
			],
			params.proxy
		)
	}

}
