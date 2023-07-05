package it.grational.http.response

import spock.lang.*
import static java.net.HttpURLConnection.*

class HttpResponseUSpec extends Specification {

	def "Should be able to read the response twice from the custom response"() {
		given: 'an expected text response'
			String expected = 'expected text response'
		and: 'a custom http response'
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
