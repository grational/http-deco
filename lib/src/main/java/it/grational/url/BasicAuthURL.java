package it.grational.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BasicAuthURL implements AuthURL {
    private static final Pattern CHECKER = Pattern.compile(
            "(https?)://(?:([^:]+):([^@]+)@)?([^:/]+)(?::([1-9][0-9]{0,4}))?(?:/(.*))?"
    );

    private final String protocol;
    private final String username;
    private final String password;
    private final String host;
    private final Integer port;
    private final String path;

    public BasicAuthURL(String url) {
        if (url == null) {
            throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid URL string 'null'");
        }
        Matcher m = CHECKER.matcher(url);
        if (!m.matches()) {
            throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid URL string '" + url + "'");
        } else {
            this.protocol = m.group(1);
            this.username = m.group(2);
            this.password = m.group(3);
            this.host = m.group(4);
            String portStr = m.group(5);
            this.port = (portStr != null) ? Integer.parseInt(portStr) : null;
            this.path = m.group(6);
        }
    }

    public Boolean secure() {
        return "https".equals(this.protocol);
    }

    public String host() {
        return this.host;
    }

    public Integer port() {
        if (this.port != null) {
            return this.port;
        }
        return this.secure() ? 443 : 80;
    }

    public String path() {
        return this.path;
    }

    @Override
    public Boolean auth() {
        return (this.username != null && this.password != null);
    }

    @Override
    public String username() {
        if (!this.auth()) {
            throw new UnsupportedOperationException("Cannot return the URL username, the URL string doesn't contain any auth credentials");
        }
        return this.username;
    }

    @Override
    public String password() {
        if (!this.auth()) {
            throw new UnsupportedOperationException("Cannot return the URL password, the URL string doesn't contain any auth credentials");
        }
        return this.password;
    }

    @Override
    public String header() {
        String authString = this.username + ":" + this.password;
        String encodedBasicAuthentication = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedBasicAuthentication;
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return new URL(this.noAuthForm());
    }

    @Override
    public URI toURI() throws URISyntaxException, MalformedURLException {
        return new URI(this.noAuthForm());
    }

    private String noAuthForm() {
        if (this.port != null) {
            return String.format("%s://%s:%d/%s", this.protocol, this.host, this.port, (this.path != null ? this.path : ""));
        } else {
            return String.format("%s://%s/%s", this.protocol, this.host, (this.path != null ? this.path : ""));
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.protocol).append("://");
        if (this.auth()) {
            result.append(this.username).append(":").append(this.password).append("@");
        }
        result.append(this.host);
        if (this.port != null) {
            result.append(":").append(this.port);
        }
        result.append("/");
        if (this.path != null) {
            result.append(this.path);
        }

        return result.toString();
    }
}
