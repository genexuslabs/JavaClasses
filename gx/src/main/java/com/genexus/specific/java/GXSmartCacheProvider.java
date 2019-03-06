package com.genexus.specific.java;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.CacheFactory;
import com.genexus.CommonUtil;
import com.genexus.GXSmartCacheProvider.DataUpdateStatus;
import com.genexus.GXSmartCacheProvider.SmartCacheStatus;
import com.genexus.ICacheService;
import com.genexus.Preferences;
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
		static final String FORCED_INVALIDATE = "SD";

		ICacheService updatedTables; 
		SmartCacheStatus status = SmartCacheStatus.Unknown;
		Object syncLock = new Object();
		Vector<String> tablesUpdatedInUTL;

		public JavaSmartCacheProvider() {
			super();
			tablesUpdatedInUTL = new Vector<String>();
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
				getUpdatedTables().clear(CacheFactory.CACHE_SD, item);
		}
		public void recordUpdates()
		{
			if (isEnabled())
			{
				Date  dt = CommonUtil.now(false,false);
				for(Enumeration enumera = tablesUpdatedInUTL.elements(); enumera.hasMoreElements();)
				{
					getUpdatedTables().<Date>set(CacheFactory.CACHE_SD, normalizeKey((String)enumera.nextElement()), dt);
				}
				tablesUpdatedInUTL.clear();
			}
		}
		public DataUpdateStatus CheckDataStatus(String queryId, Date dateLastModified, Date[] dateUpdated_arr)
		{
			try {
				if (isEnabled()){
					ICacheService updTables = getUpdatedTables();
					ConcurrentHashMap<String, Vector<String>> qryTables = queryTables();
					Date dateUpdated = startupDate; // por default los datos son tan viejos como el momento de startup de la app
		
					Date forcedInvalidate;
					if (updTables.containtsKey(CacheFactory.CACHE_SD, FORCED_INVALIDATE))
					{
						forcedInvalidate = updTables.<Date>get(CacheFactory.CACHE_SD, FORCED_INVALIDATE, Date.class);
						if (dateUpdated.before(forcedInvalidate))		// Caso en que se invalido el cache manualmente
							dateUpdated = forcedInvalidate;
					}
		
					dateUpdated_arr[0] = dateUpdated;
					if (!qryTables.containsKey(queryId))      // No hay definicion de tablas para el query -> status desconocido
						return DataUpdateStatus.Unknown;
		
					Vector<String> qTables = qryTables.get(queryId);
					for(Enumeration enumera = qTables.elements(); enumera.hasMoreElements();)
					{
						String qTable = normalizeKey((String)enumera.nextElement());
						if (updTables.containtsKey(CacheFactory.CACHE_SD, qTable) && updTables.<Date>get(CacheFactory.CACHE_SD, qTable, Date.class).after(dateUpdated))  // Tabla del query fue modificada -> registra timestamp
							dateUpdated = updTables.<Date>get(CacheFactory.CACHE_SD, qTable, Date.class);
					}
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
		
		public void discardUpdates()
		{
			if (isEnabled())
				tablesUpdatedInUTL.clear();
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
		public void setUpdated(String table) {
			if (isEnabled() && ! tablesUpdatedInUTL.contains(table))
				tablesUpdatedInUTL.add(table);
			
		}
	}


}
