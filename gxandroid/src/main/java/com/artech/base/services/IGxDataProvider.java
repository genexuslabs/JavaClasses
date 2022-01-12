package com.artech.base.services;

public interface IGxDataProvider
{
	//use to call dp with data with no list
	// use  GxUri: getParameters() , getOrder(), getSearchText() to get parameters
	public IPropertiesObject execute(IGxUri gxUri, int sessionId, int start, int count);
}
