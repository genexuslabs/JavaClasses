package com.artech.synchronization;

import java.util.TreeMap;

import json.org.json.JSONArray;

public interface ISynchronizationHelper {

	// methods to sync from procs
	public short syncSend();
	public short syncReceive();
	public short syncStatus();
	public short syncCheckPoint();
	
	// methods to sync blobs (send) from BCs
	public void processBCBlobsBeforeSaved(String mBCName, String mData, String mDataOlds, TreeMap<String, String> mMapFilesToSend);
	public String replaceBCBlobsAfterSave(String mBCName, String mAction, String mData, String mDataOlds, TreeMap<String, String> mMapFilesToSend, JSONArray mArrayFilesToSend);
	
	
	
}
