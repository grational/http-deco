package it.grational.http.request
import it.grational.http.response.HttpResponse

class Retry implements HttpRequest {

	private final HttpRequest origin
	private final Integer     retries
	private final Closure     retryOperation

	Retry (
		HttpRequest org,
		Integer retries = 3,
		Closure rop = { curr, tot -> Thread.sleep(1000 * curr) }
	) {
		this.origin = org
		this.retries = retries
		this.retryOperation = rop
	}

	@Override
	HttpResponse connect() {
		for ( Integer time = 1; time <= retries; time++ ) {
			try {
				HttpResponse response = this.origin.connect()
				if ( response.error() )
					handleErrors (
						time,
						response.exception()
					)
				else
					return response
			} catch (IOException e) {
				handleErrors(time, e)
			}
		}
	}

	private void handleErrors (
		Integer time,
		Exception e
	) {
		if (time < retries)
			this.retryOperation.call(time, retries)
		else
			throw new RuntimeException (
				"Retry limit (${retries}) exceeded for connection '${this.origin.toString()}'",
				e
			)
	}

	@Override
	String toString() {
		this.origin.toString()
	}
}
