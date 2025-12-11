package it.grational.http.request;

import it.grational.http.header.Authorization;
import it.grational.http.response.HttpResponse;
import it.grational.http.response.JdkHttpResponse;
import it.grational.http.shared.Constants;
import it.grational.proxy.EnvProxy;
import it.grational.proxy.EnvVar;
import it.grational.proxy.NoProxy;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class StandardRequest implements HttpRequest {

	protected String method;
	protected URL url;
	protected Charset charset = Constants.defaultCharset;
	protected String body;
	protected Map<String, Object> parameters = new HashMap<>();
	protected Proxy proxy;

	private static final Pattern URL_PATTERN = Pattern.compile(
			"(?<protocol>[^:]{3,})://(?:[^:]+:(?:[^@]*@)+)?(?<residual>.*)"
	);

	@Override
	public HttpResponse connect() throws IOException {
		if (this.url == null) {
			throw new IllegalStateException("URL is not set");
		}
		enableCookieManagementIfNeeded();

		try {
			// Build HttpClient
			HttpClient.Builder clientBuilder = HttpClient.newBuilder();

			// Proxy
			Proxy p = proxyFromEnvironment();
			if (p != null && p != Proxy.NO_PROXY && p.address() != null) {
				clientBuilder.proxy(ProxySelector.of((InetSocketAddress) p.address()));
			} else {
				clientBuilder.proxy(ProxySelector.of(null));
			}

			// Redirects
			if (parameters.containsKey("followRedirects")) {
				Boolean follow = (Boolean) parameters.get("followRedirects");
				if (Boolean.TRUE.equals(follow)) {
					clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
				} else {
					clientBuilder.followRedirects(HttpClient.Redirect.NEVER);
				}
			} else {
				// Default: follow redirects (matches HttpURLConnection default behavior)
				clientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
			}

			// Connect Timeout
			if (parameters.get("connectTimeout") != null) {
				clientBuilder.connectTimeout(
					Duration.ofMillis((Integer) parameters.get("connectTimeout"))
				);
			}

			// Cookie Handler
			if (CookieHandler.getDefault() != null) {
				clientBuilder.cookieHandler(CookieHandler.getDefault());
			}

			// SSL (Insecure)
			if (Boolean.TRUE.equals(parameters.get("insecure"))) {
				SSLContext insecureContext = insecureSSLContext();
				clientBuilder.sslContext(insecureContext);
				// Disable hostname verification - empty string disables endpoint identification
				SSLParameters sslParams = insecureContext.getDefaultSSLParameters();
				sslParams.setEndpointIdentificationAlgorithm("");
				clientBuilder.sslParameters(sslParams);
			}

			HttpClient client = clientBuilder.build();

			// Build HttpRequest
			// Strip userInfo from URL for URI conversion (it's added as Authorization header)
			URL requestUrl = this.url;
			if (this.url.getUserInfo() != null) {
				String urlStr = this.url.toString();
				String userInfo = this.url.getUserInfo();
				urlStr = urlStr.replace(userInfo + "@", "");
				requestUrl = new URL(urlStr);
			}

			java.net.http.HttpRequest.Builder requestBuilder = java.net.http.HttpRequest.newBuilder()
				.uri(requestUrl.toURI())
				.method(
					this.method,
					this.body != null
						? java.net.http.HttpRequest.BodyPublishers.ofString(this.body, this.charset)
						: java.net.http.HttpRequest.BodyPublishers.noBody()
				);

			// Headers
			Map<String, String> headers = getHeaders();
			if (this.url.getUserInfo() != null) {
				headers.putAll(this.addBasicAuth(this.url.getUserInfo()));
			}
			appendContentTypeCharset(headers);

			// HttpClient restricts certain headers (Host, Connection, etc.) - skip them
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				String headerName = entry.getKey();
				if (!isRestrictedHeader(headerName)) {
					requestBuilder.header(headerName, entry.getValue());
				}
			}

			// Cookies
			if (parameters.get("cookies") != null) {
				requestBuilder.header("Cookie", assembleCookies((Map) parameters.get("cookies")));
			}

			// Read Timeout
			if (parameters.get("readTimeout") != null) {
				requestBuilder.timeout(
					Duration.ofMillis((Integer) parameters.get("readTimeout"))
				);
			}

			// Send request
			java.net.http.HttpResponse<byte[]> response = client.send(
				requestBuilder.build(),
				java.net.http.HttpResponse.BodyHandlers.ofByteArray()
			);

			return new JdkHttpResponse(response, this.url);

		} catch (Exception e) {
			if (e instanceof IOException) throw (IOException) e;
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, String> getHeaders() {
		if (!parameters.containsKey("headers")) {
			parameters.put("headers", new HashMap<String, String>());
		}
		return (Map<String, String>) parameters.get("headers");
	}

	protected void appendContentTypeCharset(Map<String, String> headers) {
		String contentType = headers.get("Content-Type");
		if (contentType == null) return;
		if (contentType.contains("charset")) return;
		headers.put("Content-Type", contentType + "; charset=" + this.charset.name().toLowerCase());
	}

	private void enableCookieManagementIfNeeded() {
		if (CookieHandler.getDefault() == null) {
			CookieManager cm = new CookieManager();
			cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(cm);
		}
	}

	protected Proxy proxyFromEnvironment() {
		if (this.proxy != null) {
			return this.proxy;
		} else if (new NoProxy().exclude(this.url)) {
			return Proxy.NO_PROXY;
		} else {
			return new EnvProxy(
					EnvVar.byURL(this.url).value()
			).proxy();
		}
	}

	protected SSLContext insecureSSLContext() throws Exception {
		TrustManager[] trustAll = new TrustManager[]{
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				}
		};

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, trustAll, new SecureRandom());
		return context;
	}

	protected Map<String, String> addBasicAuth(String userInfo) {
		String[] parts = userInfo.split(":");
		String user = parts[0];
		String pass = (parts.length > 1) ? parts[1] : "";

		Map<String, Object> authParams = new HashMap<>();
		authParams.put("username", safeUrlDecode(user));
		authParams.put("password", safeUrlDecode(pass));

		Authorization auth = new Authorization(authParams);
		Map<String, String> result = new HashMap<>();
		result.put(auth.name(), auth.value());
		return result;
	}

	private String safeUrlDecode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (Exception e) {
			return value;
		}
	}

	protected String assembleCookies(Map<?, ?> cookies) {
		return cookies.entrySet().stream()
				.map(e -> e.getKey() + "=" + e.getValue() + ";")
				.collect(Collectors.joining(" "));
	}

	private boolean isRestrictedHeader(String headerName) {
		// HttpClient restricts these headers - they are set automatically
		String lowerName = headerName.toLowerCase();
		return lowerName.equals("host") ||
		       lowerName.equals("connection") ||
		       lowerName.equals("content-length") ||
		       lowerName.equals("expect") ||
		       lowerName.equals("upgrade");
	}

	@Override
	public HttpRequest withHeader(String key, String value) {
		getHeaders().put(key, value);
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public HttpRequest withCookie(String key, String value) {
		if (!parameters.containsKey("cookies")) {
			parameters.put("cookies", new HashMap<String, String>());
		}
		((Map<String, String>) parameters.get("cookies")).put(key, value);
		return this;
	}

	@Override
	public HttpRequest withParameter(String key, Object value) {
		parameters.put(key, value);
		return this;
	}

	protected HttpRequest withURL(URL url) {
		this.url = url;
		return this;
	}

	@Override
	public URL getUrl() {
		return this.url;
	}

	@Override
	public HttpRequest withBasicAuth(String username, String password) {
		Matcher m = URL_PATTERN.matcher(this.url.toString());
		if (m.find()) {
			 try {
				 this.url = new URL(m.group("protocol") + "://" + username + ":" + password + "@" + m.group("residual"));
			 } catch (MalformedURLException e) {
				 throw new RuntimeException(e);
			 }
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		r.append("method: ").append(this.method);
		r.append("\nurl: ").append(this.url);
		if (this.body != null)
			r.append("\nbody: ").append(this.body);
		if (!this.parameters.isEmpty())
			r.append("\nparameters: ").append(this.parameters);
		r.append("\nproxy: ").append(this.proxy);
		return r.toString();
	}
}
