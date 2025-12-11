package it.grational.proxy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NoProxy implements ExclusionList {
    private final List<InetSocketAddress> list;

    public NoProxy() {
        this.list = parseEnv();
    }

    private List<InetSocketAddress> parseEnv() {
        String noProxy = System.getenv("no_proxy");
        if (noProxy == null) return new ArrayList<>();

        List<InetSocketAddress> result = new ArrayList<>();
        String[] elements = noProxy.split(",");
        for (String elem : elements) {
            elem = elem.trim();
            if (elem.startsWith(".")) {
                elem = elem.substring(1);
            }

            String[] splitted = elem.split(":");
            String host = splitted[0];
            int port;
            if (splitted.length == 1) {
                port = Default.ANY.getPort();
            } else {
                port = Integer.parseInt(splitted[splitted.length - 1]);
            }
            // Use createUnresolved to avoid DNS lookup for simple string matching
            result.add(InetSocketAddress.createUnresolved(host, port));
        }
        return result;
    }

    @Override
    public Boolean exclude(URL url) {
        String host = url.getHost();
        int port = (url.getPort() != -1) ? url.getPort() : defaultPortBy(url.getProtocol());
        
        for (InetSocketAddress addr : list) {
            boolean hostMatch = host.endsWith(addr.getHostName());
            
            if (addr.getPort() != Default.ANY.getPort()) {
                if (hostMatch && port == addr.getPort()) return true;
            } else {
                if (hostMatch) return true;
            }
        }
        return false;
    }

    private Integer defaultPortBy(String protocol) {
        Integer result = Default.HTTP.getPort();
        if (protocol != null) {
            try {
                result = Default.valueOf(protocol.toUpperCase()).getPort();
            } catch (IllegalArgumentException e) {
                // Ignore, return HTTP default
            }
        }
        return result;
    }

    private enum Default {
        ANY(0),
        HTTP(80),
        HTTPS(443),
        FTP(21);

        private final int port;

        Default(int port) {
            this.port = port;
        }

        public int getPort() {
            return port;
        }
    }
}
