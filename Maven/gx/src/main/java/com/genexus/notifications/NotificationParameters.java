package com.genexus.notifications;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import json.org.json.JSONException;
import json.org.json.JSONObject;

public class NotificationParameters {
	private Hashtable<String,String> _data = new Hashtable<String,String>();
	
	public String toJson() {
		JSONObject json = new JSONObject();
		Enumeration<String> e = _data.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement();
			try {
				json.put(key, _data.get(key));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return json.toString();
	}
	
	public void add(String key,String value) {
		_data.put(key, value);
	}
	
	public void setParameters(String key,String value) {
		add(key, value);
	}	
	
	public String valueOf(String key) {
		return _data.get(key);
	}
	
	public Vector getNames() {
		Vector v = new Vector();
		Enumeration<String> e = _data.keys();
		while(e.hasMoreElements())
			v.add(e.nextElement());
		return v;
	}
}
