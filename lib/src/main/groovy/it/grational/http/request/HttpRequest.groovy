package it.grational.http.request

import it.grational.http.response.HttpResponse

interface HttpRequest {
	HttpResponse connect()
}
