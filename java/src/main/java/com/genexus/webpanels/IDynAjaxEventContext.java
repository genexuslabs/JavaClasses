package com.genexus.webpanels;

public interface IDynAjaxEventContext
{
	void Clear();
	void ClearParmsMetadata();
	boolean isInputParm(String key);
	void SetParmHash(String fieldName, Object value);
	boolean isParmModified(String fieldName, Object value);

}