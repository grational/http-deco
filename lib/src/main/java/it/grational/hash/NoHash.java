package it.grational.hash;

public class NoHash implements HashFunction {

    private final String input;

    public NoHash(String input) {
        this.input = input;
    }

    @Override
    public String digest() {
        return this.input;
    }
}
