package it.grational.http.request;

import it.grational.http.shared.Constants;

import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Post extends StandardRequest {

    public Post(URL url) {
        this(url, null, new HashMap<>(), null);
    }

    public Post(URL url, String body, Map<String, Object> params, Proxy proxy) {
        this.method = "POST";
        this.url = url;
        this.body = body;
        this.parameters = params != null ? params : new HashMap<>();
        this.proxy = proxy;
        if (this.parameters.get("charset") != null) {
            this.charset = (Charset) this.parameters.get("charset");
        } else {
            this.charset = Constants.defaultCharset;
        }
    }

    public Post(Map<String, Object> params) {
        this(
            (URL) params.get("url"),
            (String) params.get("body"),
            mergeParams(params),
            (Proxy) params.get("proxy")
        );
    }

    protected static Map<String, Object> mergeParams(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        if (params.get("parameters") instanceof Map) {
            result.putAll((Map<String, Object>) params.get("parameters"));
        }
        
        if (params.containsKey("connectTimeout")) result.put("connectTimeout", params.get("connectTimeout"));
        if (params.containsKey("readTimeout")) result.put("readTimeout", params.get("readTimeout"));
        if (params.containsKey("headers")) result.put("headers", params.get("headers"));
        if (params.containsKey("cookies")) result.put("cookies", params.get("cookies"));
        if (params.containsKey("charset")) result.put("charset", params.get("charset"));
        
        return result;
    }
}
