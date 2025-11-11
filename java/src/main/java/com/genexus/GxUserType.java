package com.genexus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.xml.GXXMLSerializable;
import java.util.HashMap;
import com.genexus.json.JSONObjectWrapper;
import org.apache.logging.log4j.Logger;

public abstract class GxUserType extends GXXMLSerializable implements Cloneable, java.io.Serializable, IGXAssigned
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GxUserType.class);

	public GxUserType(ModelContext context, String type)
	{
		super(-1, context, type);
	}

	public GxUserType(int remoteHandle, ModelContext context, String type)
	{
		super( remoteHandle, context, type);
	}

	boolean bIsAssigned = true;

	public abstract String getJsonMap( String value );

	@Override
	public boolean getIsAssigned() {
		return bIsAssigned;
	}

	@Override
	public void setIsAssigned(boolean bAssigned) {
		bIsAssigned = bAssigned;
	}

	private static final ObjectMapper MAPPER =
            new ObjectMapper().disable(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS);

    protected JSONObjectWrapper getJsonObjectFromHashMap(Object userType) {
        try {
            if (userType instanceof HashMap) {
                return new JSONObjectWrapper(map);
            }

            String jsonString = MAPPER.writeValueAsString(userType);
            return new JSONObjectWrapper(jsonString);

        } catch (Exception e) {
            log.error("Could not create Json Object", e);
            return new JSONObjectWrapper(); // return empty object instead of null
        }
    }

	protected void setHashMapFromJson(String json) {
		fromjson(json);
	}

	protected void fromjson(String json){
		try {
			Object instance = this.getClass().getMethod("getExternalInstance").invoke(this);
			ObjectMapper mapper = new ObjectMapper();
			mapper.readerForUpdating(instance).readValue(json);
		}
		catch(Exception e) {
			log.error("Error executing FromJson() method", e);
		}
	}
}
