package it.grational.http.request

import spock.lang.*
import it.grational.http.response.HttpResponse
import static java.net.HttpURLConnection.*
import it.grational.specification.MockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static java.net.HttpURLConnection.*

class RedirectionsUSpec extends Specification {

	// 1. fields
	@Shared Integer maxRedirects = 3
	@Shared MockServer http
	@Shared MockServer https

  // 2. fixture methods
  def setupSpec() {
		http = new MockServer(protocol: 'http', port: 1234)
		http.start()

		https = new MockServer(protocol: 'https', port: 4321)
		https.start()
  }

	def cleanupSpec() {
		http.stop()
		https.stop()
	}

	// 3. feature methods
	def "Should be capable of following an inter-protocol redirect"() {
		given:
			String temporaryRedirectPath = "/1/temporary/redirect"
			String temporaryRedirect = "${http.origin}${temporaryRedirectPath}"
			String destinationPath = "/1/final/destination"
			String destination = "${https.origin}${destinationPath}"
		and:
			http.stubFor (
				get(urlPathEqualTo(temporaryRedirectPath))
				.willReturn (
					aResponse()
						.withStatus(HTTP_MOVED_TEMP)
						.withHeader (
							"Location",
							destination
						)	
				)
			)
		and:
			https.stubFor (
				get(urlPathEqualTo(destinationPath))
				.willReturn (
					okJson(http.ok.body)
				)
			)

		when:
			HttpResponse response = new Redirections (
				new Get (
					temporaryRedirect.toURL()
				).withParameter (
					'insecure',
					true
				)
			).connect()

		then:
			http.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(temporaryRedirectPath)
				)
			)
		and:
			https.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(destinationPath)
				)
			)
		and:
			response.code() == https.ok.code
			response.text() == https.ok.body
			response.url()  == destination.toURL()
	}

	def "Should not follow more than the given number of redirects"() {
		given:
			String permanentRedirectPath = "/2/permanent/redirect"
			String permanentRedirect = "${http.origin}${permanentRedirectPath}"
			String temporaryRedirectPath = "/2/temporary/redirect"
			String temporaryRedirectBody = "Moved temporarily"
			String temporaryRedirect = "${http.origin}${temporaryRedirectPath}"
			String destinationPath = "/2/final/destination"
			String destination = "${https.origin}${destinationPath}"
		and:
			http.stubFor (
				get(urlPathEqualTo(permanentRedirectPath))
				.willReturn (
					aResponse()
						.withStatus(HTTP_MOVED_PERM)
						.withHeader (
							"Location",
							temporaryRedirect
						)
				)
			)
		and:
			http.stubFor (
				get(urlPathEqualTo(temporaryRedirectPath))
				.willReturn (
					aResponse()
						.withStatus(HTTP_MOVED_TEMP)
						.withHeader (
							"Location",
							destination
						)
						.withBody(temporaryRedirectBody)
				)
			)
		and:
			https.stubFor (
				get(urlPathEqualTo(destinationPath))
				.willReturn (
					okJson(http.ok.body)
				)
			)

		when:
			HttpResponse response = new Redirections (
				new Get (
					permanentRedirect.toURL()
				).withParameter (
					'insecure',
					true
				),
				1
			).connect()

		then:
			http.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(permanentRedirectPath)
				)
			)
		and:
			http.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(temporaryRedirectPath)
				)
			)
		and:
			https.verify (
				0,
				getRequestedFor (
					urlPathEqualTo(destinationPath)
				)
			)
		and:
			response.code() == HTTP_MOVED_TEMP
			response.text() == temporaryRedirectBody
			response.url()  == temporaryRedirect.toURL()
	}

  // 4. helper methods
}
