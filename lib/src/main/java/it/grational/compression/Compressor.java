package it.grational.compression;

public interface Compressor {
    String compress(String input);
    String uncompress(String compressedInput);
}
