package it.grational.http.response

import groovy.transform.Memoized
import java.nio.charset.Charset
import static java.nio.charset.StandardCharsets.*

interface HttpResponse {
	Integer code()
	Boolean error()
	Throwable exception()
	byte[] bytes()
	byte[] bytes(Charset charset)
	String text()
	String text(Charset charset)
	String header(String name)
	HttpCookie cookie(String name)

	abstract class StandardResponse implements HttpResponse {
		protected Integer   code
		protected Boolean   error = false
		protected Throwable exception

		@Override
		Integer code() {
			this.code
		}

		@Override
		byte[] bytes() {
			this.text(UTF_8).getBytes(UTF_8)
		}

		@Override
		byte[] bytes(Charset charset) {
			this.text(charset).getBytes(charset)
		}

		@Override
		String text() {
			this.text(UTF_8)
		}

	}

	/**
	 * Fake class modelling an example custom response
	 */
	final class CustomResponse extends StandardResponse {
		private final InputStream stream

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

		@Memoized
		@Override
		String text(Charset charset) {
			this.stream.getText(charset.name())
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
