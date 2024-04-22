package it.grational.http.request

import spock.lang.*
import groovy.json.JsonOutput
import it.grational.specification.MockServer
import java.nio.charset.Charset
import static java.nio.charset.StandardCharsets.*

// wiremock imports
import static com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder

class FormUrlencodedPostUSpec extends Specification {

	@Shared MockServer ms

	@Shared String contentTypeHeader = 'application/x-www-form-urlencoded'

	@Shared Charset utf8Charset = UTF_8
	@Shared Map utf16 = [
		path: '/utf16/charset/path',
		charset: UTF_16
	]

	def setupSpec() {
		ms = new MockServer(port: 3535)
		ms.start()

  	ms.stubFor (
			post(urlPathEqualTo(ms.path))
			.willReturn (
				okJson(ms.ok.body)
			)
		)

  	ms.stubFor (
			post(urlPathEqualTo(utf16.path))
			.willReturn (
				okJson(ms.ok.body)
			)
		)
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should simplify making a x-www-form-urlencoded POST request"() {
		given:
			Map params = [
				id: 1,
				add: 1.0
			]
		when:
			def response = new FormUrlencodedPost (
				url: ms.url,
				form: params
			).connect()

		then:
			def requestMatcher = requestPattern (
				ms.path,
				utf8Charset,
				params
			)

			ms.verify (
				1,
				requestMatcher
			)

		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body

		when: 'canonical constructor version'
			response = new FormUrlencodedPost (
				ms.url,
				params
			).connect()

		then:
			ms.verify (
				2,
				requestMatcher
			)

		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body

		when: 'java build style version'
			def request = new FormUrlencodedPost(ms.url)
				
		and:
			params.each { k, v ->
				request = request.withFormParam (
					(k as String),
					(v as String)
				)
			}
		and:
			response = request.connect()

		then:
			ms.verify (
				3,
				requestMatcher
			)

		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body

		when:
			request = new FormUrlencodedPost (
				url: "${ms.origin}${utf16.path}".toURL(),
				form: params,
				charset: utf16.charset
			)
		and:
			def utf16RequestMatcher = requestPattern (
				utf16.path,
				utf16.charset,
				params
			)

		and:
			response = request.connect()

		then:
			ms.verify (
				1,
				utf16RequestMatcher
			)

		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	private RequestPatternBuilder requestPattern (
		String path,
		Charset charset,
		Map params
	) {
		RequestPatternBuilder result = postRequestedFor (
			urlPathEqualTo(path)
		)
		.withHeader (
			'Content-Type',
			equalToIgnoreCase ( // wiremock has a bug, UTF-8 is always uppercase
				"${contentTypeHeader}; charset=${charset.name().toLowerCase()}"
			)
		)

		params.each { k, v ->
			result.withFormParam (
				(k as String), equalTo(v as String)
			)
		}

		return result
	}

}
