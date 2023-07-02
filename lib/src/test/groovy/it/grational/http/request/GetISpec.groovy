package it.grational.http.request

import spock.lang.*
import it.grational.http.response.HttpResponse
import static java.net.HttpURLConnection.*

class GetISpec extends Specification {

	@Unroll
	def "Should be possibile to ignore #check certificate SSL issues for testing purposes"() {
		when:
			HttpResponse response = new Get (
				"https://${check}.badssl.com".toURL()
			)
			.withParameter('insecure', true)
			.connect()

		then:
			response.code() == HTTP_OK
			response.text() =~ /${check}.*badssl\.com/

		where:
			check << [
				'expired',
				'wrong.host',
				'self-signed',
				'untrusted-root',
				'revoked',
				'pinning-test'
			]
	}

}
