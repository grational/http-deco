package it.grational.http.request

import spock.lang.*
import groovy.json.JsonOutput
import support.MockServer

// wiremock imports
import static com.github.tomakehurst.wiremock.client.WireMock.*

class JsonPostSpec extends Specification {

	@Shared MockServer ms

	@Shared String contentTypeHeader = 'application/json; utf-8'
	@Shared String acceptHeader = 'application/json'

	def setupSpec() {
		ms = new MockServer()
		ms.start()
		Integer.metaClass.getSeconds { delegate * 1000 }

  	ms.stubFor (
			post(urlPathEqualTo(ms.path))
			.willReturn (
				okJson(ms.ok.body)
			)
		)
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should simplify making a POST request with a certain json string"() {
		given:
			String stringInput = '{"id":1,"add":1.0}'
		when:
			String result = new JsonPost (
				url: ms.url,
				json: stringInput
			).text()

		then:
			ms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(ms.path)
				)
				.withHeader (
					'Content-Type',
					equalTo(contentTypeHeader)
				)
				.withRequestBody (
					equalToJson(stringInput)
				)
			)

		and:
			result == ms.ok.body
	}

	def "Should could be capable of handling a map version of the json body"() {
		given:
			String mapInput = [
				id: 1,
				add: 1.0
			]
		when:
			String result = new JsonPost (
				url: ms.url,
				map: mapInput
			).text()
		then:
			ms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(ms.path)
				)
				.withHeader (
					'Content-Type',
					equalTo(contentTypeHeader)
				)
				.withRequestBody (
					equalToJson(JsonOutput.toJson(mapInput))
				)
			)
		and:
			result == ms.ok.body
	}

}
