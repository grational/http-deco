package it.grational.http.request

import spock.lang.*
import it.grational.specification.MockServer

import static com.github.tomakehurst.wiremock.client.WireMock.*
import it.grational.http.proxy.HttpAuthProxy
import it.grational.http.proxy.HttpProxy
import it.grational.http.response.HttpResponse
import it.grational.specification.Environment

import com.github.tomakehurst.wiremock.client.BasicCredentials

class GetUSpec extends Specification {

	@Shared MockServer ms
	@Shared MockServer proxy

	def setupSpec() {
		ms = new MockServer(port: 1100)
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
			HttpResponse response = new Get(ms.url).connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(ms.path)
				)
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
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
			def response = new Get (
				url: url,
				proxy: new HttpProxy (
					host: proxy.host,
					port: proxy.port
				)
			).connect()

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
			response.code() == ms.ok.code
			response.text() == ms.ok.body
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
			def response = new Get (
				url: url,
				proxy: new HttpAuthProxy (
					host: realProxy.host,
					port: realProxy.port,
					username: 'proxy_username',
					password: 'proxy_password'
				)
			).connect()
		then:
			response.code() == ms.ok.code
			response.text() =~ 'google'
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
			def response = new Get (
				url: url,
				proxy: new HttpAuthProxy (
					host: proxy.host,
					port: proxy.port,
					username: 'proxy_username',
					password: 'proxy_password'
				)
			).connect()
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
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}
	
	def "Should read the proper proxy settings from the environment when no proxy is specified"() {
		given:
			def path = '/environment/proxy/path'
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
		and:
			new Environment (
				http_proxy: 'http://localhost:8080',
			).insert()

		when:
			def response = new Get (
				url: url
			).connect()

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
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should avoid using the env proxy when a host matches the hosts in 'no_proxy'"() {
		given:
			def path = '/environment/no_proxy/path'
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
		and:
			new Environment (
				http_proxy: 'http://localhost:8080',
				no_proxy: 'localhost'
			).insert()

		when:
			def response = new Get (
				url: url
			).connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(path)
				)
			)
		and:
			proxy.verify (
				0,
				getRequestedFor (
					urlPathEqualTo(path)
				)
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should leverage the user info coming directly from the URL object"() {
		given:
			Map credentials = [
				user: 'username',
				pass: 'password'
			]
			String userInfo = "${credentials.user}:${credentials.pass}"
		and:
			URL url = "${ms.protocol}://${userInfo}@${ms.authority}${ms.path}".toURL()
		and:
			ms.stubFor (
				get(urlPathEqualTo(ms.path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			def get = new Get (
				url: url,
				headers: [test: 'test']
			)
			HttpResponse response = get.connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(ms.path)
				)
				.withBasicAuth (
					new BasicCredentials (
						credentials.user,
						credentials.pass
					)
				)
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should correctly add the cookies to the request"() {
		given:
			ms.stubFor (
				get(urlPathEqualTo(ms.path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get(
				url: ms.url,
				cookies: [
					first_cookie: 'first cookie value',
					second_cookie: 'second cookie value'
				]
			).connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(ms.path)
				)
				.withCookie('first_cookie', matching('first cookie value'))
				.withCookie('second_cookie', matching('second cookie value'))
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

}
