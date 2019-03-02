package com.genexus.db.service;

public class ServiceException extends Error
{
    private String sqlState;
    private int vendorCode;
    public ServiceException(String reason, String sqlState, int vendorCode)
    {
        super(reason);
        this.sqlState = sqlState;
        this.vendorCode = vendorCode;
    }
    
    public String getSQLState()
    {
        return sqlState;
    }
    
    public int getVendorCode()
    {
        return vendorCode;
    }
}
