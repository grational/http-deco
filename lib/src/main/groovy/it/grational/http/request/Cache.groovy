package it.grational.http.request

import java.time.Duration
import it.italiaonline.rnd.cache.CacheContainer

class Cache implements HttpRequest {

	private final HttpRequest  origin
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
	String text() {
		String text
		if ( this.cacheContainer.valid(this.leaseTime) ) {
			text = this.cacheContainer.content()
		} else {
			if ( this.missOpBefore )
				this.missOperation()

			text = this.origin.text()
			this.cacheContainer.write(text)

			if ( ! this.missOpBefore )
				this.missOperation()
		}
		return text
	}

	@Override
	String toString() {
		this.origin.toString()
	}

}
