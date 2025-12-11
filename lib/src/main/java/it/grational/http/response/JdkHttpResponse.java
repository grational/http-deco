package it.grational.http.response;

import it.grational.http.shared.Constants;
import groovy.json.JsonSlurper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.net.HttpCookie;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

public class JdkHttpResponse extends HttpResponse.StandardResponse {
	private final java.net.http.HttpResponse<byte[]> response;
	private final URL url;
	private static final JsonSlurper js = new JsonSlurper();

	public JdkHttpResponse (
		java.net.http.HttpResponse<byte[]> response,
		URL url
	) {
		this.response = response;
		this.code = response.statusCode();
		this.url = url;
	}

	@Override
	public URL url() {
		return this.url;
	}

	@Override
	public Boolean error() {
		return this.code >= 400;
	}

	@Override
	public Throwable exception() {
		if (this.exception != null) return this.exception;
		
		if (error()) {
			return new java.io.IOException("HTTP Error " + code);
		}
		throw new IllegalStateException("No exception were been thrown at this moment!");
	}

	@Override
	public byte[] bytes(Boolean exceptions) {
		if (Boolean.TRUE.equals(exceptions) && error()) {
			this.exception = new java.io.IOException (
				"Server returned HTTP response code: " + code + " for URL: " + url
			);
			throw new RuntimeException(this.exception);
		}
		return response.body();
	}

	@Override
	public String text(Charset charset, Boolean exceptions) {
		try {
			byte[] b = bytes(exceptions);
			return new String(b, charset);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String header(String name) {
		return response.headers().firstValue(name).orElse(null);
	}

	@Override
	public HttpCookie cookie(String name) {
		java.net.CookieHandler handler = java.net.CookieHandler.getDefault();
		if (handler instanceof java.net.CookieManager) {
			List<HttpCookie> cookies = ((java.net.CookieManager) handler).getCookieStore().getCookies();
			for (HttpCookie c : cookies) {
				if (c.getName().equals(name)) return c;
			}
		}
		return null;
	}

	@Override
	public <T> T jsonObject (
		Class<T> type,
		Charset charset,
		Boolean exceptions
	) {
		String text = this.text(charset, exceptions);
		if (text == null || text.isEmpty()) return null;
		Object parsed = js.parseText(text);
		return DefaultGroovyMethods.asType(parsed, type);
	}
}