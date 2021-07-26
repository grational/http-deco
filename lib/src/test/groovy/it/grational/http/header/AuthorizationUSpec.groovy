package it.grational.http.header

import spock.lang.*

class AuthorizationUSpec extends Specification {

	@Unroll
	def "Should raise an exception when the parameters are invalid"() {
		when:
			new Authorization (
				username: username,
				password: password
			)
		then:
			def exception = thrown(IllegalArgumentException)
			exception.message == expectedMessage
		where:
			username   | password   || expectedMessage
			null       | 'password' || "[Authorization] Invalid username parameter"
			'username' | null       || "[Authorization] Invalid password parameter"
	}

	@Unroll
	def "Should return the correct value for a basic authentication header"() {
		given:
			def header = new Authorization (
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
