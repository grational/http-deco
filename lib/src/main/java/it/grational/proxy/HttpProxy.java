package it.grational.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

public class HttpProxy extends Proxy {
    
    public HttpProxy(Map<String, Object> params) {
        super(
            Proxy.Type.HTTP,
            new InetSocketAddress(
                (String) params.get("host"),
                (Integer) params.get("port")
            )
        );
    }
}
