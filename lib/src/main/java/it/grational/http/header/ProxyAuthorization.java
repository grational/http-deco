package it.grational.http.header;

import java.util.Map;

public class ProxyAuthorization extends BasicAuthentication {
    public ProxyAuthorization(Map<String, Object> params) {
        Object userObj = params.get("username");
        if (userObj == null) {
             throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid username parameter");
        }
        this.username = userObj.toString();

        Object passObj = params.get("password");
        if (passObj == null) {
             throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid password parameter");
        }
        this.password = passObj.toString();
    }

    @Override
    public String name() {
        return "Proxy-Authorization";
    }
}
