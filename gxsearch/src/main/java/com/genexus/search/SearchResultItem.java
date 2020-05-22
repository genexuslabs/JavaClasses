package com.genexus.search;

import java.util.Date;
import java.util.List;
import com.genexus.internet.StringCollection;

import json.org.json.IJsonFormattable;
import json.org.json.JSONException;
import json.org.json.JSONObject;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import com.genexus.internet.IGxJSONAble;
import java.io.Serializable;

@SuppressWarnings("deprecation")
public class SearchResultItem implements ISearchResultItem, Serializable, IGxJSONAble
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Document m_document;
	private float m_score;
    private JSONObject _Properties;

    public SearchResultItem() {
        _Properties = new JSONObject();
    }

	protected SearchResultItem(Document doc, float score)
	{
            _Properties = new JSONObject();
		m_document = doc;
		m_score = score;
	}
        public JSONObject GetProperties()
        {
            return _Properties;
        }
        public void Clear()
        {
            _Properties.clear();
        }

	public String getId()
	{
		return m_document.getField(IndexRecord.URIFIELD).stringValue();
	}

	public String getViewer()
	{
		return m_document.getField(IndexRecord.VIEWERFIELD).stringValue();
	}

	public String getTitle()
	{
		return m_document.getField(IndexRecord.TITLEFIELD).stringValue();
	}

	public String getType()
	{
		return m_document.getField(IndexRecord.ENTITYFIELD).stringValue();
	}

	public Date getTimestamp()
	{
		// We need to still using this for all indexes already created with this kind of fields.
		return DateField.stringToDate(m_document.getField(IndexRecord.TIMESTAMPFIELD).stringValue());
	}

	public float getScore()
	{
		return m_score;
	}

	public StringCollection getKey()
	{
		@SuppressWarnings("unchecked")
		List<Field> fields = m_document.getFields();
		int size = fields.size();
		StringCollection keys = new StringCollection();
		for (int i = 0; i < size; i++)
		{
			Field f = (Field)fields.get(i);
			if (f.name().startsWith(IndexRecord.KEYFIELDPREFIX))
				keys.add(f.stringValue());
		}
		return keys;
	}
        public String ToJavascriptSource()
        {
                return GetJSONObject().toString();
        }
        public Object GetJSONObject(boolean includeState)
        {
			return GetJSONObject();
        }
        public Object GetJSONObject()
        {
            tojson();
            return _Properties;
        }

        public void FromJSONObject(IJsonFormattable obj) {
        }
        public void AddObjectProperty(String name, Object prop)
        {
            try
            {
                 _Properties.put(name, prop);
            }
            catch (JSONException e) {}
        }
        public void tojson()
        {
            AddObjectProperty("Id", getId());
            AddObjectProperty("Viewer", getViewer());
            AddObjectProperty("Title", getTitle());
            AddObjectProperty("Type", getType());
            AddObjectProperty("Score", Float.valueOf(getScore()));
            AddObjectProperty("Timestamp", getTimestamp());
        }

}
