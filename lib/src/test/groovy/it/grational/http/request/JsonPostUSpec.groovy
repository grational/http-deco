package it.grational.http.request

import spock.lang.*
import groovy.json.JsonOutput
import it.grational.specification.MockServer

// wiremock imports
import static com.github.tomakehurst.wiremock.client.WireMock.*

class JsonPostUSpec extends Specification {

	@Shared MockServer ms

	@Shared String contentTypeHeader = 'application/json'

	def setupSpec() {
		ms = new MockServer(port: 2525)
		ms.start()

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
			def response = new JsonPost (
				url: ms.url,
				json: stringInput
			).connect()

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
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should could be capable of handling a map version of the json body"() {
		given:
			String mapInput = [
				id: 1,
				add: 1.0
			]
		when:
			def response = new JsonPost (
				url: ms.url,
				map: mapInput
			).connect()
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
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

}
