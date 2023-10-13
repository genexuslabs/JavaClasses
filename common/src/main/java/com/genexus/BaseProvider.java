package com.genexus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.xml.XMLReader;
import org.apache.commons.lang.StringUtils;

import com.genexus.GXSmartCacheProvider.DataUpdateStatus;
import com.genexus.common.classes.AbstractGXFile;
import com.genexus.common.interfaces.IGXSmartCacheProvider;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.GXDirectory;
import com.genexus.util.GXFileCollection;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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

				ConcurrentHashMap<String, Vector<String>> qTables = new ConcurrentHashMap();
				loadQueryTablesPlatform(configurationDirectoryPath, qTables);
				startupDate = CommonUtil.now(false,false);
				queryTables = qTables;
			}
		}

		public void loadQueryTablesPlatform(String configurationDirectoryPath, ConcurrentHashMap<String, Vector<String>> qTables) {
			if (ApplicationContext.getInstance().isSpringBootApp())
				loadQueryTablesSpringBoot(configurationDirectoryPath, qTables);
			else
				loadQueryTablesNone(configurationDirectoryPath, qTables);

		}

		public void loadQueryTablesNone(String configurationDirectoryPath, ConcurrentHashMap<String, Vector<String>> qTables) {
			GXDirectory configurationDirectory = new GXDirectory(configurationDirectoryPath);
			GXFileCollection files = configurationDirectory.getFiles();
			XMLReader reader = new XMLReader();
			short ok;
			for(int i=1; i <= files.getItemCount(); i++) {
				Vector<String> lst = new Vector<String>();
				lst.add(FORCED_INVALIDATE); // Caso en que se invalido el cache manualmente
				AbstractGXFile xmlFile = files.item(i);
				reader.open(xmlFile.getAbsoluteName());
				ok = reader.readType(1, "Table");
				while (ok == 1) {
					lst.add(normalizeKey(reader.getAttributeByName("name")));
					ok = reader.readType(1, "Table");
				}
				reader.close();
				qTables.put(normalizeKey(xmlFile.getNameNoExt()), lst);
			}
		}

		public void loadQueryTablesSpringBoot(String configurationDirectoryPath, ConcurrentHashMap<String, Vector<String>> qTables) {
			try {
				Resource[] resources = new PathMatchingResourcePatternResolver().getResources(configurationDirectoryPath + "/*.xml");
				XMLReader reader = new XMLReader();
				reader.setDocEncoding("UTF8");
				short ok;
				String xmlContent;
				for (int i = 0; i < resources.length; i++) {
					Vector<String> lst = new Vector<String>();
					lst.add(FORCED_INVALIDATE);
					xmlContent = resources[i].getContentAsString(StandardCharsets.UTF_8);
					if (!xmlContent.startsWith("<"))
						xmlContent = xmlContent.substring(1); //Avoid BOM
					reader.openFromString(xmlContent);
					ok = reader.readType(1, "Table");
					while (ok == 1) {
						lst.add(normalizeKey(reader.getAttributeByName("name")));
						ok = reader.readType(1, "Table");
					}
					reader.close();
					qTables.put(normalizeKey(resources[i].getFilename().substring(0, resources[i].getFilename().lastIndexOf("."))), lst);
				}
			}
			catch (IOException e) {
				logger.error("Error reading Table Access metadata", e);
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
