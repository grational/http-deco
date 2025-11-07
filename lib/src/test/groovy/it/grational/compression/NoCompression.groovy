package it.grational.compression

class NoCompression implements Compressor {

	String compress(String input) {
		return input
	}

	String uncompress(String input) {
		return input
	}
}
