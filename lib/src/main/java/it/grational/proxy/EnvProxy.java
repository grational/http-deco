package it.grational.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvProxy implements StructuredProxy {
    private static final Pattern PROXY_PATTERN = Pattern.compile(
            "(https?|socks[45])://(?:([^:]+):((?:[^@]*@?)+)@)?([^@:]+):([1-9][0-9]{0,4})"
    );

    private final Boolean direct;
    private final String protocol;
    private final String username;
    private final String password;
    private final String host;
    private final Integer port;

    public EnvProxy(String proxyString) {
        if (proxyString == null) {
            this.direct = true;
            this.protocol = null;
            this.username = null;
            this.password = null;
            this.host = null;
            this.port = null;
        } else {
            Matcher m = PROXY_PATTERN.matcher(proxyString);
            if (!m.matches()) {
                throw new UnsupportedOperationException("Invalid proxy string '" + proxyString + "'");
            }
            this.direct = false;
            this.protocol = m.group(1);
            this.username = m.group(2);
            this.password = m.group(3);
            this.host = m.group(4);
            this.port = Integer.parseInt(m.group(5));
        }
    }

    @Override
    public Proxy proxy() {
        if (this.direct) {
            return Proxy.NO_PROXY;
        } else {
            Proxy proxy = new Proxy(
                    this.type(),
                    new InetSocketAddress(
                            this.host,
                            this.port
                    )
            );
            if (this.auth()) {
                ProxyAuthentication.enable(this.username, this.password);
            }
            return proxy;
        }
    }

    @Override
    public Proxy.Type type() {
        if (this.direct) {
            return Proxy.Type.DIRECT;
        } else if (this.protocol.matches("https?")) {
            return Proxy.Type.HTTP;
        } else if (this.protocol.matches("socks[45]")) {
            return Proxy.Type.SOCKS;
        }
        return null; // Should not happen given regex
    }

    @Override
    public String host() {
        if (this.direct) {
            throw new UnsupportedOperationException("Cannot return the proxy host with a direct connection");
        }
        return this.host;
    }

    @Override
    public Integer port() {
        if (this.direct) {
            throw new UnsupportedOperationException("Cannot return the proxy port with a direct connection");
        }
        return this.port;
    }

    @Override
    public Boolean secure() {
        if (this.direct) {
            throw new UnsupportedOperationException("Cannot return the proxy secure flag with a direct connection");
        }
        return "https".equals(this.protocol);
    }

    @Override
    public Boolean auth() {
        if (this.direct) {
            throw new UnsupportedOperationException("Cannot return the proxy auth flag with a direct connection");
        }
        return (this.username != null && this.password != null);
    }

    @Override
    public String username() {
        if (this.direct) {
            throw new UnsupportedOperationException("Cannot return the proxy username with a direct connection");
        }
        if (!this.auth()) {
            throw new UnsupportedOperationException("Cannot return the proxy username, the proxy string doesn't contain any auth credentials");
        }
        return this.username;
    }

    @Override
    public String password() {
        if (this.direct) {
            throw new UnsupportedOperationException("Cannot return the proxy password with a direct connection");
        }
        if (!this.auth()) {
            throw new UnsupportedOperationException("Cannot return the proxy password, the proxy string doesn't contain any auth credentials");
        }
        return this.password;
    }

    @Override
    public String toString() {
        if (this.direct) {
            throw new UnsupportedOperationException("Cannot return the proxy string representation with a direct connection");
        }
        StringBuilder result = new StringBuilder();
        result.append(this.protocol).append("://");
        if (this.auth()) {
            result.append(this.username).append(":").append(this.password).append("@");
        }
        result.append(this.host);
        if (this.port != null) {
            result.append(":").append(this.port);
        }

        return result.toString();
    }
}
