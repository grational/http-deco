package it.grational.http.request

import spock.lang.*

class RetrySpec extends Specification {

	// 1. fields
	String url = 'https://www.google.it'
	String expectedContent = 'Google homepage content'
	Integer counter = 1
	Integer retries = 3

  // 2. fixture methods
	// 3. feature methods
	def "Should obtain the result after x retries"() {
		given:
			Get get = Mock()

    when: 'the request to obtain the text is done'
			def actualResult = new Retry(get, retries).text()

		then: '2 get.text() calls are done underneath'
			2 * get.text() >> {
				if ( counter < (retries-1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return expectedContent
			}
		and: 'no exception is thrown'
			notThrown(RuntimeException)
		and: 'the expected content is retrieved'
			actualResult == expectedContent
	}

	def "Should exceed the retry connection limit and raise a RuntimeException"() {
		given:
			Get get = Mock()
    when: 'the request to obtain the text is done'
			def actualResult = new Retry(get, retries).text()

		then: '3 calls to get.text() are done'
			3 * get.text() >> {
				if ( counter < (retries+1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return expectedContent
			}
		and: 'The limit exceeded RuntimeException is thrown'
			def exception = thrown(RuntimeException)
			exception.message == "Retry limit (3) exceeded for connection '${get.toString()}'"
		and: "the expected content has not been retrieved"
			actualResult != expectedContent
	}
  // 4. helper methods
}
