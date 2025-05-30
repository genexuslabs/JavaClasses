package com.genexus;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.xml.XMLReader;
import org.apache.commons.lang3.StringUtils;

import com.genexus.GXSmartCacheProvider.DataUpdateStatus;
import com.genexus.common.classes.AbstractGXFile;
import com.genexus.common.interfaces.IGXSmartCacheProvider;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.GXDirectory;
import com.genexus.util.GXFileCollection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
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
				ConcurrentHashMap<String, Vector<String>> qTables = new ConcurrentHashMap<String, Vector<String>>();
				if (ApplicationContext.getInstance().isSpringBootApp())
					loadQueryTablesSpringBoot(configurationDirectoryPath, qTables);
				else
					loadQueryTablesFileSystem(configurationDirectoryPath, qTables);
				startupDate = CommonUtil.now(false,false);
				queryTables = qTables;
			}
		}

		private void loadQueryTablesFileSystem(String configurationDirectoryPath, ConcurrentHashMap<String, Vector<String>> qTables){
			GXDirectory configurationDirectory = new GXDirectory(configurationDirectoryPath);
			GXFileCollection files = configurationDirectory.getFiles();
			XMLReader reader = new XMLReader();
			boolean anyTables=false;
			for(int i=1; i <= files.getItemCount(); i++)
			{
				AbstractGXFile xmlFile = files.item(i);
				String xmlFileName = xmlFile.getAbsoluteName();
				String xmlFileNameNoExt = xmlFile.getNameNoExt();
				anyTables = processXMLFile(reader, anyTables, xmlFileName, xmlFileNameNoExt, qTables);
			}
		}

		private void loadQueryTablesSpringBoot(String configurationDirectoryPath, ConcurrentHashMap<String, Vector<String>> qTables){
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			try {
				Resource[] resources = resolver.getResources("classpath:" + configurationDirectoryPath + "/*");
				XMLReader reader = new XMLReader();
				boolean anyTables=false;
				for (Resource resource : resources) {
					String xmlFileName = resource.getFilename();
					String xmlFileNameNoExt = xmlFileName.substring(0, xmlFileName.lastIndexOf('.'));
					xmlFileName = resource instanceof FileSystemResource? ((FileSystemResource) resource).getPath() : ((ClassPathResource) resource).getPath();
					anyTables = processXMLFile(reader, anyTables, xmlFileName, xmlFileNameNoExt, qTables);
				}
			}
			catch (IOException e){
				logger.error("Error loading Query Tables ", e);
			}
		}

		private boolean processXMLFile(XMLReader reader, boolean anyTables, String xmlFileName, String xmlFileNameNoExt, ConcurrentHashMap<String, Vector<String>> qTables) {
			Vector<String> lst = new Vector<>();
			lst.add(FORCED_INVALIDATE); // Caso en que se invalido el cache manualmente
			reader.open(xmlFileName);
			short ok = reader.readType(1, "Table");
			boolean anyLocalTables = false;
			while (ok == 1)
			{
				anyLocalTables = true;
				lst.add(normalizeKey(reader.getAttributeByName("name")));
				ok = reader.readType(1, "Table");
			}
			reader.close();
			if (anyTables || anyLocalTables) {
				qTables.put(normalizeKey(xmlFileNameNoExt), lst);
			}
			return anyTables || anyLocalTables;
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
