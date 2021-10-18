package it.grational.http.request

import java.time.Duration
import it.grational.cache.CacheContainer
import it.grational.http.response.Response
import it.grational.http.response.HttpResponse

class Cache implements HttpRequest {

	private final HttpRequest    origin
	private final CacheContainer cacheContainer
	private final Duration       leaseTime
	private final Closure        missOperation
	private final Boolean        missOpBefore

	Cache (
		HttpRequest org,
		CacheContainer cc,
		Duration lt,
		Closure mos = {},
		Boolean mosBefore = false
	) {
		this.origin         = org
		this.cacheContainer = cc
		this.leaseTime      = lt
		this.missOperation  = mos
		this.missOpBefore   = mosBefore
	}

	@Override
	HttpResponse connect() {
		HttpResponse response
		if ( this.cacheContainer.valid(this.leaseTime) ) {
			response = new HttpResponse.OkResponse (
				new ByteArrayInputStream (
					this.cacheContainer.content().getBytes()
				)
			)
		} else {
			if ( this.missOpBefore )
				this.missOperation()

			response = this.origin.connect()
			this.cacheContainer.write(response.text())

			if ( ! this.missOpBefore )
				this.missOperation()
		}
		return response
	}

	@Override
	String toString() {
		this.origin.toString()
	}

}
