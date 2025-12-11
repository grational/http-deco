package it.grational.http.response;

import it.grational.http.shared.Constants;
import groovy.json.JsonSlurper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface HttpResponse {
    URL url();
    Integer code();
    Boolean error();
    Throwable exception();
    byte[] bytes();
    byte[] bytes(Boolean exceptions);
    String text();
    String text(Charset charset);
    String text(Boolean exceptions);
    String text(Charset charset, Boolean exceptions);
    String header(String name);
    HttpCookie cookie(String name);
    <T> T jsonObject(Class<T> type);
    <T> T jsonObject(Class<T> type, Charset charset);
    <T> T jsonObject(Class<T> type, Boolean exceptions);
    <T> T jsonObject(Class<T> type, Charset charset, Boolean exceptions);

    abstract class StandardResponse implements HttpResponse {
        protected Integer code;
        protected Boolean error = false;
        protected Throwable exception;
        protected Boolean exceptions = true;

        @Override
        public Integer code() {
            return this.code;
        }

        @Override
        public byte[] bytes() {
            return this.bytes(exceptions);
        }

        @Override
        public String text() {
            return this.text(Constants.defaultCharset, exceptions);
        }

        @Override
        public String text(Charset charset) {
            return this.text(charset, exceptions);
        }

        @Override
        public String text(Boolean exceptions) {
            return this.text(Constants.defaultCharset, exceptions);
        }

        @Override
        public <T> T jsonObject(Class<T> type) {
            return this.jsonObject(type, Constants.defaultCharset, exceptions);
        }

        @Override
        public <T> T jsonObject(Class<T> type, Charset charset) {
            return this.jsonObject(type, charset, exceptions);
        }

        @Override
        public <T> T jsonObject(Class<T> type, Boolean exceptions) {
            return this.jsonObject(type, Constants.defaultCharset, exceptions);
        }
    }

    final class CustomResponse extends StandardResponse {
        private final URL url;
        private final InputStream stream;
        private static final JsonSlurper js = new JsonSlurper();

        private String cachedText;
        private byte[] cachedBytes;

        public CustomResponse(Integer code, InputStream stream) {
            this(code, stream, false, null, defaultUrl());
        }

        public CustomResponse(Integer code, InputStream stream, Boolean error, Throwable exception) {
            this(code, stream, error, exception, defaultUrl());
        }
        
        private static URL defaultUrl() {
            try {
                return new URL("http://localhost");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        public CustomResponse(Integer code, InputStream stream, Boolean error, Throwable exception, URL url) {
            this.code = code;
            this.stream = stream;
            this.error = error;
            this.exception = exception;
            this.url = url;
        }

        @Override
        public URL url() {
            return this.url;
        }

        @Override
        public Boolean error() {
            return this.error;
        }

        @Override
        public Throwable exception() {
            if (this.exception == null) {
                throw new IllegalStateException("No exception were been thrown at this moment!");
            }
            return this.exception;
        }

        @Override
        public synchronized String text(Charset charset, Boolean exceptions) {
            if (exceptions && this.exception != null) {
                if (this.exception instanceof RuntimeException) throw (RuntimeException) this.exception;
                throw new RuntimeException(this.exception);
            }
            if (cachedText != null) return cachedText;
            
            try {
                byte[] b = bytes(exceptions); // Use bytes() to read stream and cache bytes
                cachedText = new String(b, charset);
                return cachedText;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public synchronized byte[] bytes(Boolean exceptions) {
            if (exceptions && this.exception != null) {
                if (this.exception instanceof RuntimeException) throw (RuntimeException) this.exception;
                throw new RuntimeException(this.exception);
            }
            if (cachedBytes != null) return cachedBytes;

            try {
                cachedBytes = this.stream.readAllBytes();
                return cachedBytes;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String header(String name) {
            return "header " + name + " value";
        }

        @Override
        public HttpCookie cookie(String name) {
            return new HttpCookie(name, "value");
        }

        @Override
        public <T> T jsonObject(Class<T> type, Charset charset, Boolean exceptions) {
            Object parsed = js.parseText(this.text(charset, exceptions));
            return DefaultGroovyMethods.asType(parsed, type);
        }

        @Override
        public String toString() {
            String streamText = "";
            try {
                // Peek at stream or use cached?
                // Groovy code used `stream.text`.
                // If stream is consumed, it's gone.
                // But CustomResponse is for testing.
                if (cachedText != null) streamText = cachedText;
                else streamText = "stream"; 
            } catch (Exception e) {}
            
            return this.getClass().getSimpleName() + " (code:" + code + ", stream:" + streamText + ", error:" + error + ", exception:" + exception + ")";
        }
    }
}
