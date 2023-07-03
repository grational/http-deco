package it.grational.http.request

import it.grational.http.response.HttpResponse

interface HttpRequest {
	public HttpResponse connect()
	public HttpRequest withHeader(String key, String value)
	public HttpRequest withCookie(String key, String value)
	public HttpRequest withParameter(String key, def value)
	public HttpRequest withBasicAuth(String username, String password)
}
