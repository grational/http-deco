package it.grational.hash

import java.security.MessageDigest

class NoHash implements HashFunction {

	private final String input

	NoHash(String inp) {
		this.input = inp
	}

	String digest() {
		this.input
	}

}
