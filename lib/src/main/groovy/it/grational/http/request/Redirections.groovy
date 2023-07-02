package it.grational.http.request

import it.grational.http.response.HttpResponse
import static java.net.HttpURLConnection.*

class Redirections implements HttpRequest {

	private HttpRequest origin
	private final Integer max

	Redirections (
		HttpRequest org,
		Integer mx = 3
	) {
		this.origin = org
		this.max = mx
	}

	@Override
	HttpResponse connect() {
		disableIntraProtocolRedirects()
		HttpResponse response
		for ( Integer time = 0; time <= max; time++ ) {
			response = this.origin.connect()

			if ( !redirect(response) )
				break

			setNewDestination(response)
		}
		return response
	}

	private void disableIntraProtocolRedirects() {
		this.origin = this.origin.withParameter (
			'followRedirects',
			false
		)
	}

	private Boolean redirect(HttpResponse response) {
		response.code in [
			HTTP_MOVED_PERM,
			HTTP_MOVED_TEMP
		]
	}

	private void setNewDestination(HttpResponse response) {
		String location = response.header('Location')
		location = URLDecoder.decode(location, 'UTF-8')
		URL newDestination = new URL(this.origin.url, location)
		this.origin = this.origin.withURL(newDestination)
	}

	@Override
	HttpRequest withHeader(String key, String value) {
		this.origin.withHeader(key, value)
	}

	@Override
	HttpRequest withCookie(String key, String value) {
		this.origin.withCookie(key, value)
	}

	@Override
	HttpRequest withParameter(String key, def value) {
		this.origin.withParameter(key, value)
	}

	@Override
	HttpRequest withURL(URL url) {
		this.origin.withURL(url)
	}

	@Override
	String toString() {
		this.origin.toString()
	}

}
