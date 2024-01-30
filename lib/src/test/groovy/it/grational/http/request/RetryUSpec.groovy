package it.grational.http.request

import spock.lang.*
import it.grational.http.response.HttpResponse
import static java.net.HttpURLConnection.*
import it.grational.specification.MockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*

class RetryUSpec extends Specification {

	// 1. fields
	@Shared Integer retries = 3
	@Shared String url = 'https://www.google.it'
	@Shared String expectedContent = 'Google homepage content'
	@Shared HttpResponse okResponse
	@Shared String unexpectedContent = 'Error response'
	@Shared HttpResponse koResponse

	@Shared MockServer ms

	// 2. fixture methods
	def setupSpec() {
		okResponse = new HttpResponse.CustomResponse (
			HTTP_OK,
			new ByteArrayInputStream (
				expectedContent.bytes
			)
		)
		koResponse = new HttpResponse.CustomResponse (
			HTTP_BAD_REQUEST,
			new ByteArrayInputStream (
				unexpectedContent.bytes
			),
			true,
			new IOException("Your request was a bad one")
		)

		ms = new MockServer(port: 1100)
		ms.start()
	}

	def cleanupSpec() {
		ms.stop()
	}

	// 3. feature methods
	def "Should obtain the result after x retries"() {
		setup:
			HttpRequest get = Mock()
			Integer counter = 1

		when: 'the request to obtain the text is done'
			HttpResponse actualResult = new Retry(get, retries).connect()

		then: '2 get.connect() calls are done underneath'
			2 * get.connect() >> {
				( counter++ == 1 ) ? koResponse : okResponse
			}
		and: 'no exception is thrown'
			notThrown(RuntimeException)
		and: 'the expected content is retrieved'
			actualResult.code() == HTTP_OK
			actualResult.text() == expectedContent
	}

	def "Should exceed the retry connection limit and raise a RuntimeException"() {
		setup:
			HttpRequest get = Mock()
			Integer counter = 1

		when: 'the request to obtain the text is done'
			def actualResult = new Retry(get, retries).connect()

		then:
			3 * get.connect() >> { koResponse }
		and: 'The limit exceeded RuntimeException is thrown'
			def exception = thrown(RuntimeException)
			exception.message == "Retry limit (3) exceeded for connection '${get.toString()}'"
	}

	def "Should try to hit a closed port retries times before throwing a runtime exception"() {
		setup:
			HttpRequest get = new Get(ms.url)
			HttpRequest retry = new Retry(get, retries)

		when:
			HttpResponse response = retry.connect()

		then: 'The limit exceeded RuntimeException is thrown'
			ms.verify (
				3,
				getRequestedFor (
					urlPathEqualTo(ms.path)
				)
			)
		and:
			def exception = thrown(RuntimeException)
			exception.message == "Retry limit (3) exceeded for connection '${get}'"
	}

	// 4. helper methods
}
