package com.genexus.internet;

public interface IGxJSONAble
{
    void tojson();
    void AddObjectProperty(String name, Object prop);
    Object GetJSONObject();
    Object GetJSONObject(boolean includeState);
    void FromJSONObject(Object obj);
    String ToJavascriptSource();
}
