package it.italiaonline.rnd.net

class SimpleConnection implements NetConnection {

	private final URL url
	private final Map connectionParameters

	/**
	 * Secondary Constructor
	 * @param URL the URL to connect to
	 */
	SimpleConnection(URL url) {
		this(url,[:])
	}

	/**
	 * Primary Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param cp the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * http://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 */
	SimpleConnection(
		URL url,
		Map cp
	) {
		this.url                  = url
		this.connectionParameters = cp
	}

	@Override
	String text() {
		this.url.getText(this.connectionParameters)
	}

	@Override
	String toString() {
		this.url.toString()
	}
}
