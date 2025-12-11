package it.grational.http.request;

import it.grational.http.header.Authorization;
import it.grational.http.response.HttpResponse;
import it.grational.http.response.Response;
import it.grational.http.shared.Constants;
import it.grational.proxy.EnvProxy;
import it.grational.proxy.EnvVar;
import it.grational.proxy.NoProxy;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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

		if (Boolean.TRUE.equals(parameters.get("insecure"))) {
			insecure();
		}

		Proxy p = proxyFromEnvironment();
		URLConnection connection = this.url.openConnection(p);

		if (connection instanceof HttpURLConnection) {
			((HttpURLConnection) connection).setRequestMethod(this.method);

			if (parameters.containsKey("followRedirects")) {
				Object val = parameters.get("followRedirects");
				if (val != null) {
					((HttpURLConnection) connection).setInstanceFollowRedirects((Boolean) val);
				}
			}
		}

		if (parameters.get("connectTimeout") != null) {
			connection.setConnectTimeout((Integer) parameters.get("connectTimeout"));
		}
		if (parameters.get("readTimeout") != null) {
			connection.setReadTimeout((Integer) parameters.get("readTimeout"));
		}
		if (parameters.get("allowUserInteraction") != null) {
			connection.setAllowUserInteraction((Boolean) parameters.get("allowUserInteraction"));
		}
		if (parameters.get("useCaches") != null) {
			connection.setUseCaches((Boolean) parameters.get("useCaches"));
		}

		Map<String, String> headers = getHeaders();
		if (this.url.getUserInfo() != null) {
			headers.putAll(this.addBasicAuth(this.url.getUserInfo()));
		}

		appendContentTypeCharset(headers);

		for (Map.Entry<String, String> entry : headers.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}

		if (parameters.get("cookies") != null) {
			connection.setRequestProperty("Cookie", assembleCookies((Map) parameters.get("cookies")));
		}

		if (this.body != null) {
			connection.setDoOutput(true);
			try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), charset)) {
				writer.write(this.body);
			}
		} else {
			connection.connect();
		}

		Integer responseCode = null;
		if (connection instanceof HttpURLConnection) {
			responseCode = ((HttpURLConnection) connection).getResponseCode();
		}

		return new Response(responseCode, connection);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getHeaders() {
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

	private Proxy proxyFromEnvironment() {
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

	private void insecure() {
		disableServerNameCheck();
		disableCertificateCheck();
		disableHostnameCheck();
	}

	private void disableServerNameCheck() {
		System.setProperty("jsse.enableSNIExtension", "false");
	}

	private void disableCertificateCheck() {
		try {
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

			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, trustAll, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void disableHostnameCheck() {
		HostnameVerifier alwaysValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(alwaysValid);
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
