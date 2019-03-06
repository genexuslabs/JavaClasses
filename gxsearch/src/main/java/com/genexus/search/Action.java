package com.genexus.search;


public class Action 
{
	public static final int INSERT = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;
	
	private int m_actionType;
	private IndexRecord m_record;
	
	protected Action(int actionType, IndexRecord record)
	{
		this.m_actionType = actionType;
		this.m_record = record;
	}
	
	protected int getActionType()
	{
		return this.m_actionType;
	}
	
	protected IndexRecord getRecord()
	{
		return this.m_record;
	}
}
