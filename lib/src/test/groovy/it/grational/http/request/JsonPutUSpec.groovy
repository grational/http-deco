package it.grational.http.request

import spock.lang.*
import groovy.json.JsonOutput
import it.grational.specification.MockServer

// wiremock imports
import static com.github.tomakehurst.wiremock.client.WireMock.*

class JsonPutUSpec extends Specification {

	@Shared MockServer ms

	@Shared String contentTypeHeader = 'application/json'

	@Shared String secondaryPath = '/secondary/path'

	def setupSpec() {
		ms = new MockServer(port: 3535)
		ms.start()

  	ms.stubFor (
			put(urlPathEqualTo(ms.path))
			.willReturn (
				okJson(ms.ok.body)
			)
		)

  	ms.stubFor (
			put(urlPathEqualTo(secondaryPath))
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
			def response = new JsonPut (
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
			response.code() == ms.ok.code
			response.text() == ms.ok.body

		when: 'canonical constructor version'
			response = new JsonPut (
				ms.url,
				stringInput
			).connect()

		then:
			ms.verify (
				2,
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
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should could be capable of handling a map version of the json body"() {
		given:
			Map mapInput = [
				id: 1,
				add: 1.0
			]
		and:
			URL secondaryURL = "${ms.origin}${secondaryPath}".toURL()

		when:
			def response = new JsonPut (
				url: secondaryURL,
				map: mapInput
			).connect()
		then:
			ms.verify (
				1,
				putRequestedFor (
					urlPathEqualTo(secondaryPath)
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

		when: 'canonical constructor version'
			response = new JsonPut (
				secondaryURL,
				mapInput
			).connect()
		then:
			ms.verify (
				2,
				putRequestedFor (
					urlPathEqualTo(secondaryPath)
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
