package it.grational.http.request;

import it.grational.http.response.HttpResponse;
import java.util.function.BiConsumer;

public class Retry extends FunctionalRequest {

    private final Integer retries;
    private final BiConsumer<Integer, Integer> retryOperation;

    public Retry(HttpRequest org) {
        this(org, 3);
    }

    public Retry(HttpRequest org, Integer retries) {
        this(org, retries, (curr, tot) -> {
            try {
                Thread.sleep(1000L * curr);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public Retry(HttpRequest org, Integer retries, BiConsumer<Integer, Integer> rop) {
        super(org);
        this.retries = retries;
        this.retryOperation = rop;
    }

    @Override
    public HttpResponse connect() throws java.io.IOException {
        for (int time = 1; time <= retries; time++) {
            HttpResponse response = null;
            try {
                response = origin.connect();
            } catch (RuntimeException e) {
                 if (!retry(time)) {
                     raiseException(e);
                 }
                 continue;
            } catch (Exception e) {
                if (!retry(time))
                    raiseException(e);
                continue;
            }
            
            if (ok(response))
                return response;

            if (shouldNotRetry(response))
                return response;

            if (retry(time))
                continue;

            raiseException(response.exception());
        }
        throw new RuntimeException("Retry loop finished without result");
    }

    private Boolean ok(HttpResponse response) {
        return !response.error();
    }

    private Boolean shouldNotRetry(HttpResponse response) {
        return response.code() != null && response.code() >= 400 && response.code() < 500;
    }

    private Boolean retry(Integer time) {
        boolean retry = (time < retries);
        if (retry)
            retryOperation.accept(time, retries);
        return retry;
    }

    private void raiseException(Throwable e) {
        throw new RuntimeException(
            "Retry limit (" + retries + ") exceeded for connection '" + origin + "' with exception: '" + e + "'",
            e
        );
    }
}
