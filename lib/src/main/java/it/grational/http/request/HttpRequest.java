package it.grational.http.request;

import it.grational.http.response.HttpResponse;
import java.io.IOException;
import java.net.URL;

public interface HttpRequest {
    HttpResponse connect() throws IOException;

    HttpRequest withHeader(String key, String value);

    HttpRequest withCookie(String key, String value);

    HttpRequest withParameter(String key, Object value);

    HttpRequest withBasicAuth(String username, String password);
    
    java.net.URL getUrl();
}
