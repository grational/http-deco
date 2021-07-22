package it.grational.http.request

import spock.lang.*
import it.grational.http.header.Authorization

// wiremock imports
import com.github.tomakehurst.wiremock.WireMockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.client.BasicCredentials

class StandardPostSpec extends Specification {

	@Shared String  protocol    = 'http'
	@Shared String  defaultHost = 'localhost'
	@Shared Integer defaultPort = 1234
	@Shared String  origin      = "${protocol}://${defaultHost}:${defaultPort}"
	@Shared String inputPath = '/appropriate/path'
	@Shared URL url = "${origin}${inputPath}".toURL()
	@Shared WireMockServer wms

	@Shared def okResponse = [
		code: 200,
		body: '{"status":"OK"}'
	]

	def setupSpec() {
		wms = new WireMockServer(options().port(defaultPort))
		wms.start()
		Integer.metaClass.getSeconds { delegate * 1000 }

  	wms.stubFor (
			post(urlPathEqualTo(inputPath))
			.willReturn (
				okJson(okResponse.body)
			)
		)
	}

	def cleanupSpec() {
		wms.stop()
	}

	def "Should simply hit the target endpoint with a POST request without a payload"() {
		when:
			String result = new StandardPost(url).text()
		then:
			wms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(inputPath)
				)
			)
		and:
			result == okResponse.body
	}
	
	def "Should hit the target endpoint #url using the POST method with the body #body"() {
		given:
			String inputBody = '{"id":1,"add":1.0}'
		and:
			String user = 'user'
			String pass = 'pass'
			Authorization authHeader = new Authorization (
				username: user,
				password: pass
			)
			String contentTypeHeader = 'application/json; utf-8'
			String acceptHeader = 'application/json'

		when:
			String result = new StandardPost (
				url,
				inputBody,
				[
					(authHeader.name()): authHeader.value(),
					'Content-Type': contentTypeHeader,
					Accept: acceptHeader
				]
			).text()

		then:
			wms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(inputPath)
				)
				.withBasicAuth (
					new BasicCredentials (
						user,
						pass
					)
				)
				.withHeader (
					'Content-Type',
					equalTo(contentTypeHeader)
				)
				.withHeader (
					"Accept",
					equalTo(acceptHeader)
				)
				.withRequestBody (
					equalToJson(inputBody)
				)
			)
		and:
			result == okResponse.body
	}

	def "Should be capable of interrupting a connection when it is slowen then the read timeout"() {
		given:
			String delayedPath = '/more/delayed/path'
			URL delayedUrl = "${origin}${delayedPath}".toURL()
			String inputBody = '{"id":1,"add":1.0}'
		and:
			Integer lessTime = 2.seconds
			Integer moreTime = 5.seconds
		and:
  		wms.stubFor (
				post(urlPathEqualTo(delayedPath))
				.willReturn (
					okJson(okResponse.body)
					.withFixedDelay(moreTime)
				)
			)

		when:
			String result = new StandardPost (
				delayedUrl,
				inputBody,
				[ readTimeout: lessTime ],
				[:]
			).text()

		then:
			wms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(delayedPath)
				)
			)
		and:
			def exception = thrown(SocketTimeoutException)
			exception.message == 'Read timed out'
	}

	def "Should obtain the desired result when the read timeout is greater than the response delay of the connection"() {
		given:
			String delayedPath = '/less/delayed/path'
			URL delayedUrl = "${origin}${delayedPath}".toURL()
			String inputBody = '{"id":1,"add":1.0}'
		and:
			Integer lessTime = 2.seconds
			Integer moreTime = 5.seconds
		and:
  		wms.stubFor (
				post(urlPathEqualTo(delayedPath))
				.willReturn (
					okJson(okResponse.body)
					.withFixedDelay(lessTime)
				)
			)

		when:
			String result = new StandardPost (
				delayedUrl,
				inputBody,
				[ readTimeout: moreTime, ],
				[:]
			).text()

		then:
			wms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(delayedPath)
				)
			)
		and:
			def exception = noExceptionThrown()
		and:
			result == okResponse.body
	}

}
