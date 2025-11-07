package it.grational.hash

import spock.lang.Specification
import spock.lang.Shared

/**
 * The Sha1Hash class returns the sha1hash of a string passed
 * as parameter.
 */
class NoHashSpec extends Specification {

	@Shared
	String shortString = 'testing string'

	@Shared
	String longString = """\
	|This is a very long sentence, so long that can't be
	|placed on a single line but it needs to be splitted
	|on multiple lines. To keep the number of columns
	|used as low as possible in addiction a margin is used
	|with the groovy stripMargin() method to remove it
	|when needed.""".stripMargin()

	/**
	 * The hash returned by the class should be equal to the
	 * precalculated one
	 */
	def "The hash returned by NoHash should be equal to the original string"() {

		expect:
			String generatedHash = new NoHash(input).digest()
			generatedHash == precalculatedHash

		where:
			input       | precalculatedHash
			longString  | longString
			shortString | shortString
			''          | ''
	}

}
