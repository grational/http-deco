package it.grational.http.response

import spock.lang.*
import static java.net.HttpURLConnection.*
import support.JsonClass
import support.SubJsonClass

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

	def "Should be able to parse a json response into an object"() {
		given: 'a json text response'
			String jsonResponse = '''
			|{
			|	"firstKey": "firstValue",
			|	"secondKey": "secondValue",
			|	"arrayKey": [ 1, 2 ],
			|	"subObjectKey": {
			|		"firstSubKey": "firstSubValue"
			|	}
			|}'''.stripMargin()

		and: 'a corresponding class'
			JsonClass expected = new JsonClass (
				firstKey: 'firstValue',
				secondKey: 'secondValue',
				arrayKey: [ 1, 2 ],
				subObjectKey: new SubJsonClass (
					firstSubKey: 'firstSubValue'
				)
			)

		and: 'a custom http response'
			HttpResponse customResponse = new HttpResponse.CustomResponse (
				HTTP_OK,
				new ByteArrayInputStream (
					jsonResponse.getBytes()
				)
			)

		when:
			JsonClass parsedResponse = customResponse.jsonObject(JsonClass)

		then:
			noExceptionThrown()
		and:
			parsedResponse == expected
	}

	def "Should be capable of getting the URL associated with a response"() {
		given:
			URL url = 'http://given.url.io'.toURL()
		and:
			HttpResponse response = new HttpResponse.CustomResponse (
				HTTP_OK,
				new ByteArrayInputStream (
					'expected text response'.getBytes()
				),
				false,
				null,
				url
			)

		expect:
			response.url() == url
	}

}
