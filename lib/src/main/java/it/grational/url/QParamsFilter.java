package it.grational.url;

import java.net.URL;
import java.util.List;

public class QParamsFilter {

    private final URL url;
    private final List<String> taboo;

    public QParamsFilter(URL url, List<String> taboo) {
        this.url = url;
        this.taboo = taboo;
    }

    public String filtered() {
        String output = this.url.toString();
        for (String qparam : this.taboo) {
            output = output.replaceAll(qparam + "=[^&]*", "")
                           .replaceAll("&&", "&")
                           .replaceAll("[?]&", "?")
                           .replaceAll("[?]$", "")
                           .replaceAll("&$", "");
        }
        return output;
    }
}
