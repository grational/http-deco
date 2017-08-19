package it.italiaonline.rnd.net

import java.time.Duration
import it.italiaonline.rnd.cache.CacheContainer

class CachedConnection implements NetConnection { // {{{

	private final NetConnection  origin
	private final Duration       leaseTime
	private final CacheContainer cacheContainer
	private final Closure        missOperation
	private final Boolean        missOpBefore

	CachedConnection (
		NetConnection org,
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

} // }}}
