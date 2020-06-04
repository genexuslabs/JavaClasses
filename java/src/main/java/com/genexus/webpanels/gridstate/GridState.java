package com.genexus.webpanels.gridstate ;

import java.util.ArrayList;
import java.util.List;

public class GridState {
	public GridState(int currentPage, short orderedBy, List<GridStateInputValuesItem> inputValues){
		this();
		CurrentPage =currentPage;
		OrderedBy = orderedBy;
		if( inputValues!=null)
			InputValues = inputValues;
	}
	public GridState(){
		InputValues = new ArrayList<GridStateInputValuesItem>();
	}
	protected int CurrentPage;
	protected short OrderedBy;
	protected List<GridStateInputValuesItem> InputValues;
}


