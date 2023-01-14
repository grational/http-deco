package it.grational.http.response

import static java.nio.charset.StandardCharsets.*

class Response implements HttpResponse {
	private Integer code
	private URLConnection connection

	@Override
	Integer code() {
		this.code
	}

	@Override
	String text (
		Stream source = Stream.INPUT,
		String charset = UTF_8.name()
	) {
		fromSource(source).getText(charset)
	}

	@Override
	byte[] bytes (
		Stream source = Stream.INPUT,
		String charset = UTF_8.name()
	) {
		fromSource(source).getText(charset)
	}

	private InputStream fromSource(Stream source) {
		(source == Stream.INPUT) ? this.connection.inputStream : this.connection.errorStream
	}

	@Override
	HttpCookie cookie(String name) {
		CookieHandler.default.cookieStore.cookies.find { cookie ->
			cookie.name == name
		}
	}
}
