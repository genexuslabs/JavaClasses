package com.genexus.xml;

import com.genexus.ModelContext;
import com.genexus.specific.java.Connect;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GXXMLSerializableTest {

    private TestSerializable testObj;

    @Before
    public void setUp() {
        Connect.init();
        testObj = new TestSerializable(new ModelContext(TestSerializable.class), "TestType");
    }

    @Test
    public void testFromJSonStringWithSimpleFields() {
        // Arrange
        String json = "{\"stringField\":\"test value\",\"intField\":42,\"boolField\":true,\"doubleField\":3.14}";
        
        // Act
        boolean result = testObj.fromJSonString(json);
        
        // Assert
        assertTrue("JSON parsing should succeed", result);
        assertEquals("String field should match", "test value", testObj.getStringField());
        assertEquals("Int field should match", 42, testObj.getIntField());
        assertTrue("Boolean field should match", testObj.isBoolField());
        assertEquals("Double field should match", 3.14, testObj.getDoubleField(), 0.001);
    }
    
    @Test
    public void testFromJSonStringWithNullFields() {
        // Arrange
        String json = "{\"stringField\":null,\"intField\":null,\"boolField\":null,\"doubleField\":null}";
        
        // Act
        boolean result = testObj.fromJSonString(json);
        
        // Assert
        assertTrue("JSON parsing should succeed", result);
        assertNull("String field should be null", testObj.getStringField());
        assertEquals("Int field should be default", 0, testObj.getIntField());
        assertFalse("Boolean field should be default", testObj.isBoolField());
        assertEquals("Double field should be default", 0.0, testObj.getDoubleField(), 0.001);
    }
    
    @Test
    public void testFromJSonStringWithNestedObject() {
        // Arrange
        String json = "{\"stringField\":\"parent\",\"nestedObject\":{\"stringField\":\"child\"}}";
        
        // Act
        boolean result = testObj.fromJSonString(json);
        
        // Assert
        assertTrue("JSON parsing should succeed", result);
        assertEquals("Parent string field should match", "parent", testObj.getStringField());
        assertNotNull("Nested object should not be null", testObj.getNestedObject());
        assertEquals("Nested string field should match", "child", testObj.getNestedObject().getStringField());
    }
    
    @Test
    public void testFromJSonStringWithCollection() {
        // Arrange
        String json = "{\"stringField\":\"parent\",\"items\":[{\"stringField\":\"item1\"},{\"stringField\":\"item2\"}]}";
        
        // Act
        boolean result = testObj.fromJSonString(json);
        
        // Assert
        assertTrue("JSON parsing should succeed", result);
        assertEquals("Parent string field should match", "parent", testObj.getStringField());
        assertNotNull("Items collection should not be null", testObj.getItems());
        assertEquals("Items collection should have correct size", 2, testObj.getItems().size());
        assertEquals("First item should match", "item1", (testObj.getItems().item(1)).getStringField());
        assertEquals("Second item should match", "item2", (testObj.getItems().item(2)).getStringField());
    }
    
    @Test
    public void testFromJSonStringWithInvalidJson() {
        // Arrange
        String json = "{invalid json";
        
        // Act
        boolean result = testObj.fromJSonString(json);
        
        // Assert
        assertFalse("JSON parsing should fail", result);
    }
    
    @Test
    public void testFromJSonStringWithEmptyJson() {
        // Arrange
        String json = "{}";
        
        // Act
        boolean result = testObj.fromJSonString(json);
        
        // Assert
        assertTrue("Empty JSON parsing should succeed", result);
        assertNull("String field should be null", testObj.getStringField());
    }

    // Test implementation of GXXMLSerializable for testing purposes
    public static class TestSerializable extends GXXMLSerializable {
        private String stringField;
        private int intField;
        private boolean boolField;
        private double doubleField;
        private TestSerializable nestedObject;
        private TestSerializableCollection items;

        public TestSerializable() {
            super(new ModelContext(TestSerializable.class), "TestType");
        }

		public TestSerializable(ModelContext context) {
			super(context, "TestType");
		}

        public TestSerializable(ModelContext context, String type) {
            super(context, type);
        }

        @Override
        public String getJsonMap(String value) {
            return value;
        }

        @Override
        public void initialize() {
            items = new TestSerializableCollection();
        }

        // Getter and setter methods with patterns expected by GXXMLSerializable
        public String getgxTv_GXXMLSerializableTest$TestSerializable_StringField() {
            return stringField;
        }

        public void setgxTv_GXXMLSerializableTest$TestSerializable_StringField(String value) {
            this.stringField = value;
        }

        public int getgxTv_GXXMLSerializableTest$TestSerializable_IntField() {
            return intField;
        }

        public void setgxTv_GXXMLSerializableTest$TestSerializable_IntField(int value) {
            this.intField = value;
        }

        public boolean getgxTv_GXXMLSerializableTest$TestSerializable_BoolField() {
            return boolField;
        }

        public void setgxTv_GXXMLSerializableTest$TestSerializable_BoolField(boolean value) {
            this.boolField = value;
        }

        public double getgxTv_GXXMLSerializableTest$TestSerializable_DoubleField() {
            return doubleField;
        }

        public void setgxTv_GXXMLSerializableTest$TestSerializable_DoubleField(double value) {
            this.doubleField = value;
        }

        public TestSerializable getgxTv_GXXMLSerializableTest$TestSerializable_NestedObject() {
            return nestedObject;
        }

        public void setgxTv_GXXMLSerializableTest$TestSerializable_NestedObject(TestSerializable value) {
            this.nestedObject = value;
        }

        public TestSerializableCollection getgxTv_GXXMLSerializableTest$TestSerializable_Items() {
            return items;
        }

        public void setgxTv_GXXMLSerializableTest$TestSerializable_Items(TestSerializableCollection value) {
            this.items = value;
        }

        // Convenience getters for simpler test code
        public String getStringField() {
            return stringField;
        }

        public int getIntField() {
            return intField;
        }

        public boolean isBoolField() {
            return boolField;
        }

        public double getDoubleField() {
            return doubleField;
        }

        public TestSerializable getNestedObject() {
            return nestedObject;
        }

        public TestSerializableCollection getItems() {
            return items;
        }
    }

    // Collection class needed for collection tests
    public static class TestSerializableCollection extends com.genexus.GXSimpleCollection<TestSerializable> {
        public TestSerializableCollection() {
            super(TestSerializable.class, "TestSerializable", "TestCollection");
        }
        
        @Override
        public TestSerializable item(int i) {
            return get(i-1);
        }
    }
}