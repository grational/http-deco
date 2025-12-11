package it.grational.url;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class StructuredURL implements URLConvertible {
    private final String protocol;
    private final String username;
    private final String password;
    private final String authority;
    private final String path;
    private final String qstring;

    public StructuredURL(Map<String, Object> params) {
        Object originObj = params.get("origin");
        if (originObj != null && !originObj.toString().isEmpty()) {
            String origin = originObj.toString();
            String[] splitted = origin.split("://");
            this.protocol = splitted[0];
            this.authority = splitted[1];
        } else {
            Object protocolObj = params.get("protocol");
            if (protocolObj == null || protocolObj.toString().isEmpty()) {
                 throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid protocol parameter");
            }
            this.protocol = protocolObj.toString();

            Object authorityObj = params.get("authority");
            if (authorityObj == null || authorityObj.toString().isEmpty()) {
                throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid authority parameter");
            }
            this.authority = authorityObj.toString();
        }

        Object usernameObj = params.get("username");
        this.username = (usernameObj != null) ? usernameObj.toString() : "";

        Object passwordObj = params.get("password");
        this.password = (passwordObj != null) ? passwordObj.toString() : "";

        Object pathObj = params.get("path");
        this.path = (pathObj != null) ? pathObj.toString() : "";

        Object qparamsObj = params.get("qparams");
        if (qparamsObj instanceof Map) {
            Map<?, ?> qparams = (Map<?, ?>) qparamsObj;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<?, ?> entry : qparams.entrySet()) {
                Object keyObj = entry.getKey();
                if (keyObj == null) {
                     throw new IllegalArgumentException("[" + this.getClass().getSimpleName() + "] Invalid qparam key 'null'");
                }
                String key = keyObj.toString();
                Object valueObj = entry.getValue();
                String value = (valueObj == null) ? "" : valueObj.toString();
                
                if (sb.length() > 0) {
                    sb.append("&");
                } else {
                    sb.append("?");
                }
                sb.append(key).append("=").append(value);
            }
            this.qstring = sb.toString();
        } else {
            this.qstring = "";
        }
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return new URL(this.toString());
    }

    @Override
    public URI toURI() throws URISyntaxException, MalformedURLException {
        return this.toURL().toURI();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(protocol).append("://");
        if (!username.isEmpty()) {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
            String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
            result.append(encodedUsername).append(":").append(encodedPassword).append("@");
        }
        result.append(authority);
        if (!path.isEmpty()) {
            if (path.startsWith("/")) {
                result.append(path);
            } else {
                result.append("/").append(path);
            }
        }
        result.append(qstring);

        return result.toString();
    }
}
