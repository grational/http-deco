package it.grational.test

import com.github.tomakehurst.wiremock.WireMockServer
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

class MockServer {
	final String protocol
	final String host = 'localhost'
	final int port
	final String path = '/appropriate/path'
	final Map ok = [code: 200, body: '{"status":"OK"}']
	@Delegate final WireMockServer server

	MockServer(Map settings) {
		protocol = settings.protocol ?: 'http'
		port = settings.port
		server = new WireMockServer(protocol == 'https'
			? options().httpDisabled(true).httpsPort(port)
			: options().port(port))
	}

	String getAuthority() { "${host}:${port}" }
	String getOrigin() { "${protocol}://${authority}" }
	URL getUrl() { new URI(origin + path).toURL() }
}
