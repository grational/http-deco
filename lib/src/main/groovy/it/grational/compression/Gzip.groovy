package it.grational.compression

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class Gzip implements Compressor {

	String compress(String input){
		def targetStream = new ByteArrayOutputStream()

		def zipStream = new GZIPOutputStream(targetStream)
		zipStream.write(input.getBytes('UTF-8'))
		zipStream.close()

		def zippedBytes = targetStream.toByteArray()
		targetStream.close()

		String compressedString = zippedBytes.encodeBase64()
		return compressedString
	}

	String uncompress(String compressedInput) {
		def inflaterStream = new GZIPInputStream (
			new ByteArrayInputStream (
				compressedInput.decodeBase64()
			)
		)
		String uncompressedString = inflaterStream.getText('UTF-8')
		return uncompressedString
	}
}
