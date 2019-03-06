package com.genexus.search;

import java.util.Vector;


public class GXContentInfo
{
    private String m_entity;
    private String m_viewer;
    private String m_title;
    private String m_id;

    private Vector<String> m_keys = new Vector<String>();

    public GXContentInfo() { }

    public void setType(String value)
    {
            this.m_entity = value;
    }

    public String getType()
    {
            return this.m_entity;
    }
    public String getId()
    {
            return this.m_id;
    }
    public void setId(String value)
    {
            this.m_id = value;
    }

    public void setViewer(String value)
    {
            this.m_viewer = value;
    }

    public String getViewer()
    {
            return this.m_viewer;
    }

    public void addkey(String value)
    {
        m_keys.add(value);
    }
    public Vector<String> getKeys()
    {
            return this.m_keys;
    }
    public void setKeys(Vector<String> value)
    {
            this.m_keys = value;
    }
    public void setTitle(String value)
    {
            this.m_title = value;
    }
    public String getTitle()
    {
            return this.m_title;
    }

}
