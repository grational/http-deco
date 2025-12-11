package it.grational.proxy;

import java.net.URL;

public enum EnvVar {
    ALL { @Override public String value() { return System.getenv("all_proxy"); } },
    FTP { @Override public String value() { return System.getenv("ftp_proxy"); } },
    HTTP { @Override public String value() { return System.getenv("http_proxy"); } },
    HTTPS { @Override public String value() { return System.getenv("https_proxy"); } },
    RSYNC { @Override public String value() { return System.getenv("rsync_proxy"); } };

    public abstract String value();

    public static EnvVar byURL(URL url) {
        String protocol = null;
        try {
            protocol = protocolByURL(url);
            if (protocol == null) throw new IllegalArgumentException("Protocol is null");
            return EnvVar.valueOf(protocol.toUpperCase());
        } catch (Exception e) {
            throw new UnsupportedOperationException("[EnvVar] Protocol '" + protocol + "' is not supported!", e);
        }
    }

    private static String protocolByURL(URL url) {
        return url.toString().split(":")[0];
    }
}
