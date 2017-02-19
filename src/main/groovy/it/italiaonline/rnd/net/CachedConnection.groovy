package it.italiaonline.rnd.net

import it.italiaonline.rnd.cache.CacheFile

class CachedConnection implements NetConnection { // {{{

	private final NetConnection origin
	private final BigInteger    leaseTime
	private final CacheFile     cacheFile
	private final Closure       missOperation
	private final Boolean       missOpBefore

	CachedConnection (
		NetConnection org,
		CacheFile cfile,
		BigInteger lt,
		Closure mos = {},
		Boolean mosBefore = false
	) {
		this.origin        = org
		this.cacheFile     = cfile
		this.leaseTime     = lt
		this.missOperation = mos
		this.missOpBefore  = mosBefore
	}

	@Override
	String text() {
		String text
		if ( this.cacheFile.valid(this.leaseTime) ) {
			text = this.cacheFile.content()
		} else {
			if ( this.missOpBefore )
				this.missOperation()

			text = this.origin.text()
			this.cacheFile.write(text)

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
