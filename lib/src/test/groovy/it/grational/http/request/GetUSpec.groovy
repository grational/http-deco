package it.grational.http.request

import spock.lang.*

import it.grational.proxy.HttpAuthProxy
import it.grational.proxy.HttpProxy
import it.grational.http.response.HttpResponse
import it.grational.specification.Environment
import static java.net.HttpURLConnection.*

import it.grational.specification.MockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*
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

	def "Should correctly add cookies to the request"() {
		given:
			ms.stubFor (
				get(urlPathEqualTo(ms.path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get (
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

	def "Should correctly add cookies to the request using fluent API"() {
		given:
			URL subUrl = "${ms.url}/fluent".toURL()
			String subPath = "${ms.path}/fluent"
		and:
			ms.stubFor (
				get(urlPathEqualTo(subPath))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get(subUrl)
				.withCookie('first_cookie', 'first cookie value')
				.withCookie('second_cookie', 'second cookie value')
				.connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(subPath)
				)
				.withCookie('first_cookie', matching('first cookie value'))
				.withCookie('second_cookie', matching('second cookie value'))
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should correctly add headers to the request"() {
		given:
			ms.stubFor (
				get(urlPathEqualTo(ms.path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get (
				url: ms.url,
				headers: [
					first_header: 'first header value',
					second_header: 'second header value'
				]
			).connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(ms.path)
				)
				.withHeader('first_header', matching('first header value'))
				.withHeader('second_header', matching('second header value'))
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should correctly add the cookies using fluent api to the request"() {
		given:
			String subPath = "/fluent"
			URL subUrl = "${ms.origin}${subPath}".toURL()
		and:
			ms.stubFor (
				get(urlPathEqualTo(subPath))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get(subUrl)
				.withHeader('first_header', 'first header value')
				.withHeader('second_header', 'second header value')
				.connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(subPath)
				)
				.withHeader('first_header', matching('first header value'))
				.withHeader('second_header', matching('second header value'))
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should be capable of catching the server error message"() {
		given:
			def error = [
				code: 500,
				message: 'server error',
				path: '/error/path'
			]
		and:
			ms.stubFor (
				get(urlPathEqualTo(error.path))
				.willReturn (
					aResponse()
					.withStatus(error.code)
					.withBody(error.message)
				)
			)
		and:
			def url = "${ms.origin}${error.path}".toURL()

		when:
			HttpResponse response = new Get(url).connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(error.path)
				)
			)
		and: 'without an explicit source tries INPUT then ERROR'
			response.code() == error.code
			response.text() == error.message
	}

	def "Should be capable of retrieving the cookies set by the server"() {
		given:
			def cookies = [
				first: [
					name: 'first_name',
					value: 'first_value',
				],
				second: [
					name: 'second_name',
					value: 'second_value'
				]
			]
		and:
			def setCookiesPath = '/set/cookies/path'
		and:
			ms.stubFor (
				get(urlPathEqualTo(setCookiesPath))
				.willReturn (
					okJson(ms.ok.body)
					.withHeader (
						'Set-Cookie',
						"${cookies.first.name}=${cookies.first.value}"
					)
					.withHeader (
						'Set-Cookie',
						"${cookies.second.name}=${cookies.second.value}"
					)
				)
			)
		and:
			def url = "${ms.origin}${setCookiesPath}".toURL()

		when:
			HttpResponse response = new Get(url).connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(setCookiesPath)
				)
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
		and:
			def firstCookie = response.cookie(cookies.first.name) 
			firstCookie.toString() == "${cookies.first.name}=${cookies.first.value}"
		and:
			def secondCookie = response.cookie(cookies.second.name) 
			secondCookie.toString() == "${cookies.second.name}=${cookies.second.value}"
	}

	def "Should be capable of following an intra-protocol redirect by default"() {
		given:
			String permanentRedirectPath = "/1/permanent/redirect"
			String permanentRedirect = "${ms.origin}${permanentRedirectPath}"
			String temporaryRedirectPath = "/1/temporary/redirect"
			String temporaryRedirect = "${ms.origin}${temporaryRedirectPath}"
			String destinationPath = "/1/final/destination"
			String destination = "${ms.origin}${destinationPath}"
		and:
			ms.stubFor (
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
			ms.stubFor (
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
			ms.stubFor (
				get(urlPathEqualTo(destinationPath))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get (
				permanentRedirect.toURL()
			)
			.connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(permanentRedirectPath)
				)
			)
		and:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(temporaryRedirectPath)
				)
			)
		and:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(destinationPath)
				)
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
	}

	def "Should be possible to disable redirects"() {
		given:
			String temporaryRedirectPath = "/2/temporary/redirect"
			String temporaryRedirectBody = "Moved temporarily"
			String temporaryRedirect = "${ms.origin}${temporaryRedirectPath}"
			String destinationPath = "/2/final/destination"
			String destination = "${ms.origin}${destinationPath}"
		and:
			ms.stubFor (
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
			ms.stubFor (
				get(urlPathEqualTo(destinationPath))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get (
				temporaryRedirect.toURL()
			)
			.withParameter('followRedirects', false)
			.connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(temporaryRedirectPath)
				)
			)
		and:
			ms.verify (
				0,
				getRequestedFor (
					urlPathEqualTo(destinationPath)
				)
			)
		and:
			response.code() == HttpURLConnection.HTTP_MOVED_TEMP
			response.text() == temporaryRedirectBody
	}

	def "Should be capable of changing the userInfo a given URL"() {
		given:
			String protocol = 'http'
			String userInfo = 'oldUser:oldPass'
			String residual = 'hostname/path'
		and:
			String username = 'newUser'
			String password = 'newPass'

		when:
			HttpRequest request = new Get (
				"${protocol}://${userInfo}@${residual}".toURL()
			)
			.withBasicAuth (
				username,
				password
			)

		then:
			request.url.toString() == "${protocol}://${username}:${password}@${residual}"
	}

	def "Should be able to read the response twice from the concrete response"() {
		given:
			String path = '/twice/response/text'
			URL pathURL = "${ms.origin}${path}".toURL()
		and:
			ms.stubFor (
				get(urlPathEqualTo(path))
				.willReturn (
					okJson(ms.ok.body)
				)
			)

		when:
			HttpResponse response = new Get(pathURL).connect()

		then:
			ms.verify (
				1,
				getRequestedFor (
					urlPathEqualTo(path)
				)
			)
		and:
			response.code() == ms.ok.code
			response.text() == ms.ok.body
		and:
			response.text() == ms.ok.body
			noExceptionThrown()
	}


}
