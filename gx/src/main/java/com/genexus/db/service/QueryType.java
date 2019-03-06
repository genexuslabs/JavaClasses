package com.genexus.db.service;

public enum QueryType
{
    QUERY("QUERY"),
    UPD("UPD"),
    INS("INS"),
    DLT("DLT"),
    LINK("LINK"),
    EXT("EXT");

    private final String queryType;
    QueryType(String queryType)
    {
        this.queryType = queryType;
    }    
}
