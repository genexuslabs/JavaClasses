package com.genexus.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.nio.charset.StandardCharsets;

import com.genexus.internet.StringCollection;
import com.google.gson.stream.JsonReader;

public class GxJsonReader {
	private JsonReader reader;

	public GxJsonReader(InputStream stream) {
		reader = new JsonReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
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