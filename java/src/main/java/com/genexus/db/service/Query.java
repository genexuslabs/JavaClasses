package com.genexus.db.service;

import java.util.Collections;

public class Query implements IQuery {
	private static final String [] EMPTY_ARR_STRING = new String [0];
	ServiceDataStoreHelper dataStoreHelper;
	public Query(ServiceDataStoreHelper dataStoreHelper)
	{
		this.dataStoreHelper = dataStoreHelper;
	}

	private String tableName;
	public String[] projection = EMPTY_ARR_STRING;
/*
	public string[] OrderBys { get; set; } = Array.Empty<string>();
	public string[] Filters { get; set; } = Array.Empty<string>();
	private List<KeyValuePair<string, string>> mAssignAtts;
	public IEnumerable<KeyValuePair<string, string>> AssignAtts { get { return mAssignAtts ?? Array.Empty<KeyValuePair<string, string>>() as IEnumerable<KeyValuePair<string, string>>; } }
*/
	public IODataMap[] selectList = new IODataMap[0];
/*
	private List<VarValue> mVarValues;
	public IEnumerable<VarValue> Vars { get { return (mVarValues ?? Array.Empty<VarValue>() as IEnumerable<VarValue>); } }
	public CursorType CursorType { get; set; } = CursorType.Select;
 */

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
/*
	public Query OrderBy(string[] orders)
	{
		OrderBys = orders;
		return this;
	}

	public Query Filter(string[] filters)
	{
		Filters = filters;
		return this;
	}

	public Query Set(string name, string value)
	{
		mAssignAtts = mAssignAtts ?? new List<KeyValuePair<string, string>>();
		mAssignAtts.Add(new KeyValuePair<string, string>(name, value));
		return this;
	}
*/
	public Query setMaps(IODataMap[] selectList)
	{
		this.selectList = selectList;
		return this;
	}

	@Override
	public QueryType getQueryType() {
		return QueryType.QUERY;
	}
/*

	public Query SetType(CursorType cType)
	{
		CursorType = cType;
		return this;
	}

	public Query AddParm(GXType gxType, object parm)
	{
		mVarValues = mVarValues ?? new List<VarValue>();
		mVarValues.Add(new VarValue($":parm{ mVarValues.Count + 1 }", gxType, parm));
		return this;
	}
*/
}
