package com.genexus.opentelemetry;

/**
 * Helper class to provide a testable version of the extractAttributeValue method from OtelTracer.
 * This is used to support unit testing of the private method.
 */
public class ExtractAttributeValueHelper {
    
    /**
     * Extracts an attribute value from a resource attributes string
     * 
     * @param resourceAttributes the resource attributes string
     * @param attributeName the name of the attribute to extract
     * @return the attribute value, or null if not found
     */
    public static String extractAttributeValue(String resourceAttributes, String attributeName) {
        if (resourceAttributes == null || attributeName == null) {
            return null;
        }
        
        // Simple parsing for key-value pairs
        String[] pairs = resourceAttributes.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(attributeName)) {
                return keyValue[1];
            }
        }
        return null;
    }
}