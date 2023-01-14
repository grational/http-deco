package it.grational.http.response

import static java.nio.charset.StandardCharsets.*

interface HttpResponse {
	Integer code()
	String text(Stream source, String charset)
	byte[] bytes(Stream source, String charset)
	HttpCookie cookie(String name)

	/**
	 * Fake class modelling an example custom response
	 */
	final class CustomResponse implements HttpResponse {
		private final Integer     code
		private final InputStream stream

		CustomResponse (
			Integer code,
			InputStream stream
		) {
			this.code = code
			this.stream = stream
		}

		@Override
		Integer code() {
			this.code
		}

		@Override
		String text (
			Stream source = Stream.INPUT,
			String charset = UTF_8.name()
		) {
			this.stream.text
		}

		@Override
		byte[] bytes (
			Stream source = Stream.INPUT,
			String charset = UTF_8.name()
		) {
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
