package it.grational.http.response

import static java.net.HttpURLConnection.*
import static java.nio.charset.StandardCharsets.*

class Response implements HttpResponse {
	private Integer code
	private URLConnection connection
	private Boolean error = false

	@Override
	Integer code() {
		this.code
	}

	@Override
	Boolean error() {
		this.error || (this.code >= HTTP_BAD_REQUEST)
	}

	@Override
	String text() {
		this.text(UTF_8.name())
	}

	@Override
	String text(String charset) {
		this.openInput().getText(charset)
	}

	@Override
	byte[] bytes() {
		this.openInput().bytes
	}

	private InputStream openInput() {
		InputStream result
		try {
			result = this.connection.inputStream 
		} catch (e) {
			this.error = true
			result = this.connection.errorStream
		}
		return result
	}

	@Override
	HttpCookie cookie(String name) {
		CookieHandler.default.cookieStore.cookies.find { cookie ->
			cookie.name == name
		}
	}
}
