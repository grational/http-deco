package it.grational.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class NoNullURL implements URLConvertible {
    private final URL input;

    public NoNullURL(URL input) {
        this.input = input;
    }

    @Override
    public URL toURL() {
        if (input == null) {
            throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Null URL");
        }
        return this.input;
    }

    @Override
    public URI toURI() throws URISyntaxException, MalformedURLException {
        return this.toURL().toURI();
    }

    @Override
    public String toString() {
        return this.input.toString();
    }
}
