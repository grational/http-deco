package it.grational.http.request;

import it.grational.http.response.HttpResponse;
import it.grational.http.shared.Constants;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AwsSignV4 extends FunctionalRequest {

	private String service;
	private String region;
	private String accessKeyId;
	private String secretAccessKey;
	private String sessionToken;
	private String iso8601Date;
	private String iso8601Timestamp;

	private static final Pattern ENCODED_CHARACTERS_PATTERN = Pattern.compile("[+*]|%7E|%2F");
	private static final String DIGEST_ALGORITHM = "SHA-256";
	private static final String HMAC_ALGORITHM = "AWS4-HMAC-SHA256";
	private static final String AWS4_REQUEST = "aws4_request";
	private static final String AWS_S3_SERVICE = "s3";

	public AwsSignV4(HttpRequest org) {
		this (
			org,
			System.getenv("AWS_SERVICE"),
			System.getenv("AWS_DEFAULT_REGION"),
			System.getenv("AWS_ACCESS_KEY_ID"),
			System.getenv("AWS_SECRET_ACCESS_KEY"),
			System.getenv("AWS_SESSION_TOKEN"),
			iso8601Date(),
			iso8601Timestamp()
		);
	}

	public AwsSignV4(HttpRequest org, String date, String timestamp) {
		this (
			org,
			System.getenv("AWS_SERVICE"),
			System.getenv("AWS_DEFAULT_REGION"),
			System.getenv("AWS_ACCESS_KEY_ID"),
			System.getenv("AWS_SECRET_ACCESS_KEY"),
			System.getenv("AWS_SESSION_TOKEN"),
			date,
			timestamp
		);
	}

	public AwsSignV4(HttpRequest org, String ser, String reg, String acc, String secr) {
		this (
			org,
			ser,
			reg,
			acc,
			secr,
			System.getenv("AWS_SESSION_TOKEN"),
			iso8601Date(),
			iso8601Timestamp()
		);
	}

	public AwsSignV4 (
		HttpRequest org,
		String ser,
		String reg,
		String acc,
		String secr,
		String sess,
		String date,
		String timestamp
	) {
		super(org);
		if (ser == null)
			throw new IllegalArgumentException (
				"[" + this.getClass().getSimpleName() + "] service parameter must be valid"
			);
		this.service = ser;
		if (reg == null)
			throw new IllegalArgumentException (
				"[" + this.getClass().getSimpleName() + "] region parameter must be valid"
			);
		this.region = reg;
		if (acc == null)
		throw new IllegalArgumentException (
			"[" + this.getClass().getSimpleName() + "] access key id parameter must be valid"
		);
		this.accessKeyId = acc;
		if (secr == null)
			throw new IllegalArgumentException (
				"[" + this.getClass().getSimpleName() + "] secret access key parameter must be valid"
			);
		this.secretAccessKey = secr;
		this.sessionToken = sess;
		this.iso8601Date = date;
		this.iso8601Timestamp = timestamp;
	}

	@Override
	public HttpResponse connect() throws java.io.IOException {
		this.enrichHeaders();
		this.withHeader(
			"Authorization",
			String.join(" ",
				HMAC_ALGORITHM,
				String.join(", ",
						"Credential=" + accessKeyId + "/" + iso8601Date + "/" + region + "/" + service + "/" + AWS4_REQUEST,
						"SignedHeaders=" + signedHeaders(),
						"Signature=" + signature(secretAccessKey, service, region, sessionToken)
					)
			)
		);
		return this.origin.connect();
	}

	private void enrichHeaders() {
		StandardRequest std = (StandardRequest) origin;
		Map<String, String> headers = getHeaders(std);
		std.appendContentTypeCharset(headers);
		addHeaderIfMissing(headers, "Host", std.url.getHost());
		addHeaderIfMissing(headers, "X-Amz-Date", iso8601Timestamp);
		if (AWS_S3_SERVICE.equals(service)) {
			addHeaderIfMissing (
				headers,
				"X-Amz-Content-Sha256",
				hex(hash(std.body != null ? std.body : "", std.charset.name()))
			);
		}
		if (sessionToken != null && !sessionToken.isEmpty()) {
			addHeaderIfMissing (
				headers,
				"X-Amz-Security-Token",
				sessionToken
			);
		}
	}

	private Map<String, String> getHeaders(StandardRequest std) {
		if (!std.parameters.containsKey("headers"))
			std.parameters.put("headers", new HashMap<String, String>());
		return (Map<String, String>) std.parameters.get("headers");
	}

	private void addHeaderIfMissing(Map<String, String> hs, String h, String v) {
		if (!hs.containsKey(h))
			hs.put(h, v);
	}

	private String signature(String secretAccessKey, String service, String region, String sessionToken) {
		try {
			String seed = "AWS4" + secretAccessKey;
			byte[] dateKey = hmac(seed.getBytes(StandardCharsets.UTF_8), iso8601Date);
			byte[] dateRegionKey = hmac(dateKey, region);
			byte[] dateRegionServiceKey = hmac(dateRegionKey, service);
			byte[] signingKey = hmac(dateRegionServiceKey, AWS4_REQUEST);
			String stringToSign = stringToSign(service, region, sessionToken);
			return hex(hmac(signingKey, stringToSign));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] hmac(byte[] key, String data) {
		return hmac(key, data.getBytes(StandardCharsets.UTF_8));
	}

	private byte[] hmac(byte[] key, byte[] data) {
		try {
			String algorithm = "HmacSHA256";
			Mac mac = Mac.getInstance(algorithm);
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, algorithm);
			mac.init(secretKeySpec);
			return mac.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String stringToSign(String service, String region, String sessionToken) {
		return String.join("\n",
				HMAC_ALGORITHM,
				iso8601Timestamp,
				scope(service, region),
				hex(hash(canonicalRequest(sessionToken)))
		);
	}

	private String scope(String service, String region) {
		return String.join("/",
				iso8601Date,
				region,
				service,
				AWS4_REQUEST
		);
	}

	private static String iso8601Timestamp() {
		return DateTimeFormatter
				.ofPattern("yyyyMMdd'T'HHmmss'Z'")
				.withZone(ZoneId.of("UTC"))
				.format(Instant.now());
	}

	private static String iso8601Date() {
		return DateTimeFormatter
				.BASIC_ISO_DATE
				.format(LocalDate.now());
	}

	private String canonicalRequest(String sessionToken) {
		StandardRequest std = (StandardRequest) origin;
		return String.join("\n",
				std.method,
				awsUrlEncode(std.url.getPath().isEmpty() ? "/" : std.url.getPath(), true),
				encodedSortedQueryString(std),
				sortedCanonicalHeaders(std),
				signedHeaders(),
				hex(hash(std.body != null ? std.body : ""))
		);
	}

	private byte[] hash(String input) {
		return hash(input, StandardCharsets.UTF_8.name());
	}

	private byte[] hash(String input, String charsetName) {
		try {
			MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
			return md.digest(input.getBytes(charsetName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String hex(byte[] input) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : input) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private String encodedSortedQueryString(StandardRequest std) {
		Map<String, String> queryParams = queryParams(std);
		Map<String, String> encoded = new HashMap<>();
		for (Map.Entry<String, String> entry : queryParams.entrySet()) {
			encoded.put(awsUrlEncode(entry.getKey(), false), awsUrlEncode(entry.getValue(), false));
		}

		return encoded.keySet().stream().sorted().map(k -> k + "=" + encoded.get(k)).collect(Collectors.joining("&"));
	}

	private Map<String, String> queryParams(StandardRequest std) {
		String query = std.url.getQuery();
		Map<String, String> params = new HashMap<>();
		if (query != null) {
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				String[] split = pair.split("=");
				String key = split[0];
				String value = (split.length > 1) ? split[1] : "";
				params.put(key, value);
			}
		}
		return params;
	}

	private String sortedCanonicalHeaders(StandardRequest std) {
		Map<String, String> headers = filteredLowerCaseHeaders(std);
		return headers.keySet().stream().sorted().map(k -> k + ":" + normalizeHeaderValue(headers.get(k)) + "\n").collect(Collectors.joining());
	}

	private String normalizeHeaderValue(String v) {
		return v.trim().replaceAll(" +", " ");
	}

	private String signedHeaders() {
		StandardRequest std = (StandardRequest) origin;
		Map<String, String> headers = filteredLowerCaseHeaders(std);
		return headers.keySet().stream().sorted().collect(Collectors.joining(";"));
	}

	private Map<String, String> filteredLowerCaseHeaders(StandardRequest std) {
		Map<String, String> lower = lowerCaseHeaders(std);
		Map<String, String> filtered = new HashMap<>();
		for (Map.Entry<String, String> entry : lower.entrySet()) {
			String k = entry.getKey();
			if (k.matches("authority|content-type|host|x-amz-.*")) {
				filtered.put(k, entry.getValue());
			}
		}
		if (filtered.containsKey("authority")) {
			filtered.remove("host");
		}
		return filtered;
	}

	private Map<String, String> lowerCaseHeaders(StandardRequest std) {
		Map<String, String> headers = getHeaders(std);
		Map<String, String> lower = new HashMap<>();
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			lower.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		return lower;
	}

	private String awsUrlEncode(String value, boolean path) {
		return awsUrlEncode(value, path, Constants.defaultCharset.name());
	}

	private String awsUrlEncode(String value, boolean path, String encoding) {
		if (value == null) return "";

		try {
			String encoded = URLEncoder.encode(value, encoding);
			Matcher matcher = ENCODED_CHARACTERS_PATTERN.matcher(encoded);
			StringBuffer buffer = new StringBuffer(encoded.length());

			while (matcher.find()) {
				String replacement = matcher.group(0);
				if ("+".equals(replacement)) {
					replacement = "%20";
				} else if ("*".equals(replacement)) {
					replacement = "%2A";
				} else if ("%7E".equals(replacement)) {
					replacement = "~";
				} else if (path && "%2F".equals(replacement)) {
					replacement = "/";
				}
				matcher.appendReplacement(buffer, replacement);
			}
			matcher.appendTail(buffer);
			return buffer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
