package com.genexus.db.odata;

import com.genexus.db.service.IODataMapName;
import com.genexus.db.service.IOServiceContext;
import java.util.HashMap;
import org.apache.olingo.client.api.domain.ClientValue;
import org.apache.olingo.client.core.domain.ClientPrimitiveValueImpl;

public class ODataMapDomain extends IODataMapName
{
    public ODataMapDomain(String name)
    {
        super(name);
    }

    @Override
    public Object getValue(IOServiceContext context, HashMap<String, Object> currentEntry)
    {
        ClientValue value = (ClientValue)currentEntry.get(name);
        if(value == null)
            return null;        
        return new ClientPrimitiveValueImpl.BuilderImpl().buildInt32(((ODataConnection)context.getConnection()).getEnumValue(value.asEnum()));
    }    
}
