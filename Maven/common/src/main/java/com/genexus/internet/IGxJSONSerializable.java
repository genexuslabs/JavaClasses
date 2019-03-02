package com.genexus.internet;
import com.genexus.SdtMessages_Message;
import com.genexus.GXBaseCollection;

public interface IGxJSONSerializable
{
		Object GetJSONObject();
    String toJSonString();
    boolean fromJSonString(String s);
    boolean fromJSonString(String s, GXBaseCollection<SdtMessages_Message> messages);
}
