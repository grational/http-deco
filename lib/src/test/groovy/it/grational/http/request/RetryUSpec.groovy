package it.grational.http.request

import spock.lang.*
import it.grational.http.response.HttpResponse
import static java.net.HttpURLConnection.*

class RetryUSpec extends Specification {

	// 1. fields
	Integer counter = 1
	@Shared Integer retries = 3
	@Shared String url = 'https://www.google.it'
	@Shared String expectedContent = 'Google homepage content'
	@Shared HttpResponse okResponse = new HttpResponse.CustomResponse (
		HTTP_OK,
		new ByteArrayInputStream (
			expectedContent.bytes
		)
	)

  // 2. fixture methods
	// 3. feature methods
	def "Should obtain the result after x retries"() {
		given:
			Get get = Mock()

    when: 'the request to obtain the text is done'
			def actualResult = new Retry(get, retries).connect()

		then: '2 get.text() calls are done underneath'
			2 * get.connect() >> {
				if ( counter < (retries-1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return okResponse
			}
		and: 'no exception is thrown'
			notThrown(RuntimeException)
		and: 'the expected content is retrieved'
			actualResult.code() == HTTP_OK
			actualResult.text() == expectedContent
	}

	def "Should exceed the retry connection limit and raise a RuntimeException"() {
		given:
			Get get = Mock()

    when: 'the request to obtain the text is done'
			def actualResult = new Retry(get, retries).connect()

		then: '3 calls to get.text() are done'
			3 * get.connect() >> {
				if ( counter < (retries+1) ) {
					def exMessage = "Attempt ${counter}"
					counter++
					throw new IOException(exMessage)
				}
				return okResponse
			}
		and: 'The limit exceeded RuntimeException is thrown'
			def exception = thrown(RuntimeException)
			exception.message == "Retry limit (3) exceeded for connection '${get.toString()}'"
	}
  // 4. helper methods
}
