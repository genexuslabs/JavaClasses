package com.genexus.util.cloudservice;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Properties
{
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Property")
    private Property[] property;

    public Property[] getProperty ()
    {
        return property;
    }

    public void setProperty (Property[] property)
    {
        this.property = property;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Property = "+property+"]";
    }
}