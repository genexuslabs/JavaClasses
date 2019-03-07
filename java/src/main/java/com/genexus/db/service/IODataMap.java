package com.genexus.db.service;

import java.util.HashMap;

public interface IODataMap
{
    Object getValue(IOServiceContext context, HashMap<String, Object> currentEntry);
    String getName();
    void setValue(HashMap<String, Object> currentEntry, Object value);
}
