package it.grational.http.response

import spock.lang.*
import static java.net.HttpURLConnection.*

class HttpResponseUSpec extends Specification {

	@Shared
	String expected = 'expected text response'

	def "Should raise an exception when there is an error and the exceptions are not suppressed"() {
		given:
			HttpResponse customResponse = new HttpResponse.CustomResponse (
				HTTP_BAD_REQUEST,
				new ByteArrayInputStream (
					expected.getBytes()
				),
				true,
				new RuntimeException("dummy exception")
			)
		when:
			customResponse.text()
		then:
			def exception = thrown(RuntimeException)
			exception.message == "dummy exception"
	}

	def "Should not raise an exception when there is an error and the exceptions are suppressed"() {
		given:
			String error = 'this is an error message'
		and:
			HttpResponse customResponse = new HttpResponse.CustomResponse (
				HTTP_BAD_REQUEST,
				new ByteArrayInputStream (
					error.getBytes()
				)
			)
		when:
			String response = customResponse.text(false)
		then:
			noExceptionThrown()
		and:
			response == error
	}

	def "Should be able to read the response twice from the custom response"() {
		given: 'a custom http response'
			HttpResponse customResponse = new HttpResponse.CustomResponse (
				HTTP_OK,
				new ByteArrayInputStream (
					expected.getBytes()
				)
			)

		when: 'the output is called multiple times no exception is thrown'
			String firstTime = customResponse.text()
			String secondTime = customResponse.text()

		then:
			noExceptionThrown()
		and:
			firstTime == expected
			secondTime == expected
	}

}
