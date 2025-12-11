package it.grational.http.response;

import groovy.json.JsonSlurper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

public class Response extends HttpResponse.StandardResponse {
    private final URLConnection connection;
    private static final JsonSlurper js = new JsonSlurper();
    
    private String cachedText;
    private byte[] cachedBytes;

    public Response(Integer code, URLConnection connection) {
        this.code = code;
        this.connection = connection;
    }

    @Override
    public Boolean error() {
        return this.error || (this.code != null && this.code >= HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Override
    public Throwable exception() {
        if (this.exception != null) {
            return this.exception;
        } else if (this.error()) {
            try {
                this.text(false);
                return this.exception;
            } catch (Exception te) {
                this.exception = new IOException(
                    "HTTP Error " + (this.code != null ? this.code : "Unknown") + ": Unable to read error details",
                    te
                );
                return this.exception;
            }
        } else {
            throw new IllegalStateException("No exception were been thrown at this moment!");
        }
    }

    @Override
    public URL url() {
        return this.connection.getURL();
    }

    @Override
    public synchronized String text(Charset charset, Boolean exceptions) {
        if (cachedText != null) return cachedText;
        try {
            byte[] b = bytes(exceptions);
            if (b == null) return null;
            cachedText = new String(b, charset);
            return cachedText;
        } catch (Exception e) {
             if (e instanceof RuntimeException) throw (RuntimeException)e;
             throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized byte[] bytes(Boolean exceptions) {
        if (cachedBytes != null) return cachedBytes;
        try {
            InputStream is = openInput(exceptions);
            if (is == null) return new byte[0];
            cachedBytes = is.readAllBytes();
            return cachedBytes;
        } catch (IOException e) {
             throw new RuntimeException(e);
        }
    }

    private InputStream openInput(Boolean exceptions) {
        if (Boolean.TRUE.equals(exceptions)) {
             try {
                 return this.connection.getInputStream();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
        } else {
             try {
                 return this.connection.getInputStream();
             } catch (IOException e) {
                 this.error = true;
                 this.exception = e;
                 if (this.connection instanceof HttpURLConnection) {
                     return ((HttpURLConnection) this.connection).getErrorStream();
                 }
                 return null;
             }
        }
    }

    @Override
    public String header(String name) {
        return this.connection.getHeaderField(name);
    }

    @Override
    public HttpCookie cookie(String name) {
        CookieHandler handler = CookieHandler.getDefault();
        if (handler instanceof CookieManager) {
             List<HttpCookie> cookies = ((CookieManager) handler).getCookieStore().getCookies();
             for (HttpCookie c : cookies) {
                 if (c.getName().equals(name)) return c;
             }
        }
        return null;
    }

    @Override
    public <T> T jsonObject(Class<T> type, Charset charset, Boolean exceptions) {
        String text = this.text(charset, exceptions);
        if (text == null || text.isEmpty()) return null;
        Object parsed = js.parseText(text);
        return DefaultGroovyMethods.asType(parsed, type);
    }

    @Override
    public String toString() {
        return "code: " + this.code + "\n" +
               "connection: " + this.connection + "\n" +
               "error: " + this.error + "\n" +
               "exception: " + this.exception;
    }
}
