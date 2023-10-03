package com.genexus;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.xml.XMLReader;
import org.apache.commons.lang.StringUtils;

import com.genexus.GXSmartCacheProvider.DataUpdateStatus;
import com.genexus.common.classes.AbstractGXFile;
import com.genexus.common.interfaces.IGXSmartCacheProvider;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.GXDirectory;
import com.genexus.util.GXFileCollection;

public abstract class BaseProvider implements IGXSmartCacheProvider
	{
		private static final ILogger logger = LogManager.getLogger(BaseProvider.class);

		volatile ConcurrentHashMap<String, Vector<String>> queryTables;
		protected Date startupDate;
		static Object syncLock = new Object();
		protected static final String FORCED_INVALIDATE = "SD";


		protected String normalizeKey(String key)
		{
			if (StringUtils.isNotEmpty(key))
				return key.toLowerCase();
			else
				return key;
		}
		protected void normalizeKey(List<String> keys) {
			ListIterator<String> iterator = keys.listIterator();
			while (iterator.hasNext()) {
				String s = iterator.next();
				iterator.set(normalizeKey(s));
			}
		}
		private void loadQueryTables()
		{
			if (isEnabled())
			{
				String path = SpecificImplementation.Application.getModelContext().getHttpContext().getDefaultPath();
				String configurationDirectoryPath = path + File.separatorChar + "Metadata" + File.separatorChar + "TableAccess";
				ConcurrentHashMap<String, Vector<String>> qTables = new ConcurrentHashMap<String, Vector<String>>();
				GXDirectory configurationDirectory = new GXDirectory(configurationDirectoryPath);
				GXFileCollection files = configurationDirectory.getFiles();
				XMLReader reader = new XMLReader();
				short ok;
				for(int i=1; i <= files.getItemCount(); i++)
				{
					Vector<String> lst = new Vector<String>();
					lst.add(FORCED_INVALIDATE); // Caso en que se invalido el cache manualmente
					AbstractGXFile xmlFile = files.item(i);
					reader.open(xmlFile.getAbsoluteName());
					ok = reader.readType(1, "Table");
					while (ok == 1)
					{
						lst.add(normalizeKey((String) reader.getAttributeByName("name")));
						ok = reader.readType(1, "Table");
					}
					reader.close();
					qTables.put(normalizeKey((String)xmlFile.getNameNoExt()), lst);
				}
				startupDate = CommonUtil.now(false,false);
				queryTables = qTables;
			}
		}
		public ConcurrentHashMap<String, Vector<String>> queryTables() {
			if (queryTables == null)
			{
				synchronized (syncLock)
				{
					if (queryTables == null)
						loadQueryTables();
				}
			}
			return queryTables;	
		}
		
		public void invalidateAll(){}
		public void invalidate(String item){}
		public void recordUpdates(int handle){}
		public Object CheckDataStatus(String queryId, Date dateLastModified, Date[] dateUpdated_arr){return DataUpdateStatus.Unknown;}
		public void discardUpdates(int handle){}
		public boolean isEnabled() { return true;}
		public ICacheService getUpdatedTables() {
			return null;
		}
		public void setUpdated(String table, int handle) {
		}

	}
