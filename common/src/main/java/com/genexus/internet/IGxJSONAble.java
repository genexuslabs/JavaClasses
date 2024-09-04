package com.genexus.internet;

import json.org.json.*;

public interface IGxJSONAble
{
    void tojson();
    void AddObjectProperty(String name, Object prop);
    Object GetJSONObject();
    Object GetJSONObject(boolean includeState);
    void FromJSONObject(IJsonFormattable obj);
    String ToJavascriptSource();
}
