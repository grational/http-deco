package it.grational.http.response

import groovy.transform.Memoized
import java.nio.charset.Charset
import static java.net.HttpURLConnection.*
import groovy.json.JsonSlurper

class Response extends HttpResponse.StandardResponse {
	private URLConnection connection
	private static final JsonSlurper js = new JsonSlurper()

	@Override
	Boolean error() {
		this.error || (this.code >= HTTP_BAD_REQUEST)
	}

	@Override
	Throwable exception() {
		Throwable result
		if ( this.exception ) {
			result = this.exception
		} else if ( this.error() ) {
			this.text(false)
			result = this.exception
		} else {
			throw new IllegalStateException (
				"No exception were been thrown at this moment!"
			)
		}
		return result
	}

	@Override
	URL url() {
		this.connection.getURL()
	}

	@Memoized
	@Override
	String text(Charset charset, Boolean exceptions) {
		this.openInput(exceptions).getText(charset.name())
	}

	@Memoized
	@Override
	byte[] bytes(Boolean exceptions) {
		this.openInput(exceptions).getBytes()
	}

	private InputStream openInput(Boolean exceptions) {
		InputStream result
		if ( exceptions ) {
			result = this.connection.inputStream 
		} else {
			try {
				result = this.connection.inputStream 
			} catch (e) {
				this.error = true
				this.exception = e
				result = this.connection.errorStream
			}
		}
		return result
	}

	@Override
	String header(String name) {
		this.connection.getHeaderField(name)
	}

	@Override
	HttpCookie cookie(String name) {
		CookieHandler.default.cookieStore.cookies.find { cookie ->
			cookie.name == name
		}
	}

	@Override
	<T> T jsonObject(Class<T> type, Charset charset, Boolean exceptions) {
		js.parseText (
			this.text(charset, exceptions)
		).asType(type)
	}

	@Override
	String toString() {
		String r
		r  = "code: ${this.code}"
		r += "\nconnection: ${this.connection}"
		r += "\nerror: ${this.error}"
		r += "\nexception: ${this.exception}"
		return r
	}
}
