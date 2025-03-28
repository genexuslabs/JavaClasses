package com.genexus.specific.android;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.genexus.json.JSONObjectWrapper;

public class AndroidJSONTokenerWrapper extends JSONTokener {
	public AndroidJSONTokenerWrapper(String string) {
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
