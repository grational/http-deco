package it.grational.http.request

import spock.lang.*
import it.grational.http.header.Authorization
import it.grational.specification.MockServer

// wiremock imports
import static com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.client.BasicCredentials

class PostUSpec extends Specification {

	@Shared MockServer ms

	def setupSpec() {
		ms = new MockServer(port: 2020)
		ms.start()
		Integer.metaClass.getSeconds { delegate * 1000 }

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

	def "Should simply hit the target endpoint with a POST request without a payload"() {
		when:
			def response = new Post(url: ms.url).connect()
		then:
			ms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(ms.path)
				)
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
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
			String contentTypeHeader = 'application/json'
			String acceptHeader = 'application/json'

		when:
			def response = new Post (
				url: ms.url,
				body: inputBody,
				headers: [
					(authHeader.name()): authHeader.value(),
					'Content-Type': contentTypeHeader,
					Accept: acceptHeader
				]
			).connect()

		then:
			ms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(ms.path)
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
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should be capable of interrupting a connection when it is slowen then the read timeout"() {
		given:
			String delayedPath = '/more/delayed/path'
			URL delayedUrl = "${ms.origin}${delayedPath}".toURL()
			String inputBody = '{"id":1,"add":1.0}'
		and:
			Integer lessTime = 2.seconds
			Integer moreTime = 5.seconds
		and:
  		ms.stubFor (
				post(urlPathEqualTo(delayedPath))
				.willReturn (
					okJson(ms.ok.body)
					.withFixedDelay(moreTime)
				)
			)

		when:
			def response = new Post (
				url: delayedUrl,
				body: inputBody,
				readTimeout: lessTime
			).connect()

		then:
			ms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(delayedPath)
				)
			)
		and:
			def exception = thrown(SocketTimeoutException)
			exception.message == 'Read timed out'
	}

	def "Should obtain the desired response when the read timeout is greater than the response delay of the connection"() {
		given:
			String delayedPath = '/less/delayed/path'
			URL delayedUrl = "${ms.origin}${delayedPath}".toURL()
			String inputBody = '{"id":1,"add":1.0}'
		and:
			Integer lessTime = 2.seconds
			Integer moreTime = 5.seconds
		and:
  		ms.stubFor (
				post(urlPathEqualTo(delayedPath))
				.willReturn (
					okJson(ms.ok.body)
					.withFixedDelay(lessTime)
				)
			)

		when:
			def response = new Post (
				url: delayedUrl,
				body: inputBody,
				readTimeout: moreTime
			).connect()

		then:
			ms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(delayedPath)
				)
			)
		and:
			def exception = noExceptionThrown()
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

}
