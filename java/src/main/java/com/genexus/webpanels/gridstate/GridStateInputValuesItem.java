package com.genexus.webpanels.gridstate ;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GridStateInputValuesItem {
	public GridStateInputValuesItem(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public GridStateInputValuesItem() {
	}
	@JsonProperty("Name")
	protected String name;
	@JsonProperty("Value")
	protected String value;
}

