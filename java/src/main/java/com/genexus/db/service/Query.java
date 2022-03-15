package com.genexus.db.service;

import com.genexus.util.NameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Query implements IQuery {
	private static final String [] EMPTY_ARR_STRING = new String [0];
	ServiceDataStoreHelper dataStoreHelper;
	public Query(ServiceDataStoreHelper dataStoreHelper)
	{
		this.dataStoreHelper = dataStoreHelper;
	}

	public String tableName;
	public String[] projection = EMPTY_ARR_STRING;
	public String[] orderBys = EMPTY_ARR_STRING;
	public String[] filters = EMPTY_ARR_STRING;

	private final ArrayList<NameValuePair> mAssignAtts = new ArrayList<>();
	public Iterator<NameValuePair> getAssignAtts() { return mAssignAtts.iterator(); }

	public IODataMap[] selectList = new IODataMap[0];
	private final HashMap<String, VarValue> mVarValues = new HashMap<>();
	public Iterator<VarValue> getVars() { return mVarValues.values().iterator(); }

	public VarValue getParm(String parmName)
	{
		return mVarValues.get(parmName);
	}

	protected final HashMap<Integer, GXType> parmTypes = new HashMap<>();

	public Query For(String tableName)
	{
		this.tableName = tableName;
		return this;
	}

	public Query select(String[] columns)
	{
		this.projection = columns;
		return this;
	}

	public Query orderBy(String []orders)
	{
		orderBys = orders;
		return this;
	}

	public Query orderBy(String order)
	{
		return orderBy(new String[]{ order });
	}

	public Query filter(String[] filters)
	{
		this.filters = filters;
		return this;
	}

	public Query set(String name, String value)
	{
		mAssignAtts.add(new NameValuePair(name, value));
		return this;
	}

	public Query setMaps(IODataMap[] selectList)
	{
		this.selectList = selectList;
		return this;
	}

	protected QueryType queryType = QueryType.QUERY;
	@Override
	public QueryType getQueryType()
	{
		return queryType;
	}

	public Query setType(QueryType queryType)
	{
		this.queryType = queryType;
		return this;
	}

	public Query addConst(GXType gxType, Object parm)
	{
		String parmName = String.format(":const%d", mVarValues.size() + 1);
		mVarValues.put(parmName, new VarValue(parmName, gxType, parm));
		return this;
	}

	public Query setParmType(int parmId, GXType gxType)
	{
		parmTypes.put(parmId, gxType);
		return this;
	}

	public void initializeParms(Object[] parms)
	{
		for(int idx : parmTypes.keySet())
		{
			String parmName = String.format(":parm%d", idx);
			mVarValues.put(parmName, new VarValue(parmName, parmTypes.get(idx), parms[idx]));
		}
	}
}
