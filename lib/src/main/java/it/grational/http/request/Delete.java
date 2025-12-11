package it.grational.http.request;

import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Delete extends StandardRequest {

    public Delete(URL url) {
        this(url, new HashMap<>(), null);
    }

    public Delete(URL url, Map<String, Object> params, Proxy proxy) {
        this.method = "DELETE";
        this.url = url;
        this.parameters = params != null ? params : new HashMap<>();
        this.proxy = proxy;
    }

    public Delete(Map<String, Object> params) {
        this(
            (URL) params.get("url"),
            mergeParams(params),
            (Proxy) params.get("proxy")
        );
    }

    private static Map<String, Object> mergeParams(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        if (params.get("parameters") instanceof Map) {
            result.putAll((Map<String, Object>) params.get("parameters"));
        }
        
        if (params.containsKey("connectTimeout")) result.put("connectTimeout", params.get("connectTimeout"));
        if (params.containsKey("readTimeout")) result.put("readTimeout", params.get("readTimeout"));
        if (params.containsKey("headers")) result.put("headers", params.get("headers"));
        if (params.containsKey("cookies")) result.put("cookies", params.get("cookies"));
        
        return result;
    }
}
