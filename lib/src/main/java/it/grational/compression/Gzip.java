package it.grational.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip implements Compressor {

    @Override
    public String compress(String input) {
        try {
            ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
            try (GZIPOutputStream zipStream = new GZIPOutputStream(targetStream)) {
                zipStream.write(input.getBytes(StandardCharsets.UTF_8));
            }
            byte[] zippedBytes = targetStream.toByteArray();
            return Base64.getEncoder().encodeToString(zippedBytes);
        } catch (IOException e) {
            throw new RuntimeException("Compression failed", e);
        }
    }

    @Override
    public String uncompress(String compressedInput) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(compressedInput);
            try (GZIPInputStream inflaterStream = new GZIPInputStream(new ByteArrayInputStream(decodedBytes));
                 InputStreamReader reader = new InputStreamReader(inflaterStream, StandardCharsets.UTF_8)) {
                
                StringBuilder result = new StringBuilder();
                char[] buffer = new char[1024];
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    result.append(buffer, 0, n);
                }
                return result.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Decompression failed", e);
        }
    }
}
