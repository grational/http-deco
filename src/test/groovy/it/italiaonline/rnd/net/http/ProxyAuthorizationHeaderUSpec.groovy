package it.italiaonline.rnd.net.http

import spock.lang.*

class ProxyAuthorizationHeaderUSpec extends Specification {

	@Unroll
	def "Should raise an exception when the parameters are invalid"() {
		when:
			new ProxyAuthorizationHeader (
				username: username,
				password: password
			)
		then:
			def exception = thrown(IllegalArgumentException)
			exception.message == expectedMessage
		where:
			username   | password   || expectedMessage
			null       | 'password' || "[ProxyAuthorizationHeader] Invalid username parameter"
			'username' | null       || "[ProxyAuthorizationHeader] Invalid password parameter"
	}

	@Unroll
	def "Should return the correct value for a basic authentication header"() {
		given:
			def header = new ProxyAuthorizationHeader (
				username: 'username',
				password: 'password'
			)
		expect:
			header.name()     == expectedName
			header.value()    == expectedValue
			header.toString() == expectedToString
		where:
			username   | password   || expectedName    | expectedValue                    | expectedToString
			'username' | 'password' || 'Proxy-Authorization' | 'Basic dXNlcm5hbWU6cGFzc3dvcmQ=' | "${expectedName}: ${expectedValue}"
	}
}
