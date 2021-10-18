package it.grational.http.response

class Response implements HttpResponse {
	private Integer code
	@Delegate
	private InputStream stream

	@Override Integer code()  { this.code }
	@Override String  text()  { this.stream.text }
	@Override byte[]  bytes() { this.stream.bytes }

}
