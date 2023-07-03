package it.grational.http.request

/**
 * FunctionalRequest
 * This class is not instantiable since it requires some members to be defined.
 * The subclasses Cache/Retry/Redirections define what is needed and they
 * are, therefore, instantiable.
 */
abstract class FunctionalRequest implements HttpRequest {

	protected HttpRequest origin

	@Override
	public HttpRequest withHeader(String key, String value) {
		this.origin.withHeader(key, value)
	}

	@Override
	public HttpRequest withCookie(String key, String value) {
		this.origin.withCookie(key, value)
	}

	@Override
	public HttpRequest withParameter(String key, def value) {
		this.origin.withParameter(key, value)
	}

	@Override
	public HttpRequest withBasicAuth(String username, String password) {
		this.origin.withBasicAuth(username, password)
	}

	protected HttpRequest withURL(URL url) {
		this.origin.withURL(url)
	}

	@Override
	public String toString() {
		this.origin.toString()
	}

}
