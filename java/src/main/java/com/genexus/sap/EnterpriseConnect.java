package com.genexus.sap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.text.DecimalFormat;
import com.genexus.GXSimpleCollection;
import com.genexus.ModelContext;
import com.genexus.internet.IGxJSONAble;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public class EnterpriseConnect
{
	static String DESTINATION_NAME = "SAP_SERVER";
    JCoFunction function = null;
	String destinationName = DESTINATION_NAME;
	
	public EnterpriseConnect(ModelContext context)
	{
		Object destination = context.getContextProperty("SAPSessionName");
		if (destination !=null)
		{
			destinationName = (String)destination;
		}
	}
	
	/*        ---   Set Parameters   ---           */	
	public void setValue(String parameterName, String value)
	{
		boolean setvalue = false;
		if (value != null && (!value.trim().equals("")))
		{
			setvalue = true;
			function.getImportParameterList().setValue(parameterName, value);
		}
		if (setvalue)
		{
			function.getImportParameterList().setActive(parameterName, true);
		}
		else
		{
			function.getImportParameterList().setActive(parameterName, false);
		}
	}

	public void setValue(String parameterName, int value)
	{
		function.getImportParameterList().setValue(parameterName, value);
		function.getImportParameterList().setActive(parameterName, true);
	}
	
	public void setValue(String parameterName, long value)
	{
		function.getImportParameterList().setValue(parameterName, value);
		function.getImportParameterList().setActive(parameterName, true);
	}

	public void setValue(String parameterName, BigDecimal value)
	{
		function.getImportParameterList().setValue(parameterName, value);
		function.getImportParameterList().setActive(parameterName, true);
	}

	public void setValue(String parameterName, double value)
	{
		function.getImportParameterList().setValue(parameterName, value);
		function.getImportParameterList().setActive(parameterName, true);
	}

	public void setValue(String parameterName, Date value)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(0, 0, 0);
	    Date baseDate = calendar.getTime();
		if (value != null && value.after(baseDate))
		{
			function.getImportParameterList().setValue(parameterName, value);
			function.getImportParameterList().setActive(parameterName, true);
		}
		else
		{
			function.getImportParameterList().setActive(parameterName, false);
		}
	}
	
	// I/O parameter
	
	public void setValue(String parameterName, GXSimpleCollection[] value, Boolean inOut)
	{	
		if (value != null  && value.length > 0)
		{
			setValue( parameterName, value[0], inOut);
		}
	}
	
	
	public void setValue(String parameterName, GXSimpleCollection[] value)
	{
		if (value != null  && value.length > 0)
		{
			setValue( parameterName, value[0]);
		}
	}
	
	public void setValue(String parameterName, IGxJSONAble[] value)
	{
		if (value != null  && value.length > 0)
		{
			setValue( parameterName, value[0]);
		}
	}
	
	//  ------
	public void setValue(String parameterName, GXSimpleCollection value)
	{
		setValue(parameterName,  value, false);
	}
	
	public void setValue(String parameterName, GXSimpleCollection value, Boolean inOut)
	{
		
		JCoTable jTable = function.getTableParameterList().getTable(parameterName);	
        Boolean setValues = false;
		try{
			for (int i = 1; i <= value.getItemCount(); i++)
			{
				IGxJSONAble item = (IGxJSONAble)value.item(i);
				if (item != null)
				{
					setValues = true;
					JSONObject jObj = (JSONObject ) item.GetJSONObject();
					jTable.appendRow();
					jTable.lastRow();
					Iterator<?> keys = jObj.keys();
					while( keys.hasNext() )
					{
						String key = (String)keys.next();
						int jcoType = jTable.getRecordMetaData().getType(key);
						//int len = jTable.getRecordMetaData().getLength(key);
						int dec = jTable.getRecordMetaData().getDecimals(key);

						if( jObj.get(key) instanceof String )
						{
							String  obj_value = jObj.getString(key);
							if (jcoType == JCoMetaData.TYPE_NUM && dec == 0  && ( ! obj_value.trim().equals("") ))
							{								
								String sValue = new DecimalFormat("#").format(new BigDecimal(obj_value)); 							
								jTable.setValue(key, sValue);
							}
							else {
								jTable.setValue(key, obj_value);
							}
						}						
						else if (jcoType == JCoMetaData.TYPE_NUM ||  jcoType == JCoMetaData.TYPE_INT)
						{							
							jTable.setValue(key, jObj.getLong(key));
						}
						else if (jcoType == JCoMetaData.TYPE_FLOAT || jcoType == JCoMetaData.TYPE_BCD)
						{
							jTable.setValue(key, jObj.getDouble(key));
						}
						else if (jcoType == JCoMetaData.TYPE_DATE)
						{
							jTable.setValue(key, jObj.getString(key));
						}
						else if (jcoType == JCoMetaData.TYPE_INT2 || jcoType == JCoMetaData.TYPE_INT1 
							      || jcoType == JCoMetaData.TYPE_BYTE)
						{
							jTable.setValue(key, jObj.getInt(key));
						}
						else
						{
							System.out.println( key +  " Invalid Type " +  Integer.toString(jcoType));
						}
					}
				}
			}
		}
		catch( JSONException ex)
		{
			throw new RuntimeException(ex.toString());
		}
		if (setValues)
		{			
			function.getTableParameterList().setActive(parameterName, true);
		}
		else
		{			
			function.getTableParameterList().setActive(parameterName, inOut);
		}
	}
	
	public void setValue(String parameterName, IGxJSONAble value)
	{
		try
		{
			JCoStructure jStruct = function.getImportParameterList().getStructure(parameterName);	
			if (value != null)
			{
				JSONObject jObj = (JSONObject ) value.GetJSONObject();		
				Iterator<?> keys = jObj.keys();
				while( keys.hasNext() )
				{
					String key = (String)keys.next();
					int jcoType = jStruct.getMetaData().getType(key);	
				
					if( jObj.get(key) instanceof String )
					{
						jStruct.setValue(key, jObj.getString(key));
					}					
					else if (jcoType == JCoMetaData.TYPE_NUM ||  jcoType == JCoMetaData.TYPE_INT)
					{
						jStruct.setValue(key, jObj.getLong(key));
					}
					else if (jcoType == JCoMetaData.TYPE_FLOAT || jcoType == JCoMetaData.TYPE_BCD)
					{
						jStruct.setValue(key, jObj.getDouble(key));
					}
					else if (jcoType == JCoMetaData.TYPE_DATE)
					{
						jStruct.setValue(key, jObj.getString(key));
					}
					else if (jcoType == JCoMetaData.TYPE_INT2 || jcoType == JCoMetaData.TYPE_INT1 
					      || jcoType == JCoMetaData.TYPE_BYTE)
					{
						jStruct.setValue(key, jObj.getInt(key));
					}
					else
					{
						System.out.println( key +  " Invalid Type " +  Integer.toString(jcoType));
					}	
				}
				function.getImportParameterList().setActive(parameterName, true);	
		    }						
			else
			{
				function.getImportParameterList().setActive(parameterName, false);
			}			
		}
		catch( JSONException ex)
		{
			throw new RuntimeException(ex.toString());
		}		
	}
	
	
	/*        ---   Get Return Values  ---           */
	
	public void getValue(String parameterName, GXSimpleCollection[] value)
	{	
	    if (value.length != 0)
		{
			GXSimpleCollection col = value[0];		
			col.clear();
			JCoTable tbl = function.getTableParameterList().getTable(parameterName);
			JSONArray jCol = new JSONArray();
			try
			{
				for(int i = 0; i <  tbl.getNumRows(); i++)
				{					
					JSONObject jRow = new JSONObject();
					tbl.setRow(i);
					for(JCoField field : tbl)
					{
						if  (field.getType() == JCoMetaData.TYPE_INT || 
						    (field.getType() == JCoMetaData.TYPE_NUM  && field.getDecimals() == 0))
						{
							jRow.put(field.getName(), field.getLong());
						}
						else if (field.getType() == JCoMetaData.TYPE_INT2 || field.getType() == JCoMetaData.TYPE_INT1 
								|| field.getType() == JCoMetaData.TYPE_BYTE )
						{
							jRow.put(field.getName(), field.getInt());
						}
						else if ( field.getType() == JCoMetaData.TYPE_NUM || 
								field.getType() == JCoMetaData.TYPE_FLOAT ||
    	    	            	field.getType() == JCoMetaData.TYPE_BCD )
						{
								jRow.put(field.getName(), field.getDouble());
						}
						else
						{
								jRow.put(field.getName(), field.getString());
						}										
					}
					jCol.put(jRow);
				}		
				col.FromJSONObject(jCol);
			}
			catch(JSONException ex)
			{
				throw new RuntimeException(ex.toString());
			}
		}
	}			
	
	public void getValue(String parameterName, IGxJSONAble[] value)
	{
		try
		{
			IGxJSONAble struct = value[0];
			JCoStructure jStruct = function.getExportParameterList().getStructure(parameterName);				
			JSONObject jRow = new JSONObject();
			for(JCoField field : jStruct)
			{
			   if ( field.getType() == JCoMetaData.TYPE_NUM || 
				     field.getType() == JCoMetaData.TYPE_FLOAT ||
                     field.getType() == JCoMetaData.TYPE_BCD )
				{
					jRow.put(field.getName(), field.getDouble());
				}
				else
				{
					jRow.put(field.getName(), field.getString());
				}					
			}
			struct.FromJSONObject(jRow);
		}
		catch(JSONException ex)
		{
			throw new RuntimeException(ex.toString());
		}
	}
	
	public void getValue(String parameterName, String[] value)
	{		
		value[0] = function.getExportParameterList().getString(parameterName);
	}		
	
	public void getValue(String parameterName, java.util.Date[] value)
	{		
		value[0] = function.getExportParameterList().getDate(parameterName);
	}		
	
	public void getValue(String parameterName, int[] value)
	{		
		value[0] = function.getExportParameterList().getInt(parameterName);
	}
	
	public void getValue(String parameterName, long[] value)
	{		
		value[0] = function.getExportParameterList().getLong(parameterName);
	}
	
	public void getValue(String parameterName, float[] value)
	{		
		value[0] = function.getExportParameterList().getFloat(parameterName);
	}
	
	public void getValue(String parameterName, double[] value)
	{		
		value[0] = function.getExportParameterList().getDouble(parameterName);
	}
	
	public void getValue(String parameterName, BigDecimal[] value)
	{		
		value[0] = function.getExportParameterList().getBigDecimal(parameterName);
	}

	public int getIntValue(String parameterName)
	{		
		return function.getExportParameterList().getInt(parameterName);
	}
	
	/*        ---   Execute  ---           */
	
	public void executeStart(String functionName)     
	{
	   try
           {
			executeStart(functionName, false);
			}
        catch (JCoException e)
            {
                throw new RuntimeException(e.toString());    
            }
	}
	
	public void executeStart(String functionName, boolean isTransaction) throws JCoException
	{
		try
		{			
			JCoDestination destination = JCoDestinationManager.getDestination(destinationName);
			if (isTransaction)
			{
				JCoContext.begin(destination);		
			}
			function = destination.getRepository().getFunction(functionName);	
			if (function == null) 
			{
				throw new RuntimeException( functionName + " not found in SAP");
			}	
		}
        catch (AbapException e)
        {
		    throw new RuntimeException(e.toString());
        }
	} 

    public int executeFunction(String functionName)  
    {
		try
        {
			return executeFunction( functionName,false);
		}
		catch (JCoException e)
        {
            throw new RuntimeException(e.toString());    
        }
    }
	
    public int executeFunction(String functionName, boolean isTransaction) throws JCoException
    {   
        try
			{				
				JCoDestination destination = JCoDestinationManager.getDestination(destinationName);
				function.execute(destination);
				if (isTransaction)
                {
                    JCoFunction commitFnc = destination.getRepository().getFunction("BAPI_TRANSACTION_COMMIT");                    
                    commitFnc.execute(destination);
                    JCoContext.end(destination);
                }
        }
        catch (AbapException e)
        {
            throw new RuntimeException(e.toString());
        }
        return 0;
    }
}