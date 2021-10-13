package com.genexus.search;

import java.util.Vector;

public class IndexRecord
{
	public final static String URIFIELD = "URI";
	public final static String VIEWERFIELD = "VIEWER";
	public final static String TITLEFIELD = "TITLE";
	public final static String CONTENTFIELD = "CONTENT";
	public final static String ENTITYFIELD = "ENTITY";
	public final static String TIMESTAMPFIELD = "TIME";
	public final static String KEYFIELDPREFIX = "KEY";

	private String m_uri;
	private String m_entity;
	private String m_content;
	private String m_viewer;
	private String m_title;
	private String[] m_keys = new String[0];

	public IndexRecord() { }
	public IndexRecord(String URI)
	{
		this(URI, "", "", "", "", new Vector<String>());
	}
	public IndexRecord(String uri, String entity, String content, String title, String viewer, Vector<String> vectorKeys)
	{
		this.m_uri = uri;
		this.m_entity = entity;
		this.m_content = content;
		if (vectorKeys != null)
		{
			this.m_keys = new String[vectorKeys.size()];
			for (int i = 0; i < vectorKeys.size(); i++)
			{
				this.m_keys[i] = vectorKeys.get(i);
			}
		}
		this.m_title = title;
		this.m_viewer = viewer;
	}

	protected String getUri()
	{
		return this.m_uri;
	}

	protected void setUri(String value)
	{
		this.m_uri = value;
	}
	protected String getEntity()
	{
		return this.m_entity;
	}
	protected void setEntity(String value)
	{
		this.m_entity = value;
	}

	protected String getContent()
	{
		return this.m_content;
	}
	protected void setContent(String value)
	{
		this.m_content = value;
	}

	protected String[] getKeys()
	{
		return this.m_keys;
	}
	protected void setKeys(Vector<String> vectorKeys)
	{
		this.m_keys = new String[vectorKeys.size()];
		for (int i = 0; i < vectorKeys.size(); i++)
		{
			this.m_keys[i] = vectorKeys.get(i);
		}
	}

	protected String getViewer()
	{
		return this.m_viewer;
	}
	protected void setViewer(String value)
	{
		this.m_viewer = value;
	}

	protected String getTitle()
	{
		return this.m_title;
	}
	protected void setTitle(String value)
	{
		this.m_title = value;
	}
	
	public static String processContent(String content)
    {
		if (Settings.getInstance().getAnalyzer().equalsIgnoreCase(Settings.WhitespaceAnalyzer))
		{
			return content.toLowerCase();
		}
		else
			return content;

    }

}
