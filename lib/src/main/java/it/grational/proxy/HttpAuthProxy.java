package it.grational.proxy;

import java.util.Map;

public class HttpAuthProxy extends HttpProxy {
    
    public HttpAuthProxy(Map<String, Object> params) {
        super(params);
        ProxyAuthentication.enable(
            (String) params.get("username"),
            (String) params.get("password")
        );
    }
}
