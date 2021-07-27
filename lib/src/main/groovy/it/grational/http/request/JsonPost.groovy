package it.grational.http.request

import groovy.json.JsonOutput

class JsonPost extends StandardPost {

	/**
	 * Primary Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param json the json string to be passed to the url
	 * @param map a data object to be tranformed to a json map
	 * @param headers the request properties (custom headers added to the request)
	 * @param parameters the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * http://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 */
	JsonPost(Map params) {
		super (
			params.url,
			params.json ?: JsonOutput.toJson(params.map),
			params.parameters ?: [:],
			[
				'Content-Type': 'application/json; utf-8',
			] << ( params.headers ?: [:] )
		)
	}

}
