package it.grational.http.request;

import it.grational.http.response.HttpResponse;
import it.grational.http.response.JdkHttpResponse;
import it.grational.http.shared.Constants;
import it.grational.proxy.EnvProxy;
import it.grational.proxy.EnvVar;
import it.grational.proxy.NoProxy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Patch extends StandardRequest {

	public Patch(URL url) {
		this(url, null, new HashMap<>(), null);
	}

	public Patch (
		URL url,
		String body,
		Map<String, Object> params,
		java.net.Proxy proxy
	) {
		this.method = "PATCH";
		this.url = url;
		this.body = body;
		this.parameters = params != null ? params : new HashMap<>();
		this.proxy = proxy;
		if (this.parameters.containsKey("charset")) {
			this.charset = (Charset) this.parameters.get("charset");
		} else {
			this.charset = Constants.defaultCharset;
		}
	}

	public Patch(Map<String, Object> params) {
		this (
			(URL) params.get("url"),
			(String) params.get("body"),
			mergeParams(params),
			(java.net.Proxy) params.get("proxy")
		);
	}

	protected static Map<String, Object> mergeParams(Map<String, Object> params) {
		Map<String, Object> result = new HashMap<>();
		if (params.get("parameters") instanceof Map) {
			result.putAll((Map<String, Object>) params.get("parameters"));
		}
		
		if (params.containsKey("connectTimeout")) result.put("connectTimeout", params.get("connectTimeout"));
		if (params.containsKey("readTimeout")) result.put("readTimeout", params.get("readTimeout"));
		if (params.containsKey("headers")) result.put("headers", params.get("headers"));
		if (params.containsKey("cookies")) result.put("cookies", params.get("cookies"));
		if (params.containsKey("charset")) result.put("charset", params.get("charset"));
		
		return result;
	}

	@Override
	public HttpResponse connect() throws IOException {
		try {
			// Build HttpClient
			HttpClient.Builder clientBuilder = HttpClient.newBuilder();

			// 1. Proxy
			java.net.Proxy p = proxyFromEnvironment();
			if (p != java.net.Proxy.NO_PROXY) {
				// HttpClient uses ProxySelector
				clientBuilder.proxy (
					ProxySelector.of (
						p.address() instanceof java.net.InetSocketAddress
						? (java.net.InetSocketAddress) p.address()
						: null
					)
				);
			} else {
				clientBuilder.proxy(java.net.ProxySelector.of(null));
			}

			// 2. Redirects
			if (parameters.containsKey("followRedirects")) {
				Boolean follow = (Boolean) parameters.get("followRedirects");
				if (Boolean.TRUE.equals(follow)) {
					clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
				} else {
					clientBuilder.followRedirects(HttpClient.Redirect.NEVER);
				}
			} else {
				// Default to NORMAL to match HttpURLConnection common behavior
				clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
			}

			// 3. Connect Timeout
			if (parameters.get("connectTimeout") != null) {
				clientBuilder.connectTimeout (
					Duration.ofMillis (
						(Integer) parameters.get("connectTimeout")
					)
				);
			}

			// 4. CookieHandler
			if (CookieHandler.getDefault() != null) {
				clientBuilder.cookieHandler(CookieHandler.getDefault());
			}

			// 5. SSL (Insecure)
			if (Boolean.TRUE.equals(parameters.get("insecure"))) {
				clientBuilder.sslContext(insecureContext());
			}

			HttpClient client = clientBuilder.build();

			// Build HttpRequest
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
					.uri(this.url.toURI())
					.method (
						"PATCH",
						HttpRequest.BodyPublishers.ofString (
							this.body != null ? this.body : "", 
							this.charset != null ? this.charset : Constants.defaultCharset
						)
					);

			// 6. Headers
			Map<String, String> headers = getHeaders();
			if (this.url.getUserInfo() != null) {
				headers.putAll(this.addBasicAuth(this.url.getUserInfo()));
			}
			appendContentTypeCharset(headers);

			for (Map.Entry<String, String> entry : headers.entrySet()) {
				requestBuilder.header(entry.getKey(), entry.getValue());
			}

			// 7. Cookies (in headers)
			if (parameters.get("cookies") != null) {
				requestBuilder.header (
					"Cookie",
					assembleCookies((Map) parameters.get("cookies"))
				);
			}
			
			// 8. Read Timeout (Request timeout)
			if (parameters.get("readTimeout") != null) {
				requestBuilder.timeout (
					Duration.ofMillis (
						(Integer) parameters.get("readTimeout")
					)
				);
			}

			// Send
			java.net.http.HttpResponse<byte[]> response = client.send (
				requestBuilder.build(),
				java.net.http.HttpResponse.BodyHandlers.ofByteArray()
			);

			return new JdkHttpResponse(response, this.url);

		} catch (Exception e) {
			if (e instanceof IOException) throw (IOException) e;
			throw new IOException(e);
		}
	}

	// Helper to replicate StandardRequest logic locally since some methods are private or instance-dependent
	private java.net.Proxy proxyFromEnvironment() {
		if (this.proxy != null) {
			return this.proxy;
		} else if (new NoProxy().exclude(this.url)) {
			return java.net.Proxy.NO_PROXY;
		} else {
			return new EnvProxy (
				EnvVar.byURL(this.url).value()
			).proxy();
		}
	}

	private SSLContext insecureContext() throws Exception {
		TrustManager[] trustAll = new TrustManager[] {
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() { return null; }
				public void checkClientTrusted(X509Certificate[] certs, String authType) {}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {}
			}
		};
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(null, trustAll, new SecureRandom());
		return context;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> getHeaders() {
		if (!parameters.containsKey("headers")) {
			parameters.put("headers", new HashMap<String, String>());
		}
		return (Map<String, String>) parameters.get("headers");
	}
}
