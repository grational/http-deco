package it.grational.compression

interface Compressor {
	String compress(String input)
	String uncompress(String compressedInput)
}
