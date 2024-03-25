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

class AwsSignV4USpec extends Specification {

	@Shared MockServer ms
	@Shared String commonPath = '/path/to/resource'
	@Shared String commonBody = '{"id":"id_value","object":{"key":"value"}}'
	@Shared String commonAccessKeyId = 'AKIAIOSFODNN7EXAMPLE'
	@Shared String commonSecretAccessKey = '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'
	@Shared String commonRegion = 'eu-west-1'
	@Shared String commonService = 'execute-api'
	@Shared String commonEmptyAccessToken = ''

	def setupSpec() {
		ms = new MockServer(port: 4444)
		ms.start()
	}

	def cleanupSpec() {
		ms.stop()
	}

	def "Should add the appropriate aws sign v4 headers to the request with a couple of query params"() {
		given:
			String query = 'q1=v1&q2=v2'
			String relativeReference = "${commonPath}?${query}"
			URL url = "http://localhost:4444${relativeReference}".toURL()
		and:
			String timestamp = '20231027T172621Z'
			String date = timestamp.take(8)

		when:
			new AwsSignV4 (
				new JsonPost (
					url: url,
					json: commonBody
				),
				commonService,
				commonRegion,
				commonAccessKeyId,
				commonSecretAccessKey,
				commonEmptyAccessToken,
				date,
				timestamp
			).connect()

		then:
			ms.verify (
				1,
				postRequestedFor (
					urlEqualTo(relativeReference)
				)
				.withHeader('Host', matching('localhost:4444'))
				.withHeader('Content-Type', containing('application/json'))
				.withHeader('X-Amz-Date', matching(timestamp))
				.withHeader (
					'Authorization',
					matching (
						String.join(' ',
							'AWS4-HMAC-SHA256',
							String.join(', ',
								"Credential=${commonAccessKeyId}/${date}/${commonRegion}/${commonService}/aws4_request",
								'SignedHeaders=content-type;host;x-amz-date',
								'Signature=3f1dd539d4c80b006280d38c80da7a49e8ef9343b56d235c54f7b0bcdf6c7279'
							)
						)
					)
				)
			)
	}

	// NOTE: the curl 7.81.0 implementation is incorrect for a single no-value query parameters
	def "Should add the appropiate headers to the request with a single no-value query parameter and env variables"() {
		given:
			String query = 'q3'
			String relativeReference = "${commonPath}?${query}"
			URL url = "http://localhost:4444${relativeReference}".toURL()
			String timestamp = '20231029T092650Z'
			String date = timestamp.take(8)
		and:
			def env = new Environment (
				AWS_SERVICE: commonService,
				AWS_DEFAULT_REGION: commonRegion,
				AWS_ACCESS_KEY_ID: commonAccessKeyId,
				AWS_SECRET_ACCESS_KEY: commonSecretAccessKey
			)
			env.insert()

		when:
			new AwsSignV4 (
				new JsonPost (
					url: url,
					json: commonBody
				),
				date,
				timestamp
			).connect()

		then:
			ms.verify (
				1,
				postRequestedFor (
					urlEqualTo(relativeReference)
				)
				.withHeader('Host', matching('localhost:4444'))
				.withHeader('Content-Type', containing('application/json'))
				.withHeader('X-Amz-Date', matching(timestamp))
				.withHeader (
					'Authorization',
					matching (
						String.join(' ',
							'AWS4-HMAC-SHA256',
							String.join(', ',
								"Credential=${commonAccessKeyId}/${date}/${commonRegion}/${commonService}/aws4_request",
								'SignedHeaders=content-type;host;x-amz-date',
								'Signature=2e033c20357c39772c765f772913b9bbdb722429f1e88e7bfa582874bf16f802'
							)
						)
					)
				)
			 )
	}

}
