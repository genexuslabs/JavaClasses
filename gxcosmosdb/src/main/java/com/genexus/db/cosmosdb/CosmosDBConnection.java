package com.genexus.db.cosmosdb;

import com.genexus.db.service.ServiceConnection;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;

import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executor;

public class CosmosDBConnection extends ServiceConnection
{
	private static final String GXCOSMOSDB_PRODUCT_NAME = "CosmosDB";
	private static final String GXCOSMOSDB_VERSION = "1.0";
	private static final String REGION = "applicationregion";
	private static final String DATABASE = "database";
	private static final String SERVICE_URI = "serviceuri";
	private static final String ACCOUNT_KEY = "accountkey";

	private String mregion;
	private String mdatabase;
	private String maccountKey;
	private String maccountEndpoint;
	CosmosAsyncClient cosmosClient;
	CosmosAsyncDatabase cosmosDatabase = null;
	public CosmosDBConnection(String connUrl, Properties initialConnProps) throws Exception {
		super(connUrl, initialConnProps);
		initializeDBConnection(connUrl);

	}
	private void initializeDBConnection(String connUrl) throws Exception
	{
		for(Enumeration<Object> keys = props.keys(); keys.hasMoreElements(); )
		{
			String key = (String)keys.nextElement();
			String value = props.getProperty(key, key);
			switch(key.toLowerCase())
			{
				case SERVICE_URI: maccountEndpoint = value.replace("AccountEndpoint=",""); break;
				case ACCOUNT_KEY: maccountKey = value; break;
				case REGION: mregion = value; break;
				case DATABASE: mdatabase = value; break;
				default: break;
			}
		}
		if (maccountEndpoint == null)
		{
			String accountURI = "";
			if (connUrl.contains("AccountEndpoint="))
			{
				int pos1 = connUrl.indexOf("AccountEndpoint=",0);
				int pos2 = connUrl.indexOf(";",pos1);
				accountURI = connUrl.substring(pos1,pos2);
				maccountEndpoint = accountURI != "" ? accountURI.replace("AccountEndpoint=",""):null;
			}
		}

		//Consistency Level: https://learn.microsoft.com/en-us/java/api/com.azure.cosmos.consistencylevel?view=azure-java-stable

		if (maccountEndpoint == null || maccountKey == null)
			throw(new IllegalArgumentException("Missing required credentials parameters. Enter Host Name and Key."));

		if (mdatabase == null || mregion == null)
			throw(new IllegalArgumentException("Missing additional connection options. Enter databasename and region."));

		cosmosClient = new CosmosClientBuilder()
			.endpoint(maccountEndpoint)
			.key(maccountKey)
			.consistencyLevel(ConsistencyLevel.EVENTUAL)
			//.contentResponseOnWriteEnabled(true) Disabled for performance
			.preferredRegions(Collections.singletonList(mregion))
			.connectionSharingAcrossClientsEnabled(true)
			.buildAsyncClient();

			cosmosDatabase = cosmosClient.getDatabase(mdatabase);
	}

	private CosmosAsyncContainer GetContainer(String containerName)
	{
		if (cosmosDatabase != null && StringUtils.isNotEmpty(containerName))
			return cosmosDatabase.getContainer(containerName);
		return null;
	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public void close()
	{
		cosmosClient.close();
		cosmosClient = null;
	}
	@Override
	public boolean isClosed()
	{
		return cosmosClient != null;
	}
	@Override
	public String getDatabaseProductName()
	{
		return GXCOSMOSDB_PRODUCT_NAME;
	}

	@Override
	public String getDatabaseProductVersion()
	{
		return "";
	}

	@Override
	public String getDriverName()
	{
		return cosmosClient.getClass().getName();
	}

	@Override
	public String getDriverVersion()
	{
		return String.format("%s/%s", GXCOSMOSDB_PRODUCT_NAME, GXCOSMOSDB_VERSION);
	}

	// JDK8:
	@Override
	public void setSchema(String schema)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getSchema()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void abort(Executor executor)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getNetworkTimeout()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean generatedKeyAlwaysReturned()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}