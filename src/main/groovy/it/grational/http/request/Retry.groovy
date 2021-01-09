package it.grational.http.request

class Retry implements HttpRequest {

	private final HttpRequest origin
	private final Integer       retries
	private final Closure       retryOperation

	Retry (
		HttpRequest org,
		Integer     retries = 5,
		Closure     rop = { curr, tot -> sleep(1000 * curr) }
	) {
		this.origin         = org
		this.retries        = retries
		this.retryOperation = rop
	}

	@Override
	String text() {
		for ( Integer time = 1; time <= retries; time++ ) {
			try {
				return this.origin.text()
			}
			catch (IOException ioe) {
				if (time < retries)
					this.retryOperation.call(time, retries)
				else
					throw new RuntimeException("Retry limit exceeded for connection '${this.origin.toString()}'", ioe)
			}
		}
	}

	@Override
	String toString() {
		this.origin.toString()
	}
}
