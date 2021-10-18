package it.grational.http.request

import spock.lang.*
import groovy.json.JsonOutput
import support.MockServer

// wiremock imports
import static com.github.tomakehurst.wiremock.client.WireMock.*

class JsonPutUSpec extends Specification {

	@Shared MockServer ms

	@Shared String contentTypeHeader = 'application/json; utf-8'
	@Shared String acceptHeader = 'application/json'

	def setupSpec() {
		ms = new MockServer(port: 3535)
		ms.start()

  	ms.stubFor (
			put(urlPathEqualTo(ms.path))
			.willReturn (
				okJson(ms.ok.body)
			)
		)
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should simplify making a PUT request with a certain json string"() {
		given:
			String stringInput = '{"id":1,"add":1.0}'
		when:
			def result = new JsonPut (
				url: ms.url,
				json: stringInput
			).connect()

		then:
			ms.verify (
				1,
				putRequestedFor (
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
			result.text() == ms.ok.body
	}

	def "Should could be capable of handling a map version of the json body"() {
		given:
			String mapInput = [
				id: 1,
				add: 1.0
			]
		when:
			def result = new JsonPut (
				url: ms.url,
				map: mapInput
			).connect()
		then:
			ms.verify (
				1,
				putRequestedFor (
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
			result.text() == ms.ok.body
	}

}
