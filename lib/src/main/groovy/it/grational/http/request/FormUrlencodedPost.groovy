package it.grational.http.request

import groovy.json.JsonOutput
import java.nio.charset.Charset
import static java.nio.charset.StandardCharsets.*
import it.grational.http.shared.Constants

class FormUrlencodedPost extends Post {
	/**
	 * Primary Constructor
	 * <p>
	 * @param url the URL to connect to
	 * @param form a map version of the form parameters
	 * @param charset specify the charset to encode params
	 * @param headers the request properties (custom headers added to the request)
	 * @param parameters the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * https://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
	 */
	FormUrlencodedPost(Map params) {
		super (
			url: params.url,
			body: encodeFormParams (
				params.form,
				params.charset ?: Constants.defaultCharset
			),
			parameters: params.parameters,
			connectTimeout: params.connectTimeout,
			readTimeout: params.readTimeout,
			headers: ( params.headers ?: [:] ) << [
				'Content-Type': 'application/x-www-form-urlencoded'
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
	 * @param form a map version of the form parameters
	 * @param params the connection parameters as specified here:
	 * http://docs.groovy-lang.org/latest/html/groovy-jdk/java/net/URL.html#getText(java.util.Map)
	 * https://mrhaki.blogspot.it/2011/09/groovy-goodness-use-connection.html
		additionally a charset could be passed in params to specify the form parameters encoding
	 * @param proxy: an instance of the java.net.Proxy class or its local subtypes HttpProxy, HttpAuthProxy
	 */
	FormUrlencodedPost (
		URL url,
		Map form,
		Map params,
		Proxy proxy
	) {
		this (
			url: url,
			form: form,
			charset: params.charset,
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
	 * @param form a map version of the form parameters
	 */
	FormUrlencodedPost(URL url, Map form = [:]) {
		this(url, form, [:], null)
	}

	public HttpRequest withFormParam (
		String key,
		String value
	) {
		this.body = encodeFormParams (
			[ (key): value ],
			this.charset,
			this.body
		)
		return this
	}

	private static String encodeFormParams (
		Map form,
		Charset charset,
		String seed = ''
	) {
		form.inject(seed) { acc, k, v ->
			acc += "${acc ? '&' : ''}${URLEncoder.encode(k as String, charset.name())}=${URLEncoder.encode(v as String, charset.name())}"
		}
	}

}
