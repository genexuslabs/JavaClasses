package com.genexus.cloud.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class EventMessagesList {
	@JsonProperty("items")
	List<String> items = new ArrayList<String>();

	public void addItem(String item) {
		items.add(item);
	}
}
