package com.genexus.db.dynamodb;

import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.service.ServicePreparedStatement;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DynamoDBPreparedStatement extends ServicePreparedStatement
{
	final DynamoQuery query;
	final ServiceCursorBase cursor;

	DynamoDBPreparedStatement(Connection con, DynamoQuery query, ServiceCursorBase cursor, Object[] parms, GXConnection gxCon)
	{
		super(con, parms, gxCon);
		this.query = query;
		this.cursor = cursor;
		query.initializeParms(parms);
	}

	@Override
	public ResultSet executeQuery() throws SQLException
	{
		return new DynamoDBResultSet(this);
	}

	DynamoDbClient getClient() throws SQLException
	{
		return ((DynamoDBConnection)getConnection()).mDynamoDB;
	}

	/// JDK8
	@Override
	public void closeOnCompletion()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isCloseOnCompletion()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
