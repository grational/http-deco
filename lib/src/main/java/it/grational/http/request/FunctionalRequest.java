package it.grational.http.request;

import java.io.IOException;
import java.net.URL;

public abstract class FunctionalRequest implements HttpRequest {

    protected HttpRequest origin;

    public FunctionalRequest(HttpRequest origin) {
        this.origin = origin;
    }
    
    @Override
    public URL getUrl() {
        return this.origin.getUrl();
    }

    @Override
    public HttpRequest withHeader(String key, String value) {
        return this.origin.withHeader(key, value);
    }

    @Override
    public HttpRequest withCookie(String key, String value) {
        return this.origin.withCookie(key, value);
    }

    @Override
    public HttpRequest withParameter(String key, Object value) {
        return this.origin.withParameter(key, value);
    }

    @Override
    public HttpRequest withBasicAuth(String username, String password) {
        return this.origin.withBasicAuth(username, password);
    }

    protected HttpRequest withURL(URL url) {
        if (origin instanceof StandardRequest) {
            return ((StandardRequest) origin).withURL(url);
        }
        if (origin instanceof FunctionalRequest) {
            return ((FunctionalRequest) origin).withURL(url);
        }
        throw new UnsupportedOperationException("Origin request type '" + origin.getClass().getName() + "' does not support withURL");
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }
}
