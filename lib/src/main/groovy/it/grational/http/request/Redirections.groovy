package it.grational.http.request

import it.grational.http.response.HttpResponse
import static java.net.HttpURLConnection.*
import it.grational.http.shared.Constants

class Redirections extends FunctionalRequest {

	private final Integer max

	Redirections (
		HttpRequest org,
		Integer mx = 3
	) {
		this.origin = org
		this.max = mx
	}

	@Override
	public HttpResponse connect() {
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
		location = URLDecoder.decode (
			location,
			charsetFromContentType (
				response.header('Content-Type')
			)
		)
		URL newDestination = new URL(this.origin.url, location)
		this.origin = this.origin.withURL(newDestination)
	}

	private String charsetFromContentType(String header) {
		header?.find(/(?<=charset=).*/) ?: Constants.defaultCharset
	}

}
