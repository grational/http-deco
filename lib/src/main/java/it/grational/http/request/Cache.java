package it.grational.http.request;

import it.grational.cache.CacheContainer;
import it.grational.http.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Cache extends FunctionalRequest {
    private final CacheContainer cacheContainer;
    private final Duration leaseTime;
    private final Runnable missOperation;
    private final Boolean missOpBefore;
    private final Boolean cacheErrors;
    private final String separator = "\n";

    public Cache(HttpRequest org, CacheContainer cc, Duration lt) {
        this(org, cc, lt, () -> {}, false, false);
    }

    public Cache(HttpRequest org, CacheContainer cc, Duration lt, Runnable mos) {
        this(org, cc, lt, mos, false, false);
    }

    public Cache(HttpRequest org, CacheContainer cc, Duration lt, Runnable mos, Boolean mosBefore) {
        this(org, cc, lt, mos, mosBefore, false);
    }

    public Cache(HttpRequest org, CacheContainer cc, Duration lt, Runnable mos, Boolean mosBefore, Boolean ce) {
        super(org);
        this.cacheContainer = cc;
        this.leaseTime = lt;
        this.missOperation = mos;
        this.missOpBefore = mosBefore;
        this.cacheErrors = ce;
    }

    @Override
    public HttpResponse connect() throws java.io.IOException {
        HttpResponse response;
        if (this.cacheContainer.valid(this.leaseTime)) {
            response = this.cachedResponse();
        } else {
            if (this.missOpBefore)
                this.missOperation.run();

            response = this.origin.connect();

            if (!response.error() || this.cacheErrors) {
                String joinedResponse = this.joinedResponse(response);
                this.cacheContainer.write(joinedResponse);
                response = this.cachedResponse();
            }

            if (!this.missOpBefore)
                this.missOperation.run();
        }
        return response;
    }

    private HttpResponse cachedResponse() {
        List<String> cacheLines = this.cacheLines();
        return new HttpResponse.CustomResponse(
            this.cachedCode(cacheLines),
            this.cachedContent(cacheLines)
        );
    }

    private List<String> cacheLines() {
        String content = this.cacheContainer.content();
        return Arrays.asList(content.split("\\r?\\n"));
    }

    private Integer cachedCode(List<String> lines) {
        return Integer.parseInt(lines.get(0));
    }

    private ByteArrayInputStream cachedContent(List<String> lines) {
        String content = lines.stream().skip(1).collect(Collectors.joining(this.separator));
        return new ByteArrayInputStream(content.getBytes());
    }

    private String joinedResponse(HttpResponse response) {
        return response.code() + this.separator + response.text();
    }
}
