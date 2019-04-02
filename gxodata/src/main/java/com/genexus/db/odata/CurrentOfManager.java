package com.genexus.db.odata;

import java.util.HashMap;

public class CurrentOfManager
{
    private HashMap<String, ODataResultSet> currentOfManager = new HashMap<>();
    public void addQuery(String cursorId, ODataResultSet resultSet)
    {
        currentOfManager.put(cursorId, resultSet);
    }
    
    public void removeQuery(String cursorId)
    {
        currentOfManager.remove(cursorId);
    }
    
    public ODataResultSet getQuery(String cursorId)
    {
        return currentOfManager.get(cursorId);
    }
}
