package it.italiaonline.rnd.net

import it.italiaonline.rnd.cache.CacheFile

class CachedConnection implements NetConnection { // {{{

	private final NetConnection origin
	private final BigInteger    leaseTime
	private final CacheFile     cacheFile
	private final Closure       missOperation

	CachedConnection (
		NetConnection org,
		CacheFile cfile,
		BigInteger lt,
		Closure mos = {}
	) {
		this.origin        = org
		this.cacheFile     = cfile
		this.leaseTime     = lt
		this.missOperation = mos
	}

	@Override
	String text() {
		String text
		if ( this.cacheFile.valid(this.leaseTime) ) {
			text = this.cacheFile.content()
		} else {
			text = this.origin.text()
			this.cacheFile.write(text)
			this.missOperation()
		}
		return text
	}

	@Override
	String toString() {
		this.origin.toString()
	}

} // }}}
