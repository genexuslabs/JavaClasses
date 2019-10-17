package com.genexus.db.odata;

import java.util.HashMap;
import java.util.Map;
import org.apache.olingo.client.api.domain.ClientEntity;

public class CurrentOf extends ComplexHashMap
{
    private final CurrentOfManager currentOfManager;
    private final String cursorName;
    public CurrentOf(CurrentOfManager currentOfManager, String cursorName, String entity)
    {
        super(entity);
        this.currentOfManager = currentOfManager;
        this.cursorName = cursorName;
        setCurrentOfEntry();
    }

    protected ClientEntity getUpdateEntity()
    {
        return currentOfManager.getQuery(cursorName).getCurrentEntity();
    }
    
    protected HashMap<String, Object> getCurrentOfEntry()
    {
        return currentOfEntry;
    }
    
    HashMap<String, Object> currentOfEntry;
    private void setCurrentOfEntry()
    {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> map = (HashMap<String, Object>)currentOfManager.getQuery(cursorName).currentEntry.get(getEntity());
        putAll(map);
        currentOfEntry = cloneHashMaps(new HashMap<>(map));
//        putAll(new HashMap<String, Object>(currentOfEntry));
    }

    private HashMap<String, Object> cloneHashMaps(HashMap<String, Object> hashMap)
    {
        for(String key : hashMap.keySet())
        {
           Object value = hashMap.get(key);
           if(value instanceof Map)
           {
               @SuppressWarnings("unchecked")
               HashMap<String, Object> clonedMap = new HashMap<>((HashMap<String, Object>)hashMap.remove(key));
               hashMap.put(key, cloneHashMaps(clonedMap));
           }
        }
        return hashMap;
    }
}
