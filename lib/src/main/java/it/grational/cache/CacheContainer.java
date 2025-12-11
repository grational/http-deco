package it.grational.cache;

import java.time.Duration;

public interface CacheContainer {
    Boolean valid(Duration leaseTime);
    String content();
    void write(String input);
    void invalidate();
}
