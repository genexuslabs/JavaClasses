package com.genexus.util.cloudservice;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement( localName = "Services")
public class Services
{
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Service")
    private Service[] service;

    public Service[] getService ()
    {
        return service;
    }

    public void setService (Service[] Service)
    {
        this.service = Service;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Service = "+service+"]";
    }
}