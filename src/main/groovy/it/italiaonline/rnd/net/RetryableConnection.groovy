package it.italiaonline.rnd.net

import groovy.util.logging.Slf4j

class RetryableConnection implements NetConnection {

	private final NetConnection origin
	private final Integer       retries
	private final Closure       retryOperation

	RetryableConnection(
		NetConnection org,
		Integer       retries = 5,
		Closure       rop = { curr, tot -> sleep(1000 * curr) }
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
