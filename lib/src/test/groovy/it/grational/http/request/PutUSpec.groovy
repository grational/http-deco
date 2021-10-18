package it.grational.http.request

import spock.lang.*
import support.MockServer

import static com.github.tomakehurst.wiremock.client.WireMock.*

class PutUSpec extends Specification {

	@Shared MockServer ms

	@Shared String contentTypeHeader = 'application/json; utf-8'
	@Shared String acceptHeader = 'application/json'

	def setupSpec() {
		ms = new MockServer(port: 3000)
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

	def "Should simply hit the target endpoint with a PUT request without a payload"() {
		when:
			def response = new Put(url: ms.url).connect()

		then:
			ms.verify (
				1,
				putRequestedFor (
					urlPathEqualTo(ms.path)
				)
			)
		and:
			response.text() == ms.ok.body
	}

	def "Should hit the target endpoint with a PUT request with a certain string payload"() {
		given:
			String stringInput = '{"id":1,"add":1.0}'
		when:
			def result = new Put (
				url: ms.url,
				headers: [
					'Content-Type': 'application/json; utf-8'
				],
				body: stringInput
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

}
