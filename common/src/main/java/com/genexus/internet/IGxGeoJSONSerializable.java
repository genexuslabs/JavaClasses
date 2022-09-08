package com.genexus.internet;

public interface IGxGeoJSONSerializable
{

    String toJSonString();
    boolean fromJSonString(String s);

    String toGeoJSON();
    void fromGeoJSON(String s);
}