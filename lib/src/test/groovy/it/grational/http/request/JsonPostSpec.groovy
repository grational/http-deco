package it.grational.http.request

import spock.lang.*
import groovy.json.JsonOutput

// wiremock imports
import com.github.tomakehurst.wiremock.WireMockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

class JsonPostSpec extends Specification {

	@Shared String  protocol    = 'http'
	@Shared String  defaultHost = 'localhost'
	@Shared Integer defaultPort = 1234
	@Shared String  origin      = "${protocol}://${defaultHost}:${defaultPort}"
	@Shared String inputPath = '/appropriate/path'
	@Shared URL url = "${origin}${inputPath}".toURL()
	@Shared WireMockServer wms

	@Shared String contentTypeHeader = 'application/json; utf-8'
	@Shared String acceptHeader = 'application/json'

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

	def "Should simplify making a POST request with a certain json string"() {
		given:
			String stringInput = '{"id":1,"add":1.0}'
		when:
			String result = new JsonPost (
				url: url,
				json: stringInput
			).text()
		then:
			wms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(inputPath)
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
			result == okResponse.body
	}

	def "Should could be capable of handling a map version of the json body"() {
		given:
			String mapInput = [
				id: 1,
				add: 1.0
			]
		when:
			String result = new JsonPost (
				url: url,
				map: mapInput
			).text()
		then:
			wms.verify (
				1,
				postRequestedFor (
					urlPathEqualTo(inputPath)
				)
				.withHeader (
					'Content-Type',
					equalTo(contentTypeHeader)
				)
				.withRequestBody (
					equalToJson(JsonOutput.toJson(mapInput))
				)
			)
		and:
			result == okResponse.body
	}

}
