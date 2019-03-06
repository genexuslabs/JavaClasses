package com.genexus.search;

import java.util.Vector;
import java.io.*;


public class ActionBuffer 
{
	private Vector m_buff = new Vector();
	private Object m_lock = new Object();
	private int m_maxsize;
	
	public ActionBuffer(int maxsize)
	{
		m_maxsize = maxsize;
	}
	public void addAction(Action action)
	{
		synchronized(m_lock)
		{
			m_buff.add(action);
		}
		try{
			while (m_buff.size() > m_maxsize)
			{
				Thread.sleep(50);
			}
		}
		catch(Exception ex)
		{
		}
	}
	
	public Action getAction()
	{
		synchronized(m_lock)
		{
			if(m_buff.size()>0)
			{
				Action action = (Action)m_buff.elementAt(0);
				m_buff.removeElementAt(0);
				return action;
			}
		}
		return null;
	}
	
	public int getCount()
	{
		synchronized(m_lock)
		{
			return m_buff.size();
		}
	}
}
