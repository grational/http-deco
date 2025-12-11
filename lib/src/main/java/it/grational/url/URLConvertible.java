package it.grational.url;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public interface URLConvertible {
    URL toURL() throws MalformedURLException;
    URI toURI() throws URISyntaxException, MalformedURLException;
}
