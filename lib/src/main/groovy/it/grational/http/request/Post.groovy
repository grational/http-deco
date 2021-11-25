package it.grational.http.request

class Post extends StandardRequest {

	/**
	 * Primary Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param body the body to be passed to the url
	 * @param cp the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * http://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 */
	Post (
		URL url,
		String body,
		Map params,
		Proxy proxy
	) {
		this.method = 'POST'
		this.url = url
		this.body = body
		this.parameters = params ?: [:]
		this.proxy = proxy
	}

	/**
	 * Secondary Map Constructor
	 * <p>
	 * @param params containing:
	 * - url: the URL to connect to
	 * - body: the string body to be passed to the url
	 * - headers: custom headers to be sent with the request in Map format
	 * - connectTimeout: milliseconds to wait before closing the outgoing connection
	 * - readTimeout: milliseconds to wait before reading the response
	 * - parameters: the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * http://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 * - proxy: an instance of the java.net.Proxy class or its local subtypes HttpProxy, HttpAuthProxy
	 */
	Post(Map params) {
		this (
			params.url,
			params.body,
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
