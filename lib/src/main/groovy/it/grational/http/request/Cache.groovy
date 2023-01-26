package it.grational.http.request

import java.time.Duration
import static java.net.HttpURLConnection.*

import it.grational.cache.CacheContainer
import it.grational.http.response.Response
import it.grational.http.response.HttpResponse

/*
 * NOTE: the cache container should not be valid 
 * if it is empty to make the cache to work properly
 */
class Cache implements HttpRequest {

	private final HttpRequest    origin
	private final CacheContainer cacheContainer
	private final Duration       leaseTime
	private final Closure        missOperation
	private final Boolean        missOpBefore
	private final Boolean        cacheErrors
	private final String         separator = '\n'

	Cache (
		HttpRequest org,
		CacheContainer cc,
		Duration lt,
		Closure mos = {},
		Boolean mosBefore = false,
		Boolean ce = false
	) {
		this.origin         = org
		this.cacheContainer = cc
		this.leaseTime      = lt
		this.missOperation  = mos
		this.missOpBefore   = mosBefore
		this.cacheErrors    = ce
	}

	@Override
	HttpResponse connect() {
		HttpResponse response
		if ( this.cacheContainer.valid(this.leaseTime) ) {
			response = this.cachedResponse()
		} else {
			if ( this.missOpBefore )
				this.missOperation()

			response = this.origin.connect()

			Boolean responseError = response.error()
			if ( !response.error() || cacheErrors ) {
				String joinedResponse = this.joinedResponse(response)
				this.cacheContainer.write (
					joinedResponse
				)
				response = this.cachedResponse()
			}

			if ( ! this.missOpBefore )
				this.missOperation()
		}

		return response
	}

	private HttpResponse cachedResponse() {
		List cacheLines = this.cacheLines()
		return new HttpResponse.CustomResponse (
			this.cachedCode(cacheLines),
			this.cachedContent(cacheLines)
		)
	}

	@Override
	String toString() {
		this.origin.toString()
	}

	private List<String> cacheLines() {
		this.cacheContainer
			.content()
			.readLines()
	}

	private Integer cachedCode(List lines) {
		lines.first() as Integer
	}

	private ByteArrayInputStream cachedContent(List lines) {
		new ByteArrayInputStream (
			lines.drop(1).join(this.separator).getBytes()
		)
	}

	private String joinedResponse(HttpResponse response) {
		String.join (
			this.separator,
			response.code() as String,
			response.text()
		)
	}

}
