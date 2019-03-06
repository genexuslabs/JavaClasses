package com.genexus.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class StorageUtils {

    public static final String DELIMITER = "/";

    public static String normalizeDirectoryName(String name) {
        if (!name.endsWith(DELIMITER)) {
            name += DELIMITER;
        }
        return name;
    }

    public static String encodeName(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8")
                    .replaceAll("%2F", "/")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            return name;
        }
    }

    public static String decodeName(String name) {
        try {
            return URLDecoder.decode(name, "UTF-8")
                    .replaceAll("%20", "\\+")
                    .replaceAll("!", "\\%21")
                    .replaceAll("'", "\\%27")
                    .replaceAll("\\(", "\\%28")
                    .replaceAll("\\)", "\\%29")
                    .replaceAll("~", "\\%7E");
        } catch (UnsupportedEncodingException e) {
            return name;
        }
    }
}
