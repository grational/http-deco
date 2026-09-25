package it.grational.url

import spock.lang.*

class UserInfoUSpec extends Specification {

	@Unroll
	def "Should decode encoded credentials once before formatting them"() {
		expect:
			UserInfo.encoded(encodedUsername, encodedPassword).toString() ==
				new UserInfo(username, password).toString()

		where:
			encodedUsername | encodedPassword   || username    | password
			'user%40host'   | 'pa%3ass'         || 'user@host' | 'pa:ss'
			'user%20name'   | 'pa+ss'           || 'user name' | 'pa ss'
			'user%2Bname'   | 'pa%2Bss'         || 'user+name' | 'pa+ss'
			'user%2520'     | 'pa%2520%25ss'    || 'user%20'   | 'pa%20%ss'
			'user'          | 'pa%252540ss'     || 'user'      | 'pa%2540ss'
			'user'          | 'caff%C3%A8'      || 'user'      | 'caffè'
			'user'          | ''                || 'user'      | ''
	}

	@Unroll
	def "Should reject malformed encoded credentials"() {
		when:
			UserInfo.encoded(username, password)

		then:
			thrown(IllegalArgumentException)

		where:
			username | password
			'user%'  | 'pass'
			'user'   | 'pa%'
			'user'   | 'pa%2'
			'user'   | 'pa%GG'
	}
}
