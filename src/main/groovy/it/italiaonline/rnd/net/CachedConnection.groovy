package it.italiaonline.rnd.net

import java.security.MessageDigest
import it.italiaonline.rnd.cache.CacheFile
import it.italiaonline.rnd.cache.Sha1Hash

class CachedConnection implements NetConnection { // {{{

	private final NetConnection origin
	private final BigInteger    leaseTime
	private final String        basepath

	CachedConnection( NetConnection org,
										String basepath,
										BigInteger lt) {
		this.origin    = org
		this.basepath  = basepath
		this.leaseTime = lt
	}

	@Override
	String text() {
		
		String text
		CacheFile ucf = new CacheFile(
											this.basepath,
											new Sha1Hash(
												this.origin.toString()
											).digest()
										)

		if ( ucf.valid(this.leaseTime) ) {
			text = ucf.content()
		} else {
			text = this.origin.text()
			ucf.write(text)
		}

		return text
	}

	@Override
	String toString() {
		this.origin.toString()
	}

} // }}}
