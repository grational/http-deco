package it.grational.http.response

import static java.nio.charset.StandardCharsets.*

interface HttpResponse {
	Integer code()
	Boolean error()
	Throwable exception()
	String text()
	String text(String charset)
	byte[] bytes()
	String header(String name)
	HttpCookie cookie(String name)

	/**
	 * Fake class modelling an example custom response
	 */
	final class CustomResponse implements HttpResponse {
		private final Integer     code
		private final InputStream stream
		private final Boolean     error
		private final Throwable   exception

		CustomResponse (
			Integer code,
			InputStream stream,
			Boolean error = false,
			Throwable exception = null
		) {
			this.code = code
			this.stream = stream
			this.error = error
			this.exception = exception
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
		Throwable exception() {
			this.exception ?: {
				throw new IllegalStateException (
					"No exception were been thrown at this moment!"
				)
			}()
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
		String header(String name) {
			return "header ${name} value"
		}

		@Override
		HttpCookie cookie(String name) {
			new HttpCookie (
				name,
				'value'
			)
		}

		@Override
		String toString() {
			"${this.class.simpleName} (code:${code}, stream:${stream.text}, error:${error}, exception:${exception})"
		}

	}

}
