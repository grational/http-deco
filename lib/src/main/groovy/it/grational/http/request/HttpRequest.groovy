package it.grational.http.request

import it.grational.http.response.HttpResponse

interface HttpRequest {
	HttpResponse connect()
	HttpRequest withHeader(String key, String value)
	HttpRequest withCookie(String key, String value)
	HttpRequest withParameter(String key, def value)
	HttpRequest withURL(URL url)
}
