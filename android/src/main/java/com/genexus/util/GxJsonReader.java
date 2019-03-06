package com.genexus.util;

import com.google.gson.stream.JsonReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.CharEncoding;
import com.genexus.internet.StringCollection;

public class GxJsonReader {
	private JsonReader reader;

	public GxJsonReader(InputStream stream) {
		try {
			reader = new JsonReader(new InputStreamReader(stream, CharEncoding.UTF_8));
		} catch (UnsupportedEncodingException e) {
			//TODO
		}
	}

	public boolean readBeginArray() {
		boolean result;
		try {
			reader.beginArray();
			result = true;
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	public boolean readEndArray() {
		boolean result;
		try {
			reader.endArray();
			result = true;
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	public boolean endOfArray() {
		boolean result;
		try {
			result = !reader.hasNext();
		} catch (IOException e) {
			result = true;
		}
		return result;
	}

	public StringCollection readNextStringCollection() {
		StringCollection stringCollection = new StringCollection();
		
		try {
			reader.beginArray();
			while (reader.hasNext()) {
				String field = reader.nextString();
				stringCollection.add(field);
			}
			reader.endArray();
		} catch (IOException e) {
			// TODO
		}
		
		return stringCollection;
	}

	public StringCollection parseStringCollection() {
		StringCollection stringCollection = new StringCollection();
		
		try {
			reader.beginArray();
			while (reader.hasNext()) {
				String field = reader.nextString();
				stringCollection.add(field);
			}
			reader.endArray();
		} catch (IOException e) {
			// TODO
		}
		
		return stringCollection;
	}
}