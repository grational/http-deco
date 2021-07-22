package it.grational.http.request

class StandardPost implements HttpRequest {

	private final URL    url
	private final String body
	private final Map    connectionParameters

	/**
	 * Secondary Constructor
	 * @param URL the URL to connect to
	 */
	StandardPost(URL url) {
		this(url,'',[:],[:])
	}

	/**
	 * Secondary Constructor
	 * @param URL the URL to connect to
	 * @param body the body to be passed to the url
	 */
	StandardPost (
		URL url,
		String body
	) {
		this(url,body,[:],[:])
	}

	/**
	 * Secondary Constructor
	 * @param URL the URL to connect to
	 * @param body the body to be passed to the url
	 * @param headers the request properties (custom headers added to the request)
	 */
	StandardPost (
		URL url,
		String body,
		Map headers
	) {
		this(url,body,[:],headers)
	}

	/**
	 * Primary Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param body the body to be passed to the url
	 * @param headers the request properties (custom headers added to the request)
	 * @param cp the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * http://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 */
	StandardPost (
		URL url,
		String body,
		Map cp,
		Map headers
	) {
		this.url = url
		this.body = body
		this.connectionParameters = cp
		this.connectionParameters << [
			requestProperties: headers
		]
	}

	@Override
	String text() {
		String result
		this.url.openConnection().with {
			requestMethod = 'POST'

			if (this.connectionParameters.connectTimeout)
				setConnectTimeout ( // milliseconds
					this.connectionParameters.connectTimeout
				)
			if (this.connectionParameters.readTimeout)
				setReadTimeout ( // milliseconds
					this.connectionParameters.readTimeout
				)
			if (this.connectionParameters.allowUserInteraction)
				setAllowUserInteraction ( // boolean
					this.connectionParameters.allowUserInteraction
				)

			this.connectionParameters.requestProperties.each { k, v ->
				setRequestProperty(k,v)
			}

			if (this.body) {
				doOutput = true
				outputStream.withWriter { writer ->
					writer.write (this.body)
				}
			}

			result = inputStream.text
		}
		return result
	}

	@Override
	String toString() {
		"""POST 
		|${url.toString()}
		|${this.body}
		|${this.connectionParameters}""".stripMargin()
	}
}
