package com.artech.base.synchronization;

import java.io.InputStream;
import java.util.LinkedHashMap;

import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.internet.StringCollection;
import com.genexus.util.GxJsonReader;

public abstract class GXOfflineDatabase extends GXProcedure {
	private GxJsonReader reader;
	private LinkedHashMap<String, String> tablesChecksum;
	private Integer errorCode;

	public void initialize() {
	}

	public GXOfflineDatabase(int remoteHandle, ModelContext context, String location) {
		super(remoteHandle, context, location);
		tablesChecksum = new LinkedHashMap<String, String>();
	}

	public GXOfflineDatabase(boolean inNewUTL, int remoteHandle, ModelContext context, String location) {
		super(inNewUTL, remoteHandle, context, location);
		tablesChecksum = new LinkedHashMap<String, String>();
	}

	public Integer startJsonParser(InputStream stream) {
		reader = new GxJsonReader(stream);
		reader.readBeginArray();
		
		StringCollection syncMetadata = reader.parseStringCollection();

		try	{
			errorCode = Integer.parseInt(syncMetadata.item(4));
		} catch (NumberFormatException e) {
			errorCode = null;
		}

		return errorCode;
	}

	public LinkedHashMap<String, String> getTableChecksum() {
		return tablesChecksum;
	}

	public void putTableChecksum(StringCollection tableMetadata) {
		String tableName = tableMetadata.item(2);
		String tableChecksum = tableMetadata.item(3);
		tablesChecksum.put(tableName, tableChecksum);
	}

	protected GxJsonReader getJsonReader() {
		return reader;
	}
	
	// interface for called sync methods.
	public abstract String getSyncVersion( );
	public abstract void executeGXAllSync( );
	
	
}