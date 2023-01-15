package it.grational.http.response

import static java.nio.charset.StandardCharsets.*

interface HttpResponse {
	Integer code()
	Boolean error()
	String text()
	String text(String charset)
	byte[] bytes()
	HttpCookie cookie(String name)

	/**
	 * Fake class modelling an example custom response
	 */
	final class CustomResponse implements HttpResponse {
		private final Integer     code
		private final InputStream stream
		private final Boolean error

		CustomResponse (
			Integer code,
			InputStream stream,
			Boolean error = false
		) {
			this.code = code
			this.stream = stream
			this.error = error
		}

		@Override
		Integer code() {
			this.code
		}

		@Override 
		Boolean error() {
			this.error
		}

		@Override
		String text() {
			this.text(UTF_8.name())
		}

		@Override
		String text(String charset) {
			this.stream.getText(charset)
		}

		@Override
		byte[] bytes() {
			this.stream.bytes
		}

		@Override
		HttpCookie cookie(String name) {
			new HttpCookie (
				name,
				'value'
			)
		}
	}

}
