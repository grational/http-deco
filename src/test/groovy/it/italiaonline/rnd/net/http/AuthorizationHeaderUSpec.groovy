package it.italiaonline.rnd.net.http

import spock.lang.*

class AuthorizationHeaderUSpec extends Specification {

	@Unroll
	def "Should raise an exception when the parameters are invalid"() {
		when:
			new AuthorizationHeader (
				username: username,
				password: password
			)
		then:
			def exception = thrown(IllegalArgumentException)
			exception.message == expectedMessage
		where:
			username   | password   || expectedMessage
			null       | 'password' || "[AuthorizationHeader] Invalid username parameter"
			'username' | null       || "[AuthorizationHeader] Invalid password parameter"
	}

	@Unroll
	def "Should return the correct value for a basic authentication header"() {
		given:
			def header = new AuthorizationHeader (
				username: 'username',
				password: 'password'
			)
		expect:
			header.name()     == expectedName
			header.value()    == expectedValue
			header.toString() == expectedToString
		where:
			username   | password   || expectedName    | expectedValue                    | expectedToString
			'username' | 'password' || 'Authorization' | 'Basic dXNlcm5hbWU6cGFzc3dvcmQ=' | "${expectedName}: ${expectedValue}"
	}
}
