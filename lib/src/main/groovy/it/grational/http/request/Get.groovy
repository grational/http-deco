package it.grational.http.request

class Get extends StandardRequest {

	/**
	 * Secondary Constructor
	 * @param URL the URL to connect to
	 */
	Get(URL url) {
		this(url: url)
	}

	/**
	 * Primary Map Constructor
	 * <p>
	 * @param params containing:
	 * - url: the URL to connect to
	 * - proxy: an instance of the java.net.Proxy class or its local subtypes HttpProxy, HttpAuthProxy
	 * - headers: custom headers to be sent with the request in Map format
	 * - connectionParameters: the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * http://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 */
	Get(Map params) {
		this.verb = 'GET'

		this.url = params.url ?: { throw new IllegalArgumentException("[${this.class.simpleName}] Invalid url parameter") }()

		// java.net.Proxy or my own proxy classes which extends it
		this.proxy = params.proxy ?: Proxy.NO_PROXY

		this.connectionParameters = params.connectionParameters ?: [:]
		this.connectionParameters << [
			connectTimeout: params.connectTimeout,
			readTimeout: params.readTimeout
		]
		this.connectionParameters << [
			requestProperties: params.headers ?: [:]
		]
	}

}
