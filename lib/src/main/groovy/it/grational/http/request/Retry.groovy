package it.grational.http.request

import java.util.function.BiConsumer
import it.grational.http.response.HttpResponse

class Retry extends FunctionalRequest {

	private final Integer retries
	private final Boolean exception
	private final BiConsumer retryOperation

	Retry (
		HttpRequest org,
		Integer retries = 3,
		BiConsumer rop = { curr, tot -> Thread.sleep(1000 * curr) }
	) {
		this.origin = org
		this.retries = retries
		this.retryOperation = rop
	}

	@Override
	public HttpResponse connect() {
		for ( int time = 1; time <= retries; time++ ) {
			try {
				HttpResponse response = origin.connect()

				if ( ok(response) )
					return response

				if ( retry(time) )
					continue

				raiseException(response.exception())
			} catch (IOException e) {
				if ( !retry(time) )
					raiseException(e)
			}
		}
	}

	private Boolean ok(HttpResponse response) {
		return !response.error()
	}

	private Boolean retry(Integer time) {
		Boolean retry = (time < retries)
		if ( retry )
			retryOperation.accept(time, retries)
		return retry
	}

	private ByteArrayInputStream inputStream(String input) {
		new ByteArrayInputStream (
			input.join(this.separator).getBytes()
		)
	}

	private void raiseException(Exception e) {
		throw new RuntimeException (
			"Retry limit (${retries}) exceeded for connection '${origin}' with exception: '${e}'",
			e
		)
	}

}
