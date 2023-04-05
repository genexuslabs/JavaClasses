package com.genexus.xml;

import com.genexus.common.interfaces.SpecificImplementation;

public class GXXMLSerializer {

    public static String serialize(boolean includeHeader, Object instance, Class... classes) {
       if (SpecificImplementation.GXXMLSerializer != null)
		 return SpecificImplementation.GXXMLSerializer.serialize(includeHeader, instance, classes);
       return "";
    }

    public static String serialize(boolean includeHeader, Object instance, String name, Class... classes) {
    	if (SpecificImplementation.GXXMLSerializer != null)
    		return SpecificImplementation.GXXMLSerializer.serialize(includeHeader, instance, name, classes);
    	return "";
    }
}
