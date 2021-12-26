package it.grational.http.response

class Response implements HttpResponse {
	private Integer code
	@Delegate
	private InputStream stream

	@Override
	Integer code() {
		this.code
	}

	@Override 
	String text(String charset = 'UTF-8') { 
		this.stream.getText(charset)
	}

	@Override
	byte[] bytes(String charset = 'UTF-8') {
		this.stream.getBytes(charset)
	}

}
