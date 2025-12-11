package it.grational.url;

public interface AuthURL extends URLConvertible {
    Boolean auth();
    String username();
    String password();
    String header();
}
