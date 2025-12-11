package it.grational.compression;

public class NoCompression implements Compressor {

    @Override
    public String compress(String input) {
        return input;
    }

    @Override
    public String uncompress(String input) {
        return input;
    }
}
