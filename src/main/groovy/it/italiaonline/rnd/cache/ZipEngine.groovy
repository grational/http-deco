package it.italiaonline.rnd.cache

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class ZipEngine {
	private final String input

	ZipEngine(String inp) {
		this.input = inp
	}

	String zipped(){
		def targetStream = new ByteArrayOutputStream()
		def zipStream = new GZIPOutputStream(targetStream)
		zipStream.write(this.input.getBytes('UTF-8'))
		zipStream.close()
		def zippedBytes = targetStream.toByteArray()
		targetStream.close()
		return zippedBytes.encodeBase64()
	}
	
	String unzipped(String compressed){
		def inflaterStream = new GZIPInputStream(new ByteArrayInputStream(this.input.decodeBase64()))
		def uncompressedString = inflaterStream.getText('UTF-8')
		return uncompressedString
	}
}
