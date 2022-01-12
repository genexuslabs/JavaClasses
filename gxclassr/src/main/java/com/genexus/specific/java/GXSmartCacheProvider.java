package com.genexus.specific.java;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.*;
import com.genexus.GXSmartCacheProvider.DataUpdateStatus;
import com.genexus.GXSmartCacheProvider.SmartCacheStatus;
import com.genexus.common.interfaces.IExtensionGXSmartCacheProvider;
import com.genexus.common.interfaces.IGXSmartCacheProvider;
import com.genexus.diagnostics.Log;

public class GXSmartCacheProvider implements IExtensionGXSmartCacheProvider {

	@Override
	public IGXSmartCacheProvider createCache() {
		return new JavaSmartCacheProvider();
	}
	
	public class JavaSmartCacheProvider extends com.genexus.BaseProvider 
	{

		ICacheService updatedTables; 
		SmartCacheStatus status = SmartCacheStatus.Unknown;
		Object syncLock = new Object();
		ConcurrentHashMap<Integer, Vector<String>> tablesUpdatedInUTL;

		public JavaSmartCacheProvider() {
			super();
			tablesUpdatedInUTL = new ConcurrentHashMap<Integer, Vector<String>>();
		}
		
		public void invalidateAll()
		{
			if (isEnabled())
			{
				getUpdatedTables().<Date>set(CacheFactory.CACHE_SD, FORCED_INVALIDATE, CommonUtil.now(false,false));
			}
		}
		
		public ICacheService getUpdatedTables() {
			if (updatedTables == null) {
				synchronized (syncLock)
				{
					if (updatedTables == null)
						updatedTables = CacheFactory.getInstance();				
				}
			}
			return updatedTables;
		}
		public void invalidate(String item)
		{
			if (isEnabled()) 
				getUpdatedTables().clear(CacheFactory.CACHE_SD, normalizeKey(item));
		}
		public void recordUpdates(int handle)
		{
			if (isEnabled() && tablesUpdatedInUTL.containsKey(handle))
			{
				Vector<String> tablesUpdatedInUTLHandle = getTablesUpdatedInUTL(handle);
				Date  dt = CommonUtil.now(false,false);
				if (!tablesUpdatedInUTLHandle.isEmpty()) {
					ICacheService updTables = getUpdatedTables();
					normalizeKey(tablesUpdatedInUTLHandle);
					if (updTables instanceof ICacheService2) {
						((ICacheService2)updTables).setAll(CacheFactory.CACHE_SD, tablesUpdatedInUTLHandle.toArray(new String[tablesUpdatedInUTLHandle.size()]), Collections.nCopies(tablesUpdatedInUTLHandle.size(), dt).toArray(), 0);
					}else {
						for(String tbl:tablesUpdatedInUTLHandle)
						{
							updTables.<Date>set(CacheFactory.CACHE_SD, tbl, dt);
						}
					}
					tablesUpdatedInUTL.remove(handle);
				}
			}
		}
		public DataUpdateStatus CheckDataStatus(String queryId, Date dateLastModified, Date[] dateUpdated_arr)
		{
			try {
				if (isEnabled()){
					ICacheService updTables = getUpdatedTables();
					ConcurrentHashMap<String, Vector<String>> qryTables = queryTables();
					Date dateUpdated = startupDate; // por default los datos son tan viejos como el momento de startup de la app

					dateUpdated_arr[0] = dateUpdated;
					if (!qryTables.containsKey(queryId))      // No hay definicion de tablas para el query -> status desconocido
						return DataUpdateStatus.Unknown;

					Vector<String> qTables = qryTables.get(queryId);
					String[] qTablesArray = qTables.toArray(new String[qTables.size()]);
					List<Date> dateUpdates;

					if (updTables instanceof ICacheService2) {
						dateUpdates = ((ICacheService2)updTables).getAll(CacheFactory.CACHE_SD, qTablesArray, Date.class); //Value is null date for non-existing key in cache
					}else{
						dateUpdates = new Vector<Date>();
						for(String qTable:qTables)
						{
							if (updTables.containtsKey(CacheFactory.CACHE_SD, qTable)) {
								dateUpdates.add(updTables.<Date>get(CacheFactory.CACHE_SD, qTable, Date.class));
							}
						}
					}
					Date maxDateUpdated = MaxDate(dateUpdates);//Obtiene la fecha de modificación mas nueva.
					if (maxDateUpdated!=null && maxDateUpdated.after(dateUpdated))
						dateUpdated = maxDateUpdated;

					dateUpdated_arr[0] = dateUpdated;
					if (dateUpdated.after(dateLastModified) || qTables.size() == 0)    // Si alguna de las tablas del query fueron modificadas o no hay tablas involucradas-> el status de la info es INVALIDO, hay que refrescar
						return DataUpdateStatus.Invalid;
					
					//We've seen situations where SD Client sends: If-Modified-Since: Sun, 20 Apr 2200. Don't know why yet.
					if(dateLastModified != null && dateLastModified.after(new Date()))
						return DataUpdateStatus.Invalid;
					
					return DataUpdateStatus.UpToDate;
				}
			}
			catch (Exception e)
			{
				Log.error("Could not check Request Cache status", "GXSmartCacheProvider", e);
			}		
			return DataUpdateStatus.Unknown;		

		}

		private Date MaxDate(List<Date> dates){
			if (dates!=null) {
				while (dates.remove(null)) ; //RemoveNulls
				if (dates.size()>0)
					return Collections.max(dates);
			}
			return null;
		}
		public void discardUpdates(int handle)
		{
			if (isEnabled() && tablesUpdatedInUTL.containsKey(handle))
				tablesUpdatedInUTL.remove(handle);
		}
		public boolean isEnabled() 
		{ 	
			if (status == SmartCacheStatus.Unknown)
			{
				synchronized (syncLock)
				{
					if (status == SmartCacheStatus.Unknown)
						status = (Preferences.getDefaultPreferences().getSMART_CACHING() == true)? SmartCacheStatus.Enabled: SmartCacheStatus.Disabled;						
				}
			}		
			return status == SmartCacheStatus.Enabled;
		}

		@Override
		public void setUpdated(String table, int handle) {
			Vector<String> tablesUpdatedInUTLHandle = getTablesUpdatedInUTL(handle);
			if (isEnabled() && ! tablesUpdatedInUTLHandle.contains(table))
				tablesUpdatedInUTLHandle.add(table);
			
		}

		private Vector<String> getTablesUpdatedInUTL(Integer handle){
			if (tablesUpdatedInUTL.containsKey(handle)){
				return tablesUpdatedInUTL.get(handle);
			}
			else {
				Vector<String> tablesUpdated = new Vector<String>();
				tablesUpdatedInUTL.put(handle, tablesUpdated);
				return tablesUpdated;
			}
		}
	}


}
