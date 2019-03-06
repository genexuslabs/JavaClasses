package com.genexus.db.service;

import java.util.HashMap;

public class IODataMapName implements IODataMap
{
    protected String name;
    public IODataMapName(String name)
    {
        this.name = name;
    }

    @Override
    public Object getValue(IOServiceContext context, HashMap<String, Object> currentEntry)
    {
        return currentEntry.get(name);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setValue(HashMap<String, Object> currentEntry, Object value)
    {
        currentEntry.put(name, value);
    }    
}
