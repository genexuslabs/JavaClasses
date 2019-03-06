package com.genexus.xml;

import org.simpleframework.xml.*;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.*;
import org.simpleframework.xml.stream.*;
import org.simpleframework.xml.transform.*;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.GXProperties;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



public class GXXMLSerializer {

    public static String serializeSimpleXml(boolean includeHeader, Object instance, String header) {
        VisitorStrategy visitor = new VisitorStrategy(new com.genexus.xml.GXTypeInterceptor(header));
        Serializer serializer;
        Format format = null;
        if (includeHeader) {
            format = new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>");
        }
        //Probar llamar Persister con todos los parametros.
        if (format != null) {
            serializer = new Persister(visitor, getRegistryMatcher(), format);
        } else {
            serializer = new Persister(visitor, getRegistryMatcher());
        }

        StringWriter result = new StringWriter();
        try {
            serializer.write(instance, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String serializeSimpleXml(boolean includeHeader, Object instance, String header, String elementsName, String containedXmlNamespace) {
        VisitorStrategy visitor = new VisitorStrategy(new GXSDTInterceptor(header, elementsName, containedXmlNamespace));
        Serializer serializer;
        Format format = null;
        if (includeHeader) {
            format = new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>");
        }
        //Probar llamar Persister con todos los parametros.
        if (format != null) {
            serializer = new Persister(visitor, getRegistryMatcher(), format);
        } else {
            serializer = new Persister(visitor, getRegistryMatcher());
        }

        StringWriter result = new StringWriter();
        try {
            serializer.write(instance, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String serializeSimpleXml(boolean includeHeader, Object instance, GXProperties stateAttributes) {
        VisitorStrategy visitor = new VisitorStrategy(new GXStateInterceptor(stateAttributes));
        Serializer serializer;
        Format format = null;

        if (includeHeader) {
            format = new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>");
        }
        //Probar llamar Persister con todos los parametros.
        if (format != null) {
            serializer = new Persister(visitor, getRegistryMatcher(), format);
        } else {
            serializer = new Persister(visitor, getRegistryMatcher());
        }

        StringWriter result = new StringWriter();
        try {
            serializer.write(instance, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
    
    private static RegistryMatcher getRegistryMatcher(){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        RegistryMatcher m = new RegistryMatcher();
        m.bind(Date.class, new DateFormatTransformer(format));
        return m;
    }

    //SIMPLEXML
    public static String serialize(boolean includeHeader, Object instance, VisitorStrategy visitor) {
        Serializer serializer;
        Format format = null;
        if (includeHeader) {
            format = new Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>");
        }
        //Probar llamar Persister con todos los parametros.
        if (visitor != null && format != null) {
            serializer = new Persister(visitor, format);
        } else if (visitor != null) {
            serializer = new Persister(visitor);
        } else if (format != null) {
            serializer = new Persister(format);
        } else {
            serializer = new Persister();
        }

        StringWriter result = new StringWriter();
        try {
            serializer.write(instance, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static Object deserializeSimpleXml(Class instance, String xml) {
        Serializer serializer = new Persister();
        try {
            return serializer.read(instance, xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object deserializeSimpleXml(Object instance, String xml) {
        Serializer serializer = new Persister();
        try {
            return serializer.read(instance, xml);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


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
