package it.grational.http.request

import groovy.json.JsonOutput

class JsonPut extends Put {

	/**
	 * Primary Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param json the json string to be passed to the url
	 * @param map a data object to be tranformed to a json map
	 * @param headers the request properties (custom headers added to the request)
	 * @param parameters the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * https://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 */
	JsonPut(Map params) {
		super (
			url: params.url,
			body: (params.json ?: JsonOutput.toJson(params.map)),
			parameters: params.parameters,
			connectTimeout: params.connectTimeout,
			readTimeout: params.readTimeout,
			headers: ( params.headers ?: [:] ) << [
				'Content-Type': 'application/json',
			],
			cookies: params.cookies,
			proxy: params.proxy
		)
		this.charset = params.charset
			?: Constants.defaultCharset
	}

	/**
	 * Secondary Canonical Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param json the string body to be passed to the url
	 * @param cp the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * https://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 * @param proxy: an instance of the java.net.Proxy class or its local subtypes HttpProxy, HttpAuthProxy
	 */
	JsonPut (
		URL url,
		String json,
		Map params,
		Proxy proxy
	) {
		this (
			url: url,
			json: json,
			parameters: params,
			connectTimeout: params.connectTimeout,
			readTimeout: params.readTimeout,
			headers: params.headers,
			cookies: params.cookies,
			proxy: proxy
		)
	}

	/**
	 * Secondary Canonical Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param json a string version of the body to be passed to the url
	 */
	JsonPut(URL url, String json) {
		this(url, json, [:], null)
	}

	/**
	 * Secondary Canonical Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param map an object version of the body to be passed to the url
	 */
	JsonPut(URL url, def map) {
		this(url, JsonOutput.toJson(map), [:], null)
	}
}
