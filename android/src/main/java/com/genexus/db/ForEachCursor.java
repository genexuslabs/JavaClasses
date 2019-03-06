// $Log: ForEachCursor.java,v $
// Revision 1.6  2005/09/08 20:30:34  iroqueta
// Quito el ultimo put porque hacia que la conexion quedara con uncommitedchanges en el caso de las TRNs porque todas los for each cursors tienen currentof.
// Como el problema le daba solo a delarrobla y era muy dificil que pasara se prefiriio sacar el put anterior.
//
// Revision 1.5  2005/05/25 18:50:19  gusbro
// - En los selects con currentof marco uncommited changes en true
//
//

package com.genexus.db;

import java.sql.SQLException;

import com.genexus.CommonUtil;
import com.genexus.DebugFlag;
import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXDBDebug;
import com.genexus.db.driver.GXResultSet;


public class ForEachCursor extends Cursor
{
	boolean currentOf;
	boolean hold = false;

	Object[] buffers;

	GXResultSet rslt;
	IDataStoreHelper parent;
	
	private boolean isForFirst = false;
	private int fetchSize;

	public ForEachCursor(String cursorId, String sqlSentence, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int cacheableLevel, boolean isForFirst)
	{
		this(cursorId, sqlSentence, currentOf, errMask, hold, parent, 0, cacheableLevel, isForFirst);
	}
	
	public ForEachCursor(String cursorId, String sqlSentence, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int fetchSize, int cacheableLevel, boolean isForFirst)
	{
		this(cursorId, sqlSentence, currentOf, errMask);
		this.hold = hold;
		this.parent = parent;
		dynStatement = sqlSentence.equalsIgnoreCase("scmdbuf");
		this.fetchSize = fetchSize;
		this.isForFirst = isForFirst;
	}

	public ForEachCursor(String cursorId, String sqlSentence, boolean currentOf, int errMask)
	{																						
		super(cursorId, sqlSentence, errMask);
		this.currentOf	  		= currentOf;
	}


	public ForEachCursor(String cursorId, String sqlSentence, boolean currentOf)
	{
		this(cursorId, sqlSentence, currentOf, 0);
	}

	public void setOutputBuffers(Object[] buffers)
	{
		this.buffers = buffers;
	}
	
	protected boolean isForFirst()
	{
		return isForFirst;
	}
	
	byte[] preExecute(int cursorNum, AbstractDataStoreProviderBase abstractProvider, AbstractDataSource abstractDS, Object [] params) throws SQLException
	{	
	  	DataStoreProviderBase connectionProvider = (DataStoreProviderBase) abstractProvider;
    	DataSource ds = (DataSource) abstractDS;
		close();
		Object[] dynStmt;
		byte[] hasValues=null;
		final String []sentence = new String[]{ mSQLSentence };
		if(dynStatement)
		{ // Si la sentencia tiene constraints din?micos
			dynStmt = parent.getDynamicStatement(cursorNum, connectionProvider.context, connectionProvider.remoteHandle, connectionProvider.context.getHttpContext(), connectionProvider.getDynConstraints());
			sentence[0] = (String)dynStmt[0];
			hasValues = (byte[])dynStmt[1];
		}
		
		if(parent != null)
		{  // parent puede venir null cuando es un update cursor con autonumber (sqlserver), ver constructor con 3 parametros
			parent.setParametersRT(cursorNum, new RTFieldSetter(ds, sentence), hasValues != null ? getParams(hasValues, params) : params);
		}
		

		mPreparedStatement = SentenceProvider.getPreparedStatement(connectionProvider, mCursorId, sentence[0], currentOf);
		if (fetchSize != 0)
		{
			mPreparedStatement.setFetchSize(fetchSize);
		}
		return hasValues;
	}
	
	private Object [] getParams(byte [] hasValues, Object [] parms)
	{
				Object[] parmsNew = new Object[parms.length + hasValues.length];
				for (int i = 0; i < hasValues.length; i++) {
					parmsNew[i] = new Byte(hasValues[i]);
				}
				System.arraycopy(parms, 0, parmsNew, hasValues.length, parms.length);
				return parmsNew;
	}
	
	void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
	{	
		rslt = (GXResultSet) mPreparedStatement.executeQuery(hold);

		//if(currentOf)
		//{ // Si tengo currentof marco uncommited changes para hacer un rollback dado que en algunos casos
		  // de error (Bantotal) les estaban quedando locks en la bd 
		//	connectionProvider.getConnection().setUncommitedChanges();
		//}
	}

	boolean next(DataSource ds) throws SQLException
	{
  		return rslt.next();
	}

	protected void close() throws SQLException
	{
		if	(rslt != null) 
		{	
			rslt.close();
			rslt = null;
		}
	}

	/**
	* Esto es necesario porque en algunos casos se genera algo del estilo a:
	*		cursor1.execute
	*		while !eof
	*			  cursor2.execute
	*			  ..
	*
	* El cursor2.execute podria o no encontrar datos. Si no encuentra, estaba dejando
	* los valores anteriores del fetch..
	*/
	void clearBuffers()
	{
		for (int i = buffers.length - 1; i >= 0; i--)
		{
			if	(buffers[i] instanceof byte[])
			{
				((byte[]) buffers[i])[0] = 0;
			}
			else if	(buffers[i] instanceof short[])
			{
				((short[]) buffers[i])[0] = 0;
			}
			else if	(buffers[i] instanceof int[])
			{
				((int[]) buffers[i])[0] = 0;
			}
			else if	(buffers[i] instanceof long[])
			{
				((long[]) buffers[i])[0] = 0;
			}
			else if	(buffers[i] instanceof float[])
			{
				((float[]) buffers[i])[0] = 0;
			}
			else if	(buffers[i] instanceof double[])
			{
				((double[]) buffers[i])[0] = 0;
			}
			else if	(buffers[i] instanceof String[])
			{
				((String[]) buffers[i])[0] = "";
			}
			else if	(buffers[i] instanceof java.util.Date[])
			{
				((java.util.Date[]) buffers[i])[0] = CommonUtil.nullDate();
			}
		}
	}

    public boolean hasResult() {  return rslt != null; }
    public boolean isCurrentOf() { return currentOf; }
    class RTFieldSetter implements IFieldSetter
    {
    	private final String []sentence;
    	private final DataSource ds;
    	RTFieldSetter(DataSource ds, String []sentence)
    	{
    		this.ds = ds;
    		this.sentence = sentence;
    	}
    	
	    public void setNull(int index, int sqlType) throws SQLException {}
	    public void setBoolean(int index, boolean value) throws SQLException {}
	    public void setByte(int index, byte value) throws SQLException {}
	    public void setShort(int index, short value) throws SQLException {}
	    public void setInt(int index, int value) throws SQLException {}
	    public void setLong(int index, long value) throws SQLException {}
	    public void setFloat(int index, float value) throws SQLException {}
	    public void setDouble(int index, double value) throws SQLException {}
	    public void setBigDecimal(int index, java.math.BigDecimal value, int decimals) throws SQLException {}
	    public void setBigDecimal(int index, double value, int decimals) throws SQLException {}
	    public void setVarchar(int index, String value) throws SQLException {}
	    public void setVarchar(int index, String value, int length) throws SQLException {}
	    public void setLongVarchar(int index, String value) throws SQLException {}
	    public void setNLongVarchar(int index, String value) throws SQLException {}
	    public void setLongVarchar(int index, String value, int maxLength) throws SQLException {}
	    public void setString(int index, String value, int length) throws SQLException {}
	    public void setString(int index, String value) throws SQLException {}
	    public void setGXDbFileURI(int index, String fileName, String blobPath, int length, String tableName, String fieldName) throws SQLException {}
	    public void setGXDbFileURI(int index, String fileName, String blobPath, int length) throws SQLException {}
	    public void setBytes(int index, byte value[]) throws SQLException {}
	    public void setDateTime(int index, java.util.Date value, boolean onlyTime) throws SQLException {}
	    public void setDateTime(int index, java.util.Date value, boolean onlyTime, boolean onlyDate, boolean hasmilliseconds) throws SQLException {}
	    public void setDate(int index, java.util.Date value) throws SQLException {}
	    public void setDate(int index, java.sql.Date value) throws SQLException {}
	    public void setTime(int index, java.sql.Time value) throws SQLException {}
	    public void setTimestamp(int index, java.sql.Timestamp value) throws SQLException {}
	    public void setBLOBFile(int index, String fileName) throws SQLException {}
	    public void setBLOBFile(int index, String fileName, boolean isMultiMedia) throws SQLException {}
	
	    public void setVarchar(int index, String value, int length, boolean allowsNull) throws SQLException {}
	    public void setLongVarchar(int index, String value, boolean allowsNull) throws SQLException {}
	    public void setNLongVarchar(int index, String value, boolean allowsNull) throws SQLException {}
	    public void setLongVarchar(int index, String value, int maxLength, boolean allowsNull) throws SQLException {}
	    public void setGUID(int index, java.util.UUID value) throws SQLException {}
	    public void setParameterRT(String name, String value)
	    {
				if	(DebugFlag.DEBUG) ds.getLog().log(GXDBDebug.LOG_MAX, "setParameterRT: " + name + " -> " + value);
				boolean isLike = false;
		    if(value.equals("like"))
		    	isLike = true;
		    else if(!value.equals("=") && !value.equals(">") && !value.equals(">=")
		            && !value.equals("<=") && !value.equals("<") && !value.equals("<>"))
		    {
		        value = "=";
		    }
		    String[] parts = sentence[0].split("\'");
		    name = "{{" + name + "}}";
		    StringBuilder nStmt = new StringBuilder();
		    boolean inSQLConstant = false;
		    for(int i = 0; i < parts.length; i++)
		    {
		        if(i != 0)
		        {
		            nStmt.append('\'');
		        }
		        if(!inSQLConstant)
		        {
		        		while(isLike && processLike(parts, i, name, value)){}
		            nStmt.append(parts[i].replace(name, value));
		        }else
		        {
		            nStmt.append(parts[i]);
		        }
		        inSQLConstant = !inSQLConstant;
		    }
		    sentence[0] = nStmt.toString();				
	    }
	    
			String [] concatOp = null;
	    private boolean processLike(String [] parts, int i, String placeholder, String op)
	    {
				// busca el placeholder y si lo encuentra lo cambia por op y adem�s modifica el operando derecho
				// para que donde aparezca una expresion con variables le agregue un '%' al final que es lo que hace
				// el generador al pasar variables del lado derecho de un like
				if(concatOp == null)
					concatOp = ds.concatOp();
				int placeholderIdx = parts[i].indexOf(placeholder);
				if (placeholderIdx == -1)
					return false;
				int parenCount = 0;
				int placeholderLength = placeholder.length();
				int idx = placeholderIdx + placeholderLength;
				int j = i;
				boolean hasVariable = false;
				String currentPart = parts[j];
				while(parenCount >= 0)
				{
					if(idx == placeholderLength)
					{
						j += 2;
						idx = 0;
						if (j < parts.length)
							currentPart = parts[j];
						else break;
						continue;
					}
					switch(currentPart.charAt(idx))
					{
						case '(': parenCount++; break;
						case ')': parenCount--; break;
						case '?':
							hasVariable = true; break;
					}
					idx++;
				}
				if(hasVariable && j < parts.length)
				{
					idx--;
					parts[j] = String.format("%s)%s'%%'%s%s", parts[j].substring(0, idx), concatOp[1], concatOp[2], parts[j].substring(idx));
//					parts[j] = parts[j].substring(0, idx) + ") + '%'" + parts[j].substring(idx);
					op += String.format(" %sRTRIM(", concatOp[0]);
				}
	
				parts[i] = String.format("%s%s%s", parts[i].substring(0, placeholderIdx), op, parts[i].substring(placeholderIdx + placeholderLength));
//				parts[i] = parts[i].substring(0, placeholderIdx) + op + parts[i].substring(placeholderIdx + placeholderLength);
				return true;
	    }
    		
    }
}
