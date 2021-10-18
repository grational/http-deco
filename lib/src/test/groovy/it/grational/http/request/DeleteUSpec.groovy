package it.grational.http.request

import spock.lang.*
import support.MockServer

import static com.github.tomakehurst.wiremock.client.WireMock.*

class DeleteUSpec extends Specification {

	@Shared MockServer ms
	@Shared MockServer proxy

	def setupSpec() {
		ms = new MockServer(port: 4000)
		ms.start()
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should simply hit the target endpoint with a DELETE request without a payload"() {
		given:
			ms.stubFor (
				delete(urlPathEqualTo(ms.path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			def response = new Delete(url: ms.url).connect()

		then:
			ms.verify (
				1,
				deleteRequestedFor (
					urlPathEqualTo(ms.path)
				)
			)
		and:
			response.text() == ms.ok.body
	}

}
