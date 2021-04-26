package com.genexus.db.service;

import java.util.HashMap;

public class IODataMapExt implements IODataMap
{
    protected String entity;
    protected IODataMap map;
    public IODataMapExt(String entity, IODataMap map)
    {
        this.entity = entity;
        this.map = map;
    }

    @Override
	@SuppressWarnings("unchecked")
    public Object getValue(IOServiceContext context, HashMap<String, Object> currentEntry)
    {
        HashMap<String, Object> link = (HashMap<String, Object>) currentEntry.get(context.entity(entity));
        return link != null ? map.getValue(context, link) : null;
    }

    @Override
    public String getName()
    {
        return entity;
    }

    @Override
	@SuppressWarnings("unchecked")
    public void setValue(HashMap<String, Object> currentEntry, Object value)
    {
        HashMap<String, Object> child = (HashMap<String, Object>)currentEntry.get(entity);
        if(child == null)
        {
            child = new HashMap<String, Object>();
            currentEntry.put(entity, child);
        }
        map.setValue(child, value);
    }    
}
