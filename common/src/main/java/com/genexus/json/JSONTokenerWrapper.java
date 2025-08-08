package com.genexus.json;

import org.json.JSONTokener;
import org.json.JSONException;

public class JSONTokenerWrapper extends JSONTokener{
	private String mySource;
	private int myIndex;

	public JSONTokenerWrapper(String string) {
		super(string);
		mySource = string;
		myIndex = 0;
	}

	public void back() {
		if (myIndex > 0) {
			myIndex -= 1;
		}
	}

	public boolean more() {
		return myIndex < mySource.length();
	}

	public char next() {
		if (more()) {
			char c = mySource.charAt(myIndex);
			this.myIndex += 1;
			return c;
		}
		return 0;
	}

	public Object nextValue() throws JSONException {
		char c = this.nextClean();
		this.back();
		if (c == '{') {
			try {
				return new JSONObjectWrapper(this);
			} catch (StackOverflowError e) {
				throw new JSONException("JSON Array or Object depth too large to process.", e);
			}
		}
		else
			return super.nextValue();
	}
}
