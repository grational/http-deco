package it.grational.http.request

import spock.lang.*
import it.grational.specification.MockServer

import static com.github.tomakehurst.wiremock.client.WireMock.*
import it.grational.http.response.Response

class HeadUSpec extends Specification {

	@Shared MockServer ms
	@Shared MockServer proxy

	def setupSpec() {
		ms = new MockServer(port: 1200)
		ms.start()
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should simply hit the target endpoint with a HEAD request without a payload"() {
		given:
			ms.stubFor (
				head(urlPathEqualTo(ms.path))
				.willReturn (
					aResponse()
					.withStatus(ms.ok.code)
				)
			)

		when:
			Response response = new Head(ms.url).connect()

		then:
			ms.verify (
				1,
				headRequestedFor (
					urlPathEqualTo(ms.path)
				)
			)
		and:
			response.code() == ms.ok.code
	}

}
