package it.grational.http.request;

import it.grational.http.shared.Constants;

import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class FormUrlencodedPost extends Post {

    public FormUrlencodedPost(Map<String, Object> params) {
        super(createFormParams(params));
    }

    private static Map<String, Object> createFormParams(Map<String, Object> params) {
        Map<String, Object> postParams = new HashMap<>();
        postParams.put("url", params.get("url"));

        Charset charset = (Charset) params.get("charset");
        if (charset == null) charset = Constants.defaultCharset;

        Map<String, Object> form = (Map<String, Object>) params.get("form");
        postParams.put("body", encodeFormParams(form, charset, ""));

        postParams.put("parameters", params.get("parameters"));
        postParams.put("connectTimeout", params.get("connectTimeout"));
        postParams.put("readTimeout", params.get("readTimeout"));

        Map<String, String> headers = (Map<String, String>) params.get("headers");
        if (headers == null) headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        postParams.put("headers", headers);

        postParams.put("cookies", params.get("cookies"));
        postParams.put("proxy", params.get("proxy"));
        postParams.put("charset", charset);

        return postParams;
    }

    public FormUrlencodedPost(URL url, Map<String, Object> form, Map<String, Object> params, Proxy proxy) {
        super(url, null, params, proxy);
        Charset charset = (Charset) params.get("charset");
        if (charset == null) charset = Constants.defaultCharset;
        
        this.body = encodeFormParams(form, charset, "");
        ensureContentType();
    }
    
    public FormUrlencodedPost(URL url, Map<String, Object> form) {
        this(url, form, new HashMap<>(), null);
    }

    public FormUrlencodedPost(URL url) {
        this(url, new HashMap<>());
    }

    private void ensureContentType() {
        if (!this.parameters.containsKey("headers")) {
            this.parameters.put("headers", new HashMap<String, String>());
        }
        ((Map<String, String>) this.parameters.get("headers")).put("Content-Type", "application/x-www-form-urlencoded");
    }

    public FormUrlencodedPost withFormParam(String key, String value) {
        Map<String, Object> singleParam = new HashMap<>();
        singleParam.put(key, value);
        this.body = encodeFormParams(singleParam, this.charset, this.body);
        return this;
    }

    private static String encodeFormParams(Map<String, Object> form, Charset charset, String seed) {
        StringBuilder acc = new StringBuilder(seed != null ? seed : "");
        if (form == null) return acc.toString();
        
        for (Map.Entry<String, Object> entry : form.entrySet()) {
            if (acc.length() > 0) acc.append("&");
            try {
                acc.append(URLEncoder.encode(entry.getKey(), charset.name()))
                   .append("=")
                   .append(URLEncoder.encode(entry.getValue().toString(), charset.name()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return acc.toString();
    }
}