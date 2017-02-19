package it.italiaonline.rnd.net

import groovy.util.logging.Slf4j

class RetryableConnection implements NetConnection {

	private final NetConnection origin
	private final Integer baseTimeout
	private final Integer retries

	RetryableConnection(
		NetConnection org,
		Integer baseTimeout,
		Integer retries
	) {
		this.origin      = org
		this.baseTimeout = baseTimeout
		this.retries     = retries
	}

	@Override
	String text() {
		for ( Integer time = 1; time <= retries; time++ ) {
			try {
				return this.origin.text()
			}
			catch (IOException ioe) {
				if (time < retries) {
					log.debug "Retrying connection after ${sleepTime}ms (time = ${time}, retries = ${retries}, sleepTime = ${sleepTime})"
					Integer sleepTime = baseTimeout * time
					sleep sleepTime
				} else {
					log.debug "Connection killed after ${retries} times (time = ${time})"
					throw new RuntimeException("Connection retry limit exceeded", ioe)
				}
			}
		}
	}

	@Override
	String toString() {
		this.origin.toString()
	}
}
