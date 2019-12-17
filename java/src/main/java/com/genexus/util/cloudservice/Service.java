package com.genexus.util.cloudservice;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonPropertyOrder({ "Name", "Type", "ClassName", "AllowMultiple", "Properties"})
public class Service {

    @JacksonXmlProperty(localName = "Type")
    private String type;

    @JacksonXmlProperty(localName = "Name")
    private String name;

    @JacksonXmlProperty(localName = "AllowMultiple")
    private boolean allowMultiple;

    @JacksonXmlProperty(localName = "ClassName")
    private String className;

    @JacksonXmlProperty(localName = "Properties")
    private Properties properties;



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean getAllowMultiple(){
        return this.allowMultiple;
    }

    public void setAllowMultiple(boolean allowMultiple){
        this.allowMultiple = allowMultiple;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ClassPojo [Type = " + type + ", ClassName = " + className + ", Properties = " + properties + ", Name = "
                + name + "]";
    }
}