package it.grational.http.header;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class BasicAuthentication implements Header {
    protected String username;
    protected String password;

    public abstract String name();

    @Override
    public String value() {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }

    @Override
    public String toString() {
        return this.name() + ": " + this.value();
    }
}
