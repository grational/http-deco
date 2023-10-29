package it.grational.http.request

import it.grational.http.response.HttpResponse
import java.net.URLEncoder
import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.regex.Pattern
import java.util.regex.Matcher
import static java.nio.charset.StandardCharsets.*

class AwsSignV4 extends FunctionalRequest {

	private String service
	private String region
	private String accessKeyId
	private String secretAccessKey
	private String sessionToken
	private String iso8601Date
	private String iso8601Timestamp

	private static final Pattern ENCODED_CHARACTERS_PATTERN = ~/[+*]|%7E|%2F/
	private static final String DIGEST_ALGORITHM = 'SHA-256'
	private static final String HMAC_ALGORITHM = 'AWS4-HMAC-SHA256'
	private static final String AWS4_REQUEST = 'aws4_request'
	private static final String AWS_S3_SERVICE = 's3'

	AwsSignV4(HttpRequest org) {
		this (
			org,
			System.getenv('AWS_SERVICE'),
			System.getenv('AWS_DEFAULT_REGION'),
			System.getenv('AWS_ACCESS_KEY_ID'),
			System.getenv('AWS_SECRET_ACCESS_KEY'),
			System.getenv('AWS_SESSION_TOKEN'),
			iso8601Date(),
			iso8601Timestamp()
		)
	}

	AwsSignV4 (
		HttpRequest org,
		String date,
		String timestamp
	) {
		this (
			org,
			System.getenv('AWS_SERVICE'),
			System.getenv('AWS_DEFAULT_REGION'),
			System.getenv('AWS_ACCESS_KEY_ID'),
			System.getenv('AWS_SECRET_ACCESS_KEY'),
			System.getenv('AWS_SESSION_TOKEN'),
			date,
			timestamp
		)
	}

	AwsSignV4 (
		HttpRequest org,
		String ser,
		String reg,
		String acc,
		String secr
	) {
		this (
			org,
			ser,
			reg,
			acc,
			secr,
			System.getenv('AWS_SESSION_TOKEN'),
			iso8601Date(),
			iso8601Timestamp()
		)
	}

	AwsSignV4 (
		HttpRequest org,
		String ser,
		String reg,
		String acc,
		String secr,
		String sess,
		String date,
		String timestamp
	) {
		this.origin = org
		this.service = ser ?: { throw new IllegalArgumentException("[${this.class.simpleName}] service parameter must be valid") }()
		this.region = reg ?: { throw new IllegalArgumentException("[${this.class.simpleName}] region parameter must be valid") }()
		this.accessKeyId = acc ?: { throw new IllegalArgumentException("[${this.class.simpleName}] access key id parameter must be valid") }()
		this.secretAccessKey = secr ?: { throw new IllegalArgumentException("[${this.class.simpleName}] secret access key parameter must be valid") }()
		this.sessionToken = sess
		this.iso8601Date = date
		this.iso8601Timestamp = timestamp
	}

	@Override
	public HttpResponse connect() {
		this.enrichHeaders()
		this.withHeader (
			'Authorization',
			String.join(' ',
				HMAC_ALGORITHM,
				String.join(', ',
					"Credential=${accessKeyId}/${iso8601Date}/${region}/${service}/${AWS4_REQUEST}",
					"SignedHeaders=${signedHeaders()}",
					"Signature=${signature(secretAccessKey, service, region, sessionToken)}"
				)
			)
		).connect()
	}

	private void enrichHeaders() {
		Map hs = origin.parameters.headers
		addHeaderIfMissing(hs, 'Host', origin.url.host)
		addHeaderIfMissing(hs, 'X-Amz-Date', iso8601Timestamp)
		if ( service == AWS_S3_SERVICE )
			addHeaderIfMissing(hs, 'X-Amz-Content-Sha256', hex(hash(origin.body)))
		if ( sessionToken )
			addHeaderIfMissing(hs, 'X-Amz-Security-Token', sessionToken)
	}

	private void addHeaderIfMissing(Map hs, String h, String v) {
		if ( !hs.containsKey(h) )
			hs << [ (h): v ]
	}

	private String signature (
		String secretAccessKey,
		String service,
		String region,
		String sessionToken
	) {
		String seed = "AWS4${secretAccessKey}"
		byte[] dateKey = hmac(seed, iso8601Date)
		byte[] dateRegionKey = hmac(dateKey, region)
		byte[] dateRegionServiceKey = hmac(dateRegionKey, service)
		byte[] signingKey = hmac(dateRegionServiceKey, AWS4_REQUEST)
		String stringToSign = stringToSign(service, region, sessionToken)
		String signature = hmac(signingKey, stringToSign).encodeHex()
		return signature
	}

	private byte[] hmac (
		String key,
		String data
	) {
		this.hmac (
			key.getBytes(UTF_8),
			data.getBytes(UTF_8)
		)
	}

	private byte[] hmac (
		byte[] key,
		String data
	) {
		this.hmac (
			key,
			data.getBytes(UTF_8)
		)
	}

	private byte[] hmac (
		byte[] key,
		byte[] data,
		String algorithm = 'HmacSHA256'
	) {
		Mac mac = Mac.getInstance(algorithm)
		SecretKeySpec secretKeySpec = new SecretKeySpec (
			key,
			algorithm
		)
		mac.init(secretKeySpec)
		mac.doFinal(data)
	}

	private String stringToSign (
		String service,
		String region,
		String sessionToken
	) {
		String.join('\n',
			HMAC_ALGORITHM,
			iso8601Timestamp,
			scope(service, region),
			hash(canonicalRequest(sessionToken))
		)
	}

	private String scope (
		String service,
		String region
	) {
		String.join('/',
			iso8601Date,
			region,
			service,
			AWS4_REQUEST
		)
	}

	private static String iso8601Timestamp() {
		DateTimeFormatter
			.ofPattern("YYYYMMdd'T'HHmmss'Z'")
			.withZone(ZoneId.of('UTC'))
			.format(Instant.now())
	}

	private static String iso8601Date() {
		DateTimeFormatter
			.BASIC_ISO_DATE
			.format(LocalDate.now())
	}

	private String canonicalRequest(String sessionToken) {
		String.join('\n',
			origin.method,
			awsUrlEncode(origin.url.path, true),
			encodedSortedQueryString(),
			sortedCanonicalHeaders(),
			signedHeaders(),
			hash(origin.body)
		)
	}

	private hash(String input) {
		input.digest(DIGEST_ALGORITHM)
	}

	private String hex(String input, String charset = UTF_8) {
		hex(input.getBytes(charset))
	}

	private String hex(byte[] input) {
		input.encodeHex()
	}

	private String encodedSortedQueryString() {
		this.encodedQueryParams().sort().inject('') { s, k, v ->
			s += "${s ? '&' : ''}${k}=${v}"
		}
	}

	private Map encodedQueryParams() {
		this.queryParams().collectEntries { k, v ->
			[ (awsUrlEncode(k)): awsUrlEncode(v) ]
		}
	}

	private Map queryParams() {
		origin.url.query?.split('&')?.collectEntries { String entry ->
			List split = entry.split('=')
			String key = split[0]
			String value = split?[1] ?: ''
			[ (key): value ]
		} ?: [:]
	}

	private String sortedCanonicalHeaders() {
		filteredLowerCaseHeaders().sort().inject('') { s, h, v ->
			s += "${h}:${normalizeHeaderValue(v)}\n"
		}
	}

	private String normalizeHeaderValue(String v) {
		v.trim().replaceAll(/ +/, ' ')
	}

	private String signedHeaders() {
		filteredLowerCaseHeaders().sort().keySet().collect().inject('') { s, k ->
			s += "${s ? ';' : ''}${k}"
		}
	}

	private Map filteredLowerCaseHeaders() {
		Map fhs = lowerCaseHeaders().findAll { k, v ->
			k =~ /authority|content-type|host|x-amz-.*/
		}
		if ( fhs.containsKey('authority') )
			fhs.remove('host')
		return fhs
	}

	private Map lowerCaseHeaders() {
		origin.parameters.headers.collectEntries { k, v ->
			[ (k.toLowerCase()): v ]
		}
	}

	private String awsUrlEncode (
		String value,
		Boolean path = false,
		String encoding = UTF_8
	) {
		if (!value) return ""

		String encoded = URLEncoder.encode(value, encoding)

		Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded)
		StringBuffer buffer = new StringBuffer(encoded.length())

		while (matcher.find()) {
			String replacement = matcher.group(0)

			if ("+".equals(replacement)) {
				replacement = "%20"
			} else if ("*".equals(replacement)) {
				replacement = "%2A"
			} else if ("%7E".equals(replacement)) {
				replacement = "~"
			} else if (path && "%2F".equals(replacement)) {
				replacement = "/"
			}

			matcher.appendReplacement(buffer, replacement)
		}

		matcher.appendTail(buffer)
		return buffer.toString()
	}

}
