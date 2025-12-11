package it.grational.http.request;

import groovy.json.JsonOutput;
import it.grational.http.shared.Constants;

import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JsonPut extends Put {

    public JsonPut(Map<String, Object> params) {
        super(createPutParams(params));
    }

    private static Map<String, Object> createPutParams(Map<String, Object> params) {
        Map<String, Object> putParams = new HashMap<>();
        putParams.put("url", params.get("url"));

        String body = (String) params.get("json");
        if (body == null && params.get("map") != null) {
            body = JsonOutput.toJson(params.get("map"));
        }
        putParams.put("body", body);

        putParams.put("parameters", params.get("parameters"));
        putParams.put("connectTimeout", params.get("connectTimeout"));
        putParams.put("readTimeout", params.get("readTimeout"));

        Map<String, String> headers = (Map<String, String>) params.get("headers");
        if (headers == null) headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        putParams.put("headers", headers);

        putParams.put("cookies", params.get("cookies"));
        putParams.put("proxy", params.get("proxy"));
        putParams.put("charset", params.get("charset"));

        return putParams;
    }

    public JsonPut(URL url, String json, Map<String, Object> params, Proxy proxy) {
        super(url, json, params, proxy);
        ensureContentType();
    }

    public JsonPut(URL url, String json) {
        this(url, json, new HashMap<>(), null);
    }

    public JsonPut(URL url, Object map) {
        this(url, JsonOutput.toJson(map), new HashMap<>(), null);
    }

    private void ensureContentType() {
        if (!this.parameters.containsKey("headers")) {
            this.parameters.put("headers", new HashMap<String, String>());
        }
        ((Map<String, String>) this.parameters.get("headers")).put("Content-Type", "application/json");
    }
}
