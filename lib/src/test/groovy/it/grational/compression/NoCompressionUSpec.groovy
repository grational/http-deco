package it.grational.compression

import spock.lang.Specification
import spock.lang.Shared

/**
 * The NoCompression class should be a no-operation filter
 */
class NoCompressionUSpec extends Specification {

	@Shared
	NoCompression nc = new NoCompression()

	@Shared
	String longString = """\
	|This is a very long sentence, so long that can't be
	|placed on a single line but it needs to be splitted
	|on multiple lines. To keep the number of columns
	|used as low as possible in addiction a margin is used
	|with the groovy stripMargin() method to remove it
	|when needed""".stripMargin()

	/**
	 * The compressed string should be smaller that the
	 * uncompressed one.
	 */
	def "Use the compress method should keep the string exactly the same"() {
		when:
			String notReallyCompressed = nc.compress(longString)
		then:
			notReallyCompressed == longString
	}

	/**
	 * Uncompressing a compressed string should return the
	 * original string.
	 */
	def "Uncompressing a compressed string should return the original string"() {
		when:
			String compressed   = nc.compress(longString)
			String uncompressed = nc.uncompress(compressed)
		then:
			longString == uncompressed
	}

}
