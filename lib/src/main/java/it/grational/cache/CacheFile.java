package it.grational.cache;

import it.grational.compression.Compressor;
import it.grational.compression.NoCompression;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Date;

public final class CacheFile implements CacheContainer {

    private final File file;
    private final Compressor compressor;

    public CacheFile() {
        this(createTempFile(), new NoCompression());
    }

    public CacheFile(File file) {
        this(file, new NoCompression());
    }

    public CacheFile(Compressor compressor) {
        this(createTempFile(), compressor);
    }

    public CacheFile(File file, Compressor compressor) {
        this.file = file;
        this.compressor = compressor;
        if (!this.file.getParentFile().isDirectory()) {
            throw new IllegalArgumentException("The parent directory of '" + file + " does not exists!");
        }
    }

    private static File createTempFile() {
        try {
            return File.createTempFile("temp", ".cache");
        } catch (IOException e) {
            throw new RuntimeException("Could not create temp file", e);
        }
    }

    @Override
    public Boolean valid(Duration leaseTime) {
        return this.file.isFile() && this.isNotEmpty() && this.newer(leaseTime);
    }

    private Boolean isNotEmpty() {
        return this.file.length() > 0;
    }

    @Override
    public String content() {
        try {
            String fileContent = Files.readString(this.file.toPath(), StandardCharsets.UTF_8);
            return this.compressor.uncompress(fileContent);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file content", e);
        }
    }

    @Override
    public void write(String input) {
        this.write(input, StandardCharsets.UTF_8);
    }

    public void write(String input, String charsetName) {
        this.write(input, Charset.forName(charsetName));
    }

    public void write(String input, Charset charset) {
        try {
            String compressed = this.compressor.compress(input);
            Files.writeString(this.file.toPath(), compressed, charset);
        } catch (IOException e) {
            throw new RuntimeException("Could not write to file", e);
        }
    }

    @Override
    public void invalidate() {
        this.file.delete();
    }

    private Boolean newer(Duration leaseTime) {
        long currentEpoch = new Date().getTime();
        long lastModified = this.file.lastModified();
        return (currentEpoch - lastModified) < leaseTime.toMillis();
    }
}
