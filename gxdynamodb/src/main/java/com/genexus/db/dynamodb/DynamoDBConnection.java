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


	DynamoDbClient mDynamoDB; //mDynamoDB
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

/*	ServiceError getServiceError(String errorCode)
	{
		if(errorCode != null)
		{
			if (modelInfo.recordNotFoundServiceCodes != null && modelInfo.recordNotFoundServiceCodes.contains(errorCode))
				return ServiceError.OBJECT_NOT_FOUND;
			if (modelInfo.recordAlreadyExistsServiceCodes != null && modelInfo.recordAlreadyExistsServiceCodes.contains(errorCode))
				return ServiceError.DUPLICATE_KEY;
		}
		return ServiceError.INVALID_QUERY;
	}*/

/*	private boolean getBoolean(String value)
	{
		return value.equalsIgnoreCase("y") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
	}

	boolean needsCheckOptimisticConcurrency(URI updURI)
	{
		return modelInfo.needsCheckOptimisticConcurrency(updURI);
	}

	public int getEnumValue(ClientEnumValue enumValue)
	{
		String typeName = enumValue.getTypeName();
		return Integer.parseInt(modelInfo.getModel().getEnumType(new FullQualifiedName(typeName)).getMember(enumValue.getValue()).getValue());
	}

	public String toEnumValue(EdmEnumType type, int value)
	{
		String sValue = Integer.toString(value);
		for(String memberName:type.getMemberNames())
		{
			EdmMember member = type.getMember(memberName);
			if(member.getValue().equals(sValue))
				return member.getName();
		}
		throw new RuntimeException(String.format("Cannot parse enum value %s - %d", type.toString(), value));
	}

	private String entity(String name)
	{
		return modelInfo.entity(name);
	}

	public String entity(EdmEntityType [] fromEntity, String name)
	{
		if(fromEntity == null || fromEntity[0] == null)
			return entity((EdmEntityType)null, name);
		String entityName = entity(fromEntity[0], name);
		if(entityName == null)
			return entity(name);
		EdmNavigationProperty navProp = fromEntity[0].getNavigationProperty(entityName);
		if(navProp != null)
			fromEntity[0] = navProp.getType();
		return entityName;
	}

	public String entity(EdmEntityType fromEntity, String name)
	{
		return modelInfo.entity(fromEntity, name);
	}

	Edm getModel()
	{
		return modelInfo.getModel();
	}
*/
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
