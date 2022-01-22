package com.genexus.eo.utils;

public class GeneXusConfigUtils {

    public static String getDefaultPackageName() {
        try {
            return com.genexus.configuration.ConfigurationManager.getValue("PACKAGE");
        } catch (Exception e) {
            return "";
        }
    }
}
