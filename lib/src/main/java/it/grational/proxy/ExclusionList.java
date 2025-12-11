package it.grational.proxy;

import java.net.URL;

public interface ExclusionList {
    Boolean exclude(URL url);
}
