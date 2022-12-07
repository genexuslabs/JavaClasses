package com.genexus.db.dynamodb;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class DynamoDBDriver implements Driver
{
	private static final int MAJOR_VERSION = 1;
	private static final int MINOR_VERSION = 0;
	private static final String DRIVER_ID = "dynamodb:";

	private static final DynamoDBDriver DYNAMODB_DRIVER;
	static
	{
		DYNAMODB_DRIVER = new DynamoDBDriver();
		try
		{
			DriverManager.registerDriver(DYNAMODB_DRIVER);
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public DynamoDBDriver()
	{
	}

	@Override
	public Connection connect(String url, Properties info)
	{
		if(!acceptsURL(url))
			return null;
		return new DynamoDBConnection(url.substring(DRIVER_ID.length()), info);
	}

	@Override
	public boolean acceptsURL(String url)
	{
		return url.startsWith(DRIVER_ID);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
	{
		return new DriverPropertyInfo[0];
	}

	@Override
	public int getMajorVersion()
	{
		return MAJOR_VERSION;
	}

	@Override
	public int getMinorVersion()
	{
		return MINOR_VERSION;
	}

	@Override
	public boolean jdbcCompliant()
	{
		return false;
	}

	@Override
	public Logger getParentLogger()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
