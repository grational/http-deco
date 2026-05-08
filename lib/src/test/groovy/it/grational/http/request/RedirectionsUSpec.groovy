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

	def "Should follow redirects for various status codes"() {
		given:
			String redirectPath = "/redirect/${status}"
			String redirectUrl = "${http.origin}${redirectPath}"
			String destinationPath = "/final/${status}"
			String destination = "${http.origin}${destinationPath}"
		and:
			http.stubFor (
				get(urlPathEqualTo(redirectPath))
				.willReturn (
					aResponse()
						.withStatus(status)
						.withHeader("Location", destination)
				)
			)
		and:
			http.stubFor (
				get(urlPathEqualTo(destinationPath))
				.willReturn (
					okJson('{"status": "ok"}')
				)
			)

		when:
			HttpResponse response = new Redirections (
				new Get(redirectUrl.toURL())
			).connect()

		then:
			response.code() == HTTP_OK
			response.url()  == destination.toURL()

		where:
			status << [HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER, 307, 308]
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

	def "Should switch to GET when receiving a 303 See Other redirect"() {
		given:
			String postPath = "/303/post"
			String postUrl = "${http.origin}${postPath}"
			String destinationPath = "/303/get"
			String destination = "${http.origin}${destinationPath}"
		and:
			http.stubFor (
				post(urlPathEqualTo(postPath))
				.willReturn (
					aResponse()
						.withStatus(HTTP_SEE_OTHER)
						.withHeader("Location", destination)
				)
			)
		and:
			http.stubFor (
				get(urlPathEqualTo(destinationPath))
				.willReturn (
					okJson('{"method": "GET"}')
				)
			)

		when:
			HttpResponse response = new Redirections (
				new Post (
					postUrl.toURL(),
					'{"data": "some data"}',
					[:],
					null
				)
			).connect()

		then:
			response.code() == HTTP_OK
			response.text() == '{"method": "GET"}'
			response.url()  == destination.toURL()
	}

	def "Should preserve HEAD method when receiving a 303 See Other redirect"() {
		given:
			String headPath = "/303/head"
			String headUrl = "${http.origin}${headPath}"
			String destinationPath = "/303/head/destination"
			String destination = "${http.origin}${destinationPath}"
		and:
			http.stubFor (
				head(urlPathEqualTo(headPath))
				.willReturn (
					aResponse()
						.withStatus(HTTP_SEE_OTHER)
						.withHeader("Location", destination)
				)
			)
		and:
			http.stubFor (
				head(urlPathEqualTo(destinationPath))
				.willReturn (
					aResponse()
						.withStatus(HTTP_OK)
				)
			)

		when:
			HttpResponse response = new Redirections (
				new Head(headUrl.toURL())
			).connect()

		then:
			http.verify (
				1,
				headRequestedFor (
					urlPathEqualTo(destinationPath)
				)
			)
		and:
			response.code() == HTTP_OK
			response.url()  == destination.toURL()
	}

	def "Should preserve POST method when receiving a method-preserving redirect"() {
		given:
			String postPath = "/${status}/post"
			String postUrl = "${http.origin}${postPath}"
			String destinationPath = "/${status}/destination"
			String destination = "${http.origin}${destinationPath}"
			String body = '{"data": "some data"}'
		and:
			http.stubFor (
				post(urlPathEqualTo(postPath))
				.willReturn (
					aResponse()
						.withStatus(status)
						.withHeader("Location", destination)
				)
			)
		and:
			http.stubFor (
				post(urlPathEqualTo(destinationPath))
				.withRequestBody(equalTo(body))
				.willReturn (
					okJson('{"method": "POST", "status": "ok"}')
				)
			)

		when:
			HttpResponse response = new Redirections (
				new Post (
					postUrl.toURL(),
					body,
					[:],
					null
				)
			).connect()

		then:
			response.code() == HTTP_OK
			response.text() == '{"method": "POST", "status": "ok"}'
			response.url()  == destination.toURL()

		where:
			status << [307, 308]
	}

  // 4. helper methods
}
