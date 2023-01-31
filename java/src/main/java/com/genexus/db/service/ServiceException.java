package com.genexus.db.service;

import java.sql.SQLException;

public class ServiceException extends Error
{
    private String sqlState;
    private int vendorCode;
    public ServiceException(String reason, String sqlState, int vendorCode)
    {
		this(reason, sqlState, vendorCode, null);
    }

	public ServiceException(String reason, String sqlState, int vendorCode, Throwable innerException)
	{
		super(reason, innerException);
		this.sqlState = sqlState;
		this.vendorCode = vendorCode;
	}

	public static SQLException createSQLException(ServiceError serviceError, Throwable e)
	{
		return new SQLException(createServiceException(serviceError, e));
	}

	public String getSQLState()
    {
        return sqlState;
    }
    
    public int getVendorCode()
    {
        return vendorCode;
    }

	public static ServiceException createServiceException(ServiceError serviceError)
	{
		return new ServiceException(serviceError.toString(), serviceError.getSqlState(), serviceError.getCode());
	}

	public static ServiceException createServiceException(ServiceError serviceError, Throwable innerException)
	{
		return new ServiceException(serviceError.toString(), serviceError.getSqlState(), serviceError.getCode(), innerException);
	}
}
