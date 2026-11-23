package it.grational.http.request

import it.grational.http.response.HttpResponse
import it.grational.http.response.RetryResponse
import java.util.function.BiConsumer

class Retry extends FunctionalRequest {

	static final int DEFAULT_ATTEMPTS = 3

	private final int limit
	private final BiConsumer<Integer, Integer> waitOperation
	private final boolean body

	Retry (
		HttpRequest origin,
		Integer retries = DEFAULT_ATTEMPTS,
		BiConsumer<Integer, Integer> delay = { attempt, total -> Thread.sleep(1000L * attempt) },
		boolean body = true
	) {
		this.origin = Objects.requireNonNull(origin)
		if (retries == null || retries < 1)
			throw new IllegalArgumentException('At least one attempt is required')
		this.limit = retries
		this.waitOperation = Objects.requireNonNull(delay)
		this.body = body
	}

	@Override
	HttpResponse connect() {
		def attempts = new Attempts(origin, limit, waitOperation)
		def first = attempts.open()
		return body ? new RetryResponse(first, attempts.&reopen) : first
	}

	private static final class Attempts {
		private final HttpRequest request
		private final int limit
		private final BiConsumer<Integer, Integer> delay
		private int used

		Attempts(HttpRequest request, int limit, BiConsumer<Integer, Integer> delay) {
			this.request = request
			this.limit = limit
			this.delay = delay
		}

		HttpResponse open() {
			while (true) {
				used++
				Throwable failure
				try {
					HttpResponse response = request.connect()
					if (!response.error() || response.code() in 400..<500)
						return response
					failure = response.exception()
				} catch (IOException ex) {
					failure = ex
				}
				again(failure)
			}
		}

		HttpResponse reopen(IOException failure) {
			again(failure)
			return open()
		}

		private void again(Throwable failure) {
			if (used >= limit)
				throw new RuntimeException("Retry limit (${limit}) exceeded (${failure.class.simpleName})", failure)
			if (Thread.currentThread().isInterrupted())
				throw new InterruptedIOException('Retry interrupted')
			delay.accept(used, limit)
		}
	}
}
