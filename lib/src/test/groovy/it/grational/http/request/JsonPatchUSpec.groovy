package it.grational.http.request

import spock.lang.*
import it.grational.specification.MockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*

class JsonPatchUSpec extends Specification {

	@Shared MockServer ms

	def setupSpec() {
		ms = new MockServer(port: 5051)
		ms.start()

		ms.stubFor (
			patch(urlEqualTo(ms.path))
            .withHeader('Content-Type', containing('application/json'))
			.willReturn (
				ok(ms.ok.body)
			)
		)
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should simplify making a PATCH request with a certain json string"() {
		given:
			String json = '{"id":1,"add":1.0}'
		when:
			def response = new JsonPatch (
				url: ms.url,
				json: json
			).connect()

		then:
			ms.verify (
				1,
				patchRequestedFor(urlEqualTo(ms.path))
                .withHeader('Content-Type', containing('application/json'))
				.withRequestBody(equalToJson(json))
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}
}