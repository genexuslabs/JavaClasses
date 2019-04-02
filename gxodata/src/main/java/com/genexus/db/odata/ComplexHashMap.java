package com.genexus.db.odata;
import com.genexus.db.service.IODataMap;
import java.util.HashMap;

public class ComplexHashMap extends HashMap<String, Object>
{
    private String entity;
    public ComplexHashMap(String entity)
    {
        this.entity = entity;
    }
    
    public String getEntity()
    {
        return entity;
    }
    
    public ComplexHashMap set(String key, Object value)
    {
        put(key, value);
        return this;
    }
    
    public ComplexHashMap set(IODataMap key, Object value)
    {
        key.setValue(this, value);
        return this;
    } 
    
    public ComplexHashMap set(ComplexHashMap complex)
    {
        put(complex.getEntity(), complex);
        return this;
    }
}
