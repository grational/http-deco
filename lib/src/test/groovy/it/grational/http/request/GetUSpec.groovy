package it.grational.http.request

import spock.lang.*
import support.MockServer

import static com.github.tomakehurst.wiremock.client.WireMock.*
import it.grational.http.proxy.HttpAuthProxy
import it.grational.http.proxy.HttpProxy
import it.grational.http.header.ProxyAuthorization

class GetUSpec extends Specification {

	@Shared MockServer ms
	@Shared MockServer proxy

	def setupSpec() {
		ms = new MockServer()
		ms.start()

		proxy = new MockServer(port: 8080)
		proxy.start()
	}

	def cleanupSpec() {
		ms.stop()
		proxy.stop()
	}

	def "Should simply hit the target endpoint with a GET request without a payload"() {
		given:
			ms.stubFor (
				get(urlPathEqualTo(ms.path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			String result = new Get(ms.url).text()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(ms.path)
				)
			)
		and:
			result == ms.ok.body
	}

	def "Should hit the target endpoint through a proxy with a GET request"() {
		given:
			def path = '/proxy/path'
		and:
			def url = "${ms.origin}${path}".toURL()
		and:
			proxy.stubFor (
				get(urlPathEqualTo(path))
				.willReturn (
					aResponse()
					.proxiedFrom(ms.origin)
				)
			)
		and:
			ms.stubFor (
				get(urlPathEqualTo(path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			def result = new Get (
				url: url,
				proxy: new HttpProxy (
					host: proxy.host,
					port: proxy.port
				)
			).text()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(path)
				)
			)
		and:
			proxy.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(path)
				)
			)
		and:
			result == ms.ok.body
	}

	@Ignore
	// TODO: waiting for wiremock to be capable of emulating a real proxy that requires authentication. The current implementation doesn't really work
	def "Should hit the target endpoint through an authenticated proxy with a GET request"() {
		given:
			def path = '/proxy/authenticated/path'
		and:
			def url = "${ms.origin}${path}".toURL()
		and:
			def authHeader = [
				name:  'Proxy-Authorization',
				value: 'Basic cHJveHlfdXNlcm5hbWU6cHJveHlfcGFzc3dvcmQ='
			]
		and:
			proxy.stubFor (
				get(urlPathEqualTo(path))
				.withHeader (
					authHeader.name,
					equalTo(authHeader.value)
				)
				.willReturn (
					aResponse()
					.proxiedFrom(ms.origin)
				)
			)
		and:
			ms.stubFor (
				get(urlPathEqualTo(path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)
		when:
			def result = new Get (
				url: url,
				proxy: new HttpAuthProxy (
					host: proxy.host,
					port: proxy.port,
					username: 'proxy_username',
					password: 'proxy_password'
				)
			).text()
		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(path)
				)
			)
		and:
			proxy.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(path)
				)
			)
		and:
			result == ms.ok.body
	}

	@Ignore
	// TODO: it works but it requires one to manually setup and start a squid (or similar) proxy server that requires authentication using proxy_username and proxy_password
	def "Should hit google through a squid proxy server with authentication"() {
		given:
			def url = "https://www.google.com".toURL()
		and:
			def realProxy = [
				host: 'localhost',
				port: 8888
			]
		when:
			def result = new Get (
				url: url,
				proxy: new HttpAuthProxy (
					host: realProxy.host,
					port: realProxy.port,
					username: 'proxy_username',
					password: 'proxy_password'
				)
			).text()
		then:
			result =~ 'google'
	}

}
