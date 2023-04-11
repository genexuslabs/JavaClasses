package com.genexus.db.cosmosdb;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
public class CosmosDBDriver implements  Driver
{
	private static final int MAJOR_VERSION = 1;
	private static final int MINOR_VERSION = 0;
	private static final String DRIVER_ID = "cosmosdb:";

	private static final CosmosDBDriver COSMOSDB_DRIVER;
	static
	{
		COSMOSDB_DRIVER = new CosmosDBDriver();
		try
		{
			DriverManager.registerDriver(COSMOSDB_DRIVER);
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public CosmosDBDriver()
	{
	}

	@Override
	public Connection connect(String url, Properties info)
	{
		if(!acceptsURL(url))
			return null;
		try {
			return new CosmosDBConnection(url.substring(DRIVER_ID.length()), info);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
