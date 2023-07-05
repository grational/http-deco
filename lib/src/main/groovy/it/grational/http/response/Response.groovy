package it.grational.http.response

import groovy.transform.Memoized
import java.nio.charset.Charset
import static java.net.HttpURLConnection.*

class Response extends HttpResponse.StandardResponse {
	private URLConnection connection

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
			this.text()
			result = this.exception
		} else {
			throw new IllegalStateException (
				"No exception were been thrown at this moment!"
			)
		}
		return result
	}

	@Memoized
	@Override
	String text(Charset charset) {
		this.openInput().getText(charset.name())
	}

	private InputStream openInput() {
		InputStream result
		try {
			result = this.connection.inputStream 
		} catch (e) {
			this.error = true
			this.exception = e
			result = this.connection.errorStream
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
	String toString() {
		String r
		r  = "code: ${this.code}"
		r += "\nconnection: ${this.connection}"
		r += "\nerror: ${this.error}"
		r += "\nexception: ${this.exception}"
		return r
	}
}
