package it.grational.http.request

import spock.lang.*

class RetrySpec extends Specification {

	// 1. fields
	String  url     = 'https://www.google.it'
	String  content = 'Google homepage content'
	Integer counter = 1
	Integer retries = 3

  // 2. fixture methods
	// 3. feature methods
	def "Should obtain the result after x retries"() {
		setup:
			StandardGet stdGet = Mock()
			Number.metaClass.getSeconds { delegate * 1000 }
			Retry retry = new Retry(stdGet, retries)

    when: 'the request to obtain the text is done'
			def actualResult = retry.text()

		then: '2 calls to StandardGet are done and the actual content is retrieved'
			2 * stdGet.text() >> {
				if ( counter < (retries-1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return content
			}
			notThrown(RuntimeException)
			actualResult == content
	}

	def "Should exceed the retry connection limit and raise a RuntimeException"() {
		setup:
			StandardGet stdGet = Mock()
			Number.metaClass.getSeconds { delegate * 1000 }
			Retry retry = new Retry(stdGet, retries)

    when: 'the request to obtain the text is done'
			def actualResult = retry.text()

		then: '3 calls to StandardGet are done and 1 RuntimeException is thrown'
			3 * stdGet.text() >> {
				if ( counter < (retries+1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return content
			}
			def exception = thrown(RuntimeException)
			exception.message == "Retry limit exceeded for connection '${stdGet.toString()}'"
			actualResult != content
	}
  // 4. helper methods
}
