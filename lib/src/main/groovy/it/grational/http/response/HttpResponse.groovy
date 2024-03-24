package it.grational.http.response

import groovy.transform.Memoized
import java.nio.charset.Charset
import static java.nio.charset.StandardCharsets.*
import static it.grational.http.shared.Constants.*

interface HttpResponse {
	Integer code()
	Boolean error()
	Throwable exception()
	byte[] bytes()
	byte[] bytes(Charset charset)
	byte[] bytes(Boolean exceptions)
	byte[] bytes(Charset charset, Boolean exceptions)
	String text()
	String text(Charset charset)
	String text(Boolean exceptions)
	String text(Charset charset, Boolean exceptions)
	String header(String name)
	HttpCookie cookie(String name)

	abstract class StandardResponse implements HttpResponse {
		protected Integer code
		protected Boolean error = false
		protected Throwable exception
		protected Boolean exceptions = true

		@Override
		Integer code() {
			this.code
		}

		@Override
		byte[] bytes() {
			this.text(defaultCharset, exceptions).getBytes(defaultCharset)
		}

		@Override
		byte[] bytes(Charset charset) {
			this.text(charset).getBytes(charset)
		}

		@Override
		byte[] bytes(Boolean exceptions) {
			this.text(defaultCharset, exceptions).getBytes(defaultCharset)
		}

		@Override
		byte[] bytes(Charset charset, Boolean exceptions) {
			this.text(charset, exceptions).getBytes(charset)
		}

		@Override
		String text() {
			this.text(defaultCharset, exceptions)
		}

		@Override
		String text(Charset charset) {
			this.text(charset, exceptions)
		}

		@Override
		String text(Boolean exceptions) {
			this.text(defaultCharset, exceptions)
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
		String text(Charset charset, Boolean exceptions) {
			if (exceptions && this.exception)
				throw this.exception
			else
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
