package it.italiaonline.rnd.net

class RetryableConnection implements NetConnection {

	private final NetConnection origin
	private final Integer baseTimeout
	private final Integer retries

	RetryableConnection(
		Integer baseTimeout,
		Integer retries,
		NetConnection org
	) {
		this.origin      = org
		this.baseTimeout = baseTimeout
		this.retries     = retries
	}

	@Override
	String text() {
		for ( Integer time = 1; time <= retries; time++ ) {
			try {
				// Get Yext REST result
				return this.origin.text()
			}
			catch (IOException ioe) {
				if (time < retries) {
					Integer sleepTime = timeout * time
					println "Retrying connection after ${sleepTime}ms (time = ${time}, retries = ${retries}, sleepTime = ${sleepTime})"
					// sleep linearly more at every retry
					sleep sleepTime
				} else {
					println "Connection killed after ${retries} times (time = ${time})"
					throw new IOException("Connection retry limit exceeded", ioe)
				}
			}
		}
	}

	@Override
	String toString() {
		this.origin.toString()
	}
}
