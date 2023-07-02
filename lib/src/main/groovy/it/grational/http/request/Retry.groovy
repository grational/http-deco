package it.grational.http.request

import it.grational.http.response.HttpResponse
import java.util.function.BiConsumer;

class Retry implements HttpRequest {

	private final HttpRequest origin
	private final Integer     retries
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
	HttpResponse connect() {
		for ( Integer time = 1; time <= retries; time++ ) {
			try {
				HttpResponse response = this.origin.connect()
				if ( response.error() ) {
					if ( !retry(time) ) {
						raiseException(response.exception())
					}
				} else {
					return response
				}
			} catch (IOException e) {
				if ( !retry(time) )
					raiseException(e)
			}
		}
	}

	private Boolean retry(Integer time) {
		Boolean retry = (time < retries)
		if ( retry ) {
			this.retryOperation.accept(time, retries)
		}
		return retry
	}

	private void raiseException(Exception e) {
		throw new RuntimeException (
			"Retry limit (${retries}) exceeded for connection '${this.origin}'",
			e
		)
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
