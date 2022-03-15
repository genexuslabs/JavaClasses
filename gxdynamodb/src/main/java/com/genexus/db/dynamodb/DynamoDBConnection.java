package com.genexus.db.dynamodb;

import com.genexus.db.service.ServiceConnection;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.regions.Region;import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executor;

public class DynamoDBConnection extends ServiceConnection
{
	private static final String GXDYNAMODB_VERSION = "1.0";

	private static final String CLIENT_ID = "user";
	private static final String CLIENT_SECRET = "password";
	private static final String REGION = "region";
	private static final String LOCAL_URL = "localurl";


	DynamoDbClient mDynamoDB;
	Region mRegion = Region.US_EAST_1;

	public DynamoDBConnection(String connUrl, Properties initialConnProps) throws SQLException
	{
		super(connUrl, initialConnProps); // After initialization use props variable from super class to manage properties
		initializeDBConnection(connUrl);
	}

	private void initializeDBConnection(String connUrl)
	{
		String mLocalUrl = null, mClientId = null, mClientSecret = null;
		for(Enumeration keys = props.keys(); keys.hasMoreElements(); )
		{
			String key = ((String)keys.nextElement());
			String value = props.getProperty(key, key);
			switch(key.toLowerCase())
			{
				case LOCAL_URL: mLocalUrl = value; break;
				case CLIENT_ID: mClientId = value; break;
				case CLIENT_SECRET: mClientSecret = value; break;
				case REGION: mRegion = Region.of(value); break;
				default: break;
			}
		}
		DynamoDbClientBuilder builder = DynamoDbClient.builder().region(mRegion);
		if(mLocalUrl != null)
			builder = builder.endpointOverride(URI.create(mLocalUrl));
		if(mClientId != null && mClientSecret != null)
		{
			AwsBasicCredentials mCredentials = AwsBasicCredentials.create(mClientId, mClientSecret);
			builder = builder.credentialsProvider(StaticCredentialsProvider.create(mCredentials));
		}
		mDynamoDB = builder.build();
	}

//----------------------------------------------------------------------------------------------------

	@Override
	public void close()
	{
		mDynamoDB.close();
		mDynamoDB = null;
	}

	@Override
	public boolean isClosed()
	{
		return mDynamoDB != null;
	}

	//----------------------------------------------------------------------------------------------------
	@Override
	public String getDatabaseProductName()
	{
		return "DynamoDB";
	}

	@Override
	public String getDatabaseProductVersion()
	{
		return "";
	}

	@Override
	public String getDriverName()
	{
		return mDynamoDB.getClass().getName();
	}

	@Override
	public String getDriverVersion()
	{
		return String.format("%s/%s", mDynamoDB.serviceName(), GXDYNAMODB_VERSION);
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
