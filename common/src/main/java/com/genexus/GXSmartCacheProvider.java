package com.genexus;

import java.util.*;
import java.io.File;

import com.genexus.common.interfaces.IGXSmartCacheProvider;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.Log;
import com.genexus.util.GXDirectory;
import com.genexus.util.GXFileCollection;
import java.util.concurrent.ConcurrentHashMap;
public class GXSmartCacheProvider
{

	static IGXSmartCacheProvider provider = SpecificImplementation.GXSmartCacheProvider.createCache();
	private int handle;


	public GXSmartCacheProvider(int handle)
	{
		this.handle = handle;
	}

	// Marca una tabla como modificada. Se utiliza desde los pgms generados luego de una actulizacion sobre la tabla
	public void setUpdated(String table)
	{
		provider.setUpdated(table, handle);
	}


	public static ICacheService getUpdatedTables() {
		return (ICacheService) provider.getUpdatedTables();
	}

	public static boolean isEnabled()
	{
		return provider.isEnabled();
	}

	public static void invalidateAll()
	{
		provider.invalidateAll();
	}
	public static void invalidate(String item)
	{
		provider.invalidate(item);

	}
	// Commit de datos actualizados. Las tablas modificadas hasta el momento se registran con el timestamp del commit.
	public void recordUpdates()
	{
		provider.recordUpdates(handle);
	}

	// Rollback. Elimina todas las tablas pendientes sin registrarlas.
	public void discardUpdates()
	{
		provider.discardUpdates(handle);
	}

	// Dado un query y na fecha de actualizacion, chequea si la info del query esta vigente.
	// <param name="queryId"></param>
	// <param name="dateLastModified"></param>
	// <param name="dateUpdated"></param>
	// <returns>Unknown/Invalid/UpToDate</returns>
	static public DataUpdateStatus CheckDataStatus(String queryId, Date dateLastModified, Date[] dateUpdated_arr)
	{
		return (DataUpdateStatus) provider.CheckDataStatus(queryId, dateLastModified, dateUpdated_arr);
	}


	// Carga el mapping de query -> tablas que utiliza
	public static ConcurrentHashMap<String, Vector<String>> queryTables()
	{
		return provider.queryTables();

	}



	static public enum DataUpdateStatus
	{
        Unknown,
        Invalid,
        UpToDate
	}

	static public enum SmartCacheStatus
	{
        Unknown,
        Enabled,
        Disabled
	}
}
