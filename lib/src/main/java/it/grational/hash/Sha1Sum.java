package it.grational.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Sha1Sum implements HashFunction {

    private final String input;

    public Sha1Sum(String input) {
        this.input = input;
    }

    @Override
    public String digest() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(this.input.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1 algorithm not available", e);
        }
    }
}
