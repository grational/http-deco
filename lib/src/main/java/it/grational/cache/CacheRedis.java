package it.grational.cache;

import it.grational.compression.Compressor;
import it.grational.compression.NoCompression;
import redis.clients.jedis.commands.JedisCommands;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class CacheRedis implements CacheContainer {

    private final String cacheKey;
    private final JedisCommands jedis;
    private final Duration expireTime;
    private final Compressor compressor;

    public CacheRedis(JedisCommands jedis, String key) {
        this(jedis, key, null, new NoCompression());
    }

    public CacheRedis(JedisCommands jedis, String key, Duration expire) {
        this(jedis, key, expire, new NoCompression());
    }

    public CacheRedis(JedisCommands jedis, String key, Compressor compressor) {
        this(jedis, key, null, compressor);
    }

    public CacheRedis(JedisCommands jedis, String key, Duration expire, Compressor compressor) {
        this.jedis = Objects.requireNonNull(jedis);
        this.cacheKey = Objects.requireNonNull(key);
        this.expireTime = expire;
        this.compressor = (compressor != null) ? compressor : new NoCompression();
    }

    @Override
    public Boolean valid(Duration leaseTime) {
        return this.jedis.exists(this.cacheKey + ":content")
                && this.jedis.exists(this.cacheKey + ":timestamp")
                && this.newer(Objects.requireNonNull(leaseTime));
    }

    @Override
    public String content() {
        String result = this.jedis.get(this.cacheKey + ":content");
        if (result == null) {
            throw new IllegalStateException("No value for key '" + this.cacheKey + ":content'");
        }
        return this.compressor.uncompress(result);
    }

    @Override
    public void write(String input) {
        String content = this.compressor.compress(input);
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        writeKey("content", content);
        writeKey("timestamp", timestamp);
    }

    private void writeKey(String subkey, String value) {
        String key = this.cacheKey + ":" + subkey;
        this.jedis.set(key, value);
        if (this.expireTime != null) {
            this.jedis.expire(key, this.expireTime.getSeconds());
        }
    }

    @Override
    public void invalidate() {
        this.jedis.del(this.cacheKey + ":content");
        this.jedis.del(this.cacheKey + ":timestamp");
    }

    private Boolean newer(Duration leaseTime) {
        String timestampStr = this.jedis.get(this.cacheKey + ":timestamp");
        if (timestampStr == null) return false;
        long keyCreationTime = Long.parseLong(timestampStr);
        long currentTime = Instant.now().getEpochSecond();
        long howOldInSeconds = currentTime - keyCreationTime;
        return howOldInSeconds < leaseTime.getSeconds();
    }
}
