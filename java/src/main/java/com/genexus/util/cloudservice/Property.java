package com.genexus.util.cloudservice;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder({ "Name", "Value" })
public class Property
{

    @JacksonXmlProperty(localName = "Value")
    private String value;

    @JacksonXmlProperty(localName = "Name")
    private String name;

    public String getValue ()
    {
        return value;
    }

    public void setValue (final String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Value = "+value+", Name = "+name+"]";
    }
}