package com.genexus.json;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;

public class JSONTokenerWrapper extends JSONTokener{

	public JSONTokenerWrapper(String string) {
		super(string);
	}

	public Object nextValue() throws JSONException {
		char c = this.nextClean();
		this.back();
		if (c == '{') {
			try {
				return new JSONObjectWrapper((JSONObject) super.nextValue());
			} catch (StackOverflowError e) {
				throw new JSONException("JSON Array or Object depth too large to process.", e);
			}
		}
		else
			return super.nextValue();
	}
}
