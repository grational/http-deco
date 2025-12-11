package it.grational.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ProxyAuthentication {

    public static void enable(String username, String password) {
        // enable proxy basic auth for https - needed after j8
        // otherwise you will obtain a 417 http error
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        
        // set the jdk net class that handles proxy basic authentication
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });
    }
}
