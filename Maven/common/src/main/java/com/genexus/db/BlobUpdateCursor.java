/**
 * $Log: BlobUpdateCursor.java,v $
 * Revision 1.3  2005/11/28 14:06:51  gusbro
 * - Se estaban 'perdiendo' cursores
 *
 * Revision 1.2  2005/09/02 23:19:43  gusbro
 * - Agrego soporte para grabar nulos en los campos blob de Oracle
 *
 * Revision 1.1  2004/03/17 20:04:02  gusbro
 * - Agrego soporte de blobs para Oracle
 *
 */
package com.genexus.db;

import java.util.*;

import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.common.classes.IGXPreparedStatement;

import java.sql.SQLException;

public class BlobUpdateCursor extends UpdateCursor
{
	private static final int BLOB_CURSOR_IDX_BASE = 65536;
	private static final String BLOB_SEL_ID = "Sel";
	private static final String BLOB_SET_NULL_ID = "SetNull";
	private String blobStmt2;
	private String type;
	private int cantNoBlobParms;
	private int cursorIdx;
	private Object [] params;
	
    public BlobUpdateCursor(String cursorId, String blobStmt1, String blobStmt2, String type, int cantNoBlobParms, int errMask, String tableName)
    {
		this(cursorId, blobStmt1, blobStmt2, type, cantNoBlobParms, errMask);
    }

	public BlobUpdateCursor(String cursorId, String blobStmt1, String blobStmt2, String type, int cantNoBlobParms, int errMask)
	{
		super(cursorId, blobStmt1, errMask);
		this.blobStmt2 = blobStmt2;
		this.type = type;
		this.cantNoBlobParms = cantNoBlobParms;
	}
	
	byte[] preExecute(int cursorNum, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds, Object [] params) throws SQLException
	{		
		if(type.equalsIgnoreCase("ins"))
		{
			preExecuteInsert(cursorNum, connectionProvider, ds, params);
		}
		else
		{
			preExecuteUpdate(cursorNum, connectionProvider, ds, params);
		}
		return null;
	}
	
	private void preExecuteInsert(int cursorNum, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds, Object [] params) throws SQLException
	{
		mPreparedStatement = (IGXPreparedStatement) SentenceProvider.getCallableStatement(connectionProvider, mCursorId, mSQLSentence);
		((IGXCallableStatement)mPreparedStatement).registerOutParameter(cantNoBlobParms + 1, java.sql.Types.CHAR);		
		// Avisamos al stmt que mantenga los nombres de los blobs
		mPreparedStatement.skipSetBlobs(true);
	}
	
	private void preExecuteUpdate(int cursorNum, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds, Object [] params) throws SQLException
	{
		super.preExecute(cursorNum, connectionProvider, ds, params);
		this.cursorIdx = cursorNum;
		this.params = params;
		mPreparedStatement.skipSetBlobs(true);
	}
	
	void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
	{
		if(type.equalsIgnoreCase("ins"))
		{
			postExecuteInsert(connectionProvider, ds);
		}
		else
		{
			postExecuteUpdate(connectionProvider, ds);
		}
	}
	
	void postExecuteInsert(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
	{
		mPreparedStatement.executeUpdate(); 
		mPreparedStatement.skipSetBlobs(false);
		
		// Ahora debo modificar el contenido del blob
		IGXPreparedStatement selStmt = SentenceProvider.getPreparedStatement(connectionProvider, mCursorId + BLOB_SEL_ID, blobStmt2, false);
		
		// Seteo el argumento (el rowId) y ejecuto el select
		String rowId = ((IGXCallableStatement)mPreparedStatement).getString(cantNoBlobParms + 1);
		selStmt.setString(1, rowId);
		IGXResultSet resultSet = (IGXResultSet)selStmt.executeQuery();
		resultSet.next();
		updateBlobs(connectionProvider, resultSet, selStmt, rowId);
	}
	
	void postExecuteUpdate(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
	{
		mPreparedStatement.executeUpdate();		
		mPreparedStatement.skipSetBlobs(false);
		
		// Ahora debo actualizar los blobs
		IGXPreparedStatement selStmt = SentenceProvider.getPreparedStatement(connectionProvider, mCursorId + BLOB_SEL_ID, blobStmt2, false);
		((ILocalDataStoreHelper)connectionProvider.getHelper()).setParameters(cursorIdx + BLOB_CURSOR_IDX_BASE, (IFieldSetter) selStmt, params);

		IGXResultSet resultSet = (IGXResultSet)selStmt.executeQuery();
		resultSet.next();
		
		// Aqui es donde realmente actualizo el stream de los blobs
		updateBlobs(connectionProvider, resultSet, selStmt, null);
	}
	
	private void updateBlobs(AbstractDataStoreProviderBase connectionProvider, IGXResultSet resultSet, IGXPreparedStatement selStmt, String rowId) throws SQLException
	{
		int countSetNulls = 0;
		
		// Ok, ahora debo setear los args del update de los blobs
		String [] blobFiles = mPreparedStatement.getBlobFiles();
		for(int index = 1;index <= blobFiles.length; index++)
		{
			if(blobFiles[index-1] == null)
			{ // Si el nombre del blob esta en nulo es porque quiero hacer un setNull()
				countSetNulls++;
			}
			else
			{
				selStmt.setBLOBFile(resultSet.getBlob(index), blobFiles[index-1].substring(blobFiles[index-1].indexOf(',')+1));
			}
		}
		
		resultSet.close();
		connectionProvider.getConnection().dropCursor(selStmt);
		
		// Hasta aqui seteamos los blobs con contenido. Nos falta setear los que quedan en Null
		// Esto lo hacemos con una sentence update Table set Att=null
		
		if(countSetNulls != 0)
		{
			String tmp1 = blobStmt2.substring(blobStmt2.indexOf(" FROM ") + 6);
			String tmp2 = tmp1.substring(tmp1.indexOf(" WHERE "));
			String tmp3 = blobStmt2.substring(7, blobStmt2.length() - tmp1.length() - 6);			
			String tmp4 = tmp2.substring(0, tmp2.indexOf(" FOR UPDATE"));
			String setNullBlobStmt = "UPDATE " + tmp1.substring(0, tmp1.length() - tmp2.length()) + " SET ";
			StringTokenizer tokenizer = new StringTokenizer(tmp3, ",", false);
			for(int index = 1; index <= blobFiles.length; index++)
			{
				String nextToken = tokenizer.nextToken();
				if(blobFiles[index-1] == null)
				{
					setNullBlobStmt += nextToken + " = NULL";
					countSetNulls--;
					if(countSetNulls != 0)
					{
						setNullBlobStmt += ", ";
					}
				}
			}
			
			setNullBlobStmt += tmp4;
			
			IGXPreparedStatement setNullStmt = SentenceProvider.getPreparedStatement(connectionProvider, mCursorId + BLOB_SET_NULL_ID, setNullBlobStmt, false);
			((ILocalDataStoreHelper)connectionProvider.getHelper()).setParameters(cursorIdx + BLOB_CURSOR_IDX_BASE, (IFieldSetter)setNullStmt, params);
			if(rowId != null)
			{
				setNullStmt.setString(1, rowId);
			}
			setNullStmt.execute();
			connectionProvider.getConnection().dropCursor(setNullStmt);
			
			connectionProvider.getConnection().dropCursor(mPreparedStatement);
		}
	}
}
