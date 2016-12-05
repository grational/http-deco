package it.italiaonline.rnd.net

import spock.lang.Specification

class NoNullConnectionSpec extends Specification {

	def "should throw exception when the required arg is null"()
	throws IllegalArgumentException {
		when:
			new NoNullConnection(null).text()

		then:
			final IllegalArgumentException exception = thrown()
			// Alternate syntax: def exception = thrown(ArticleNotFoundException)
			exception.message == "Input is NULL: can't go ahead."
	}

	def "Should NOT throw an exception where the required arg is provided"() {
		setup:
			URL url = new URL('https://www.google.it')
		when:
			new NoNullConnection(url).text()
		then:
			final IllegalArgumentException exception = notThrown()
	}

}
