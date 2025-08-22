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
	@Shared HttpResponse notFoundResponse
	@Shared HttpResponse badRequestResponse
	@Shared HttpResponse unauthorizedResponse
	@Shared HttpResponse serverErrorResponse

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
			HTTP_INTERNAL_ERROR,
			new ByteArrayInputStream (
				unexpectedContent.bytes
			),
			true,
			new IOException("Internal server error")
		)
		notFoundResponse = new HttpResponse.CustomResponse (
			404,
			new ByteArrayInputStream (
				"Not Found".bytes
			),
			true,
			new IOException("Resource not found")
		)
		badRequestResponse = new HttpResponse.CustomResponse (
			400,
			new ByteArrayInputStream (
				"Bad Request".bytes
			),
			true,
			new IOException("Bad request")
		)
		unauthorizedResponse = new HttpResponse.CustomResponse (
			401,
			new ByteArrayInputStream (
				"Unauthorized".bytes
			),
			true,
			new IOException("Unauthorized")
		)
		serverErrorResponse = new HttpResponse.CustomResponse (
			500,
			new ByteArrayInputStream (
				"Internal Server Error".bytes
			),
			true,
			new IOException("Server error")
		)

		ms = new MockServer(port: 1111)
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
			exception.message == "Retry limit (3) exceeded for connection '${get.toString()}' with exception: '${koResponse.exception()}'"
	}

	def "Should try to hit a closed port retries times before throwing a runtime exception"() {
		setup:
			def closedPortUrl = "http://localhost:9999/closed".toURL()
			HttpRequest get = new Get(closedPortUrl)
			HttpRequest retry = new Retry(get, retries)

		when:
			HttpResponse response = retry.connect()

		then: 'The limit exceeded RuntimeException is thrown'
			def exception = thrown(RuntimeException)
			exception.message.contains("Retry limit (3) exceeded")
			exception.message.contains("ConnectException") || exception.message.contains("Connection refused")
	}

	def "Should not retry on 404 Not Found and return immediately"() {
		setup:
			HttpRequest get = Mock()

		when: 'the request returns 404'
			HttpResponse actualResult = new Retry(get, retries).connect()

		then: 'only 1 get.connect() call is made'
			1 * get.connect() >> notFoundResponse
		and: 'the 404 response is returned immediately without retries'
			actualResult.code() == 404
			actualResult.text(false) == "Not Found"
	}

	def "Should not retry on 400 Bad Request and return immediately"() {
		setup:
			HttpRequest get = Mock()

		when: 'the request returns 400'
			HttpResponse actualResult = new Retry(get, retries).connect()

		then: 'only 1 get.connect() call is made'
			1 * get.connect() >> badRequestResponse
		and: 'the 400 response is returned immediately without retries'
			actualResult.code() == 400
			actualResult.text(false) == "Bad Request"
	}

	def "Should not retry on 401 Unauthorized and return immediately"() {
		setup:
			HttpRequest get = Mock()

		when: 'the request returns 401'
			HttpResponse actualResult = new Retry(get, retries).connect()

		then: 'only 1 get.connect() call is made'
			1 * get.connect() >> unauthorizedResponse
		and: 'the 401 response is returned immediately without retries'
			actualResult.code() == 401
			actualResult.text(false) == "Unauthorized"
	}

	def "Should retry on 500 Server Error"() {
		setup:
			HttpRequest get = Mock()
			Integer counter = 1

		when: 'the request returns 500 then 200'
			HttpResponse actualResult = new Retry(get, retries).connect()

		then: '2 get.connect() calls are made'
			2 * get.connect() >> {
				( counter++ == 1 ) ? serverErrorResponse : okResponse
			}
		and: 'the final successful response is returned'
			actualResult.code() == HTTP_OK
			actualResult.text() == expectedContent
	}

}
