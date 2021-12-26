package it.grational.http.response

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
		String charset = 'UTF-8'
	) { 
		fromSource(source).getText(charset)
	}

	@Override
	byte[] bytes (
		Stream source = Stream.INPUT,
		String charset = 'UTF-8'
	) {
		fromSource(source).getText(charset)
	}

	private InputStream fromSource(Stream source) {
		(source == Stream.INPUT) ? this.connection.inputStream : this.connection.errorStream
	}

}
