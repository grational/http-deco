package it.grational.http.request

import spock.lang.*
import it.grational.specification.MockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*

class PatchUSpec extends Specification {

	@Shared MockServer ms

	def setupSpec() {
		ms = new MockServer(port: 5050)
		ms.start()

		ms.stubFor (
			patch(urlEqualTo(ms.path))
			.willReturn (
				ok(ms.ok.body)
			)
		)
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should simply hit the target endpoint with a REAL PATCH request without a payload"() {
		when:
			def response = new Patch(ms.url).connect()

		then:
			ms.verify (
				1,
				patchRequestedFor(urlEqualTo(ms.path))
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should hit the target endpoint with a PATCH request with a certain string payload"() {
		given:
			String body = '{"key":"value"}'
		when:
			def response = new Patch (
				ms.url,
				body,
				[:],
				null
			).connect()

		then:
			ms.verify (
				1,
				patchRequestedFor(urlEqualTo(ms.path))
				.withRequestBody(equalTo(body))
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

}