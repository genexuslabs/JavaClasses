package com.genexus.opentelemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ExtractAttributeValueTest {

    @Test
    void testExtractAttributeValue() {
        // Test case 1: Test with null parameters
        String attributeValue = ExtractAttributeValueHelper.extractAttributeValue(null, "service.name");
        assertNull(attributeValue, "Should return null for null resource attributes");
        
        attributeValue = ExtractAttributeValueHelper.extractAttributeValue("service.name=test", null);
        assertNull(attributeValue, "Should return null for null attribute name");
        
        // Test case 2: Empty resource attributes
        attributeValue = ExtractAttributeValueHelper.extractAttributeValue("", "service.name");
        assertNull(attributeValue, "Should return null for empty resource attributes");
        
        // Test case 3: Simple key-value pair
        String simpleAttributes = "service.name=test-service";
        attributeValue = ExtractAttributeValueHelper.extractAttributeValue(simpleAttributes, "service.name");
        assertEquals("test-service", attributeValue, "Should extract simple attribute correctly");
        
        // Test case 4: Multiple key-value pairs
        String multiAttributes = "service.name=my-service,service.version=1.0.0";
        attributeValue = ExtractAttributeValueHelper.extractAttributeValue(multiAttributes, "service.name");
        assertEquals("my-service", attributeValue, "Should extract first attribute correctly");
        
        attributeValue = ExtractAttributeValueHelper.extractAttributeValue(multiAttributes, "service.version");
        assertEquals("1.0.0", attributeValue, "Should extract second attribute correctly");
        
        // Test case 5: Non-existent attribute
        attributeValue = ExtractAttributeValueHelper.extractAttributeValue(multiAttributes, "non.existent");
        assertNull(attributeValue, "Should return null for non-existent attribute");
    }
}