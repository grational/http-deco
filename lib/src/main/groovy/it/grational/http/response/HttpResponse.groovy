package it.grational.http.response

import groovy.transform.Memoized
import java.nio.charset.Charset
import groovy.json.JsonSlurper
import static java.nio.charset.StandardCharsets.*
import static it.grational.http.shared.Constants.*

interface HttpResponse {
	URL url()
	Integer code()
	Boolean error()
	Throwable exception()
	byte[] bytes()
	byte[] bytes(Boolean exceptions)
	String text()
	String text(Charset charset)
	String text(Boolean exceptions)
	String text(Charset charset, Boolean exceptions)
	String header(String name)
	HttpCookie cookie(String name)
	<T> T jsonObject(Class<T> type)
	<T> T jsonObject(Class<T> type, Charset charset)
	<T> T jsonObject(Class<T> type, Boolean exceptions)
	<T> T jsonObject(Class<T> type, Charset charset, Boolean exceptions)

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
			this.bytes(exceptions)
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

		@Override
		<T> T jsonObject(Class<T> type) {
			this.jsonObject(type, defaultCharset, exceptions)
		}

		@Override
		<T> T jsonObject(Class<T> type, Charset charset) {
			this.jsonObject(type, charset, exceptions)
		}

		@Override
		<T> T jsonObject(Class<T> type, Boolean exceptions) {
			this.jsonObject(type, defaultCharset, exceptions)
		}

	}

	/**
	 * Fake class modelling an example custom response
	 */
	final class CustomResponse extends StandardResponse {
		private final URL url
		private final InputStream stream
		private static final JsonSlurper js = new JsonSlurper()

		CustomResponse (
			Integer code,
			InputStream stream,
			Boolean error = false,
			Throwable exception = null,
			URL url = 'http://localhost'.toURL()
		) {
			this.code = code
			this.stream = stream
			this.error = error
			this.exception = exception
			this.url = url
		}

		@Override 
		URL url() {
			this.url
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
			return this.stream.getText(charset.name())
		}

		@Memoized
		@Override
		byte[] bytes(Boolean exceptions) {
			if (exceptions && this.exception)
				throw this.exception
			return this.stream.getBytes()
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
		<T> T jsonObject(Class<T> type, Charset charset, Boolean exceptions) {
			js.parseText (
				this.text(charset, exceptions)
			).asType(type)
		}

		@Override
		String toString() {
			"${this.class.simpleName} (code:${code}, stream:${stream.text}, error:${error}, exception:${exception})"
		}

	}

}
