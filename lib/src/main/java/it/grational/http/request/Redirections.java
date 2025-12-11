package it.grational.http.request;

import it.grational.http.response.HttpResponse;
import it.grational.http.shared.Constants;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Redirections extends FunctionalRequest {

    private final Integer max;

    public Redirections(HttpRequest org) {
        this(org, 3);
    }

    public Redirections(HttpRequest org, Integer mx) {
        super(org);
        this.max = mx;
    }

    @Override
    public HttpResponse connect() throws java.io.IOException {
        disableIntraProtocolRedirects();
        HttpResponse response = null;
        for (int time = 0; time <= max; time++) {
            response = this.origin.connect();

            if (!redirect(response)) {
                break;
            }

            setNewDestination(response);
        }
        return response;
    }

    private void disableIntraProtocolRedirects() {
        // origin is mutable? functional request wrappers assume origin might be immutable?
        // Groovy code: this.origin = this.origin.withParameter(...)
        // But origin is final in FunctionalRequest.
        // Wait, FunctionalRequest has `protected final HttpRequest origin`.
        // I cannot reassign `this.origin`.
        // Groovy code: `this.origin = this.origin.withParameter(...)` works because Groovy ignores final?
        // Or because `origin` was not final in Groovy `FunctionalRequest`.
        // In Java, `origin` is final.
        // I need to handle this.
        // FunctionalRequest implies wrapping.
        // If I need to change the origin, I should return a NEW FunctionalRequest?
        // But `connect()` calls `this.origin.connect()`.
        
        // If `FunctionalRequest` is immutable-ish (wrapper), `withParameter` returns `HttpRequest`.
        // `withParameter` calls `origin.withParameter`.
        // If `StandardRequest` is mutable (it returns `this`), then `origin.withParameter` modifies `origin` and returns `origin`.
        // So `this.origin` refers to the same object.
        
        // But `Redirections` logic: `this.origin = this.origin.withURL(newDestination)`.
        // `withURL` in `StandardRequest` modifies `this.url` and returns `this`.
        // So reassigning `this.origin` is redundant IF `origin` returns `this`.
        // But `FunctionalRequest` wrappers delegate.
        // If `origin` is another `FunctionalRequest`, it delegates.
        
        // Problem: `origin` is final.
        // I should remove `final` from `origin` in `FunctionalRequest.java`.
        
        this.origin.withParameter("followRedirects", false);
    }

    private Boolean redirect(HttpResponse response) {
        int code = response.code();
        return code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP;
    }

    private void setNewDestination(HttpResponse response) {
        try {
            String location = response.header("Location");
            location = URLDecoder.decode(
                    location,
                    charsetFromContentType(response.header("Content-Type")).name()
            );
            URL newDestination = new URL(this.origin.getUrl(), location);
            // this.origin = this.withURL(newDestination); // FunctionalRequest.withURL is protected and delegates.
            // But we want to update the origin's URL.
            // If origin is StandardRequest, withURL modifies it in place and returns it.
            // If origin is FunctionalRequest, withURL delegates to its origin.
            // So calling withURL on this.origin works.
            
            // However, withURL returns HttpRequest.
            // We need to reassign to this.origin?
            // If it modifies in place, no need to reassign unless wrapper changes.
            // StandardRequest modifies in place.
            
            // But FunctionalRequest.withURL is protected.
            // We are in same package.
            this.withURL(newDestination);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Charset charsetFromContentType(String header) {
        if (header != null) {
            Matcher m = Pattern.compile("(?<=charset=).*").matcher(header);
            if (m.find()) {
                try {
                    return Charset.forName(m.group());
                } catch (Exception e) {}
            }
        }
        return Constants.defaultCharset;
    }
}
