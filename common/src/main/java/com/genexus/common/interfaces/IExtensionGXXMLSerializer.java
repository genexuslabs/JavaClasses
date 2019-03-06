package com.genexus.common.interfaces;

public interface IExtensionGXXMLSerializer {

	String serialize(boolean includeHeader, Object instance, Class[] classes);

	String serialize(boolean includeHeader, Object instance, String name, Class[] classes);

}
