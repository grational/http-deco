package it.italiaonline.rnd.cache

import java.security.MessageDigest

class Sha1Hash { // {{{

	private final String input

	Sha1Hash(String inp) {
		this.input = inp
	}

	String digest() {
		def digest = MessageDigest.getInstance("SHA1").digest(this.input.getBytes())
		new BigInteger(1, digest).toString(16)
	}

} // }}}
