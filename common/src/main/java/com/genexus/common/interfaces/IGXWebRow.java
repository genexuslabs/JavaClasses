package com.genexus.common.interfaces;

public interface IGXWebRow {
	IGXWebGrid getParentGrid();
	void AddHidden(String name, Object value);
}
