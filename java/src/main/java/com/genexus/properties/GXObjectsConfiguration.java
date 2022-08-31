package  com.genexus.properties;

import com.genexus.internet.*;
import java.util.HashMap;

public class GXObjectsConfiguration {
		
	private  HashMap<String, GXObjectProperties> properties = new HashMap<String, GXObjectProperties>();	

	public GXObjectProperties propertiesFor(String objName)
	{
		if (!properties.containsKey(objName))
			properties.put(objName, new GXObjectProperties());
		return properties.get(objName);
	}
}
