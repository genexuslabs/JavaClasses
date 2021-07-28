package com.genexus.specific.java;

import java.io.StringWriter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import com.genexus.common.interfaces.IExtensionGXXMLSerializer;
import com.genexus.wrapper.GXCollectionWrapper;

public class GXXMLserializer implements IExtensionGXXMLSerializer {

	@Override
	public String serialize(boolean includeHeader, Object instance, Class[] classes) {
		String name = "";
        if (classes.length == 2) {
            name = classes[1].getSimpleName();
            if (name.contains("StructSdt")) {
                name = name.replaceFirst("StructSdt", "");
            }
        }
        return serialize(includeHeader, instance, name, classes);
	}

	@Override
	public String serialize(boolean includeHeader, Object instance, String name, Class[] classes) {
	 	StringWriter result = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(classes);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            if (!name.isEmpty()) {
                JAXBElement<GXCollectionWrapper> jaxbElement = new JAXBElement<GXCollectionWrapper>(new QName(null, name + "Collection"), GXCollectionWrapper.class, (GXCollectionWrapper) instance);
                marshaller.marshal(jaxbElement, result);
            } else {
                marshaller.marshal(instance, result);
            }
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString().replaceAll("<ns\\d:", "<").replaceAll(":ns\\d=", "=").replaceAll("</ns\\d:", "</");
	}
	
	
	

}
