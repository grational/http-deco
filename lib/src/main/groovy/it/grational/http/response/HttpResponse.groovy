package it.grational.http.response

interface HttpResponse {
	Integer code()
	String  text()
	byte[]  bytes()
	HttpCookie cookie(String name)

	/**
	 * Fake class modelling an example ok response
	 */
	final class OkResponse implements HttpResponse {
		private final InputStream stream

		OkResponse(InputStream stream) {
			this.stream = stream
		}

		@Override
		Integer code(){
			200
		}

		@Override
		String text() {
			this.stream.text
		}

		@Override
		byte[] bytes() {
			this.stream.bytes
		}

		@Override
		HttpCookie cookie(String name) {
			new HttpCookie (
				name,
				'value'
			)
		}
	}

}
