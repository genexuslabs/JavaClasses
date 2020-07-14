package com.genexus.webpanels.gridstate ;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GridState {
	public GridState(int currentPage, short orderedBy, List<GridStateInputValuesItem> inputValues){
		this();
		this.currentPage =currentPage;
		this.orderedBy = orderedBy;
		if( inputValues!=null)
			this.inputValues = inputValues;
	}
	public GridState(){
		inputValues = new ArrayList<GridStateInputValuesItem>();
	}
	@JsonProperty("CurrentPage")
	protected int currentPage;
	@JsonProperty("OrderedBy")
	protected short orderedBy;
	@JsonProperty("InputValues")
	protected List<GridStateInputValuesItem> inputValues;
}


