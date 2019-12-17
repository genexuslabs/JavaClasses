package com.genexus.util;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.util.cloudservice.Property;
import com.genexus.util.cloudservice.Service;
import com.genexus.util.cloudservice.Services;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class GXServices {

	public static final ILogger logger = LogManager.getLogger(GXServices.class);

	private static final boolean DEBUG = com.genexus.DebugFlag.DEBUG;
	public static final String WEBNOTIFICATIONS_SERVICE = "WebNotifications";
	public static final String STORAGE_SERVICE = "Storage";
	public static final String STORAGE_APISERVICE = "StorageAPI";
	public static final String CACHE_SERVICE = "Cache";
	private static final String SERVICES_FILE = "CloudServices.config";
	private static final String SERVICES_DEV_FILE = "CloudServices.dev.config";
	private static GXServices instance;
	private Hashtable<String, GXService> services = new Hashtable<String, GXService>();

	public GXServices() {
		readServices("");
	}

	public GXServices(String basePath) {
		readServices(basePath);
	}

	public static GXServices getInstance() {
		if (instance == null)
			instance = new GXServices();
		return instance;
	}

	public static GXServices getInstance(String basePath) {
		if (instance == null)
			instance = new GXServices(basePath);
		return instance;
	}

	public static void endGXServices() {
		instance = null;
	}

	public static void loadFromFile(String basePath, String fileName, GXServices services) {
		if (basePath.equals("")) {
			basePath = services.configBaseDirectory();
		}
		String fullPath = basePath + fileName;
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		Services xmlServices = null;
		try {
			xmlServices = xmlMapper.readValue(new File(fullPath), Services.class);
		} catch (JsonParseException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}

		if (services == null)
			services = new GXServices();

		for (Service xmlService : xmlServices.getService()) {
			GXService service = new GXService();
			service.setName(xmlService.getName());
			service.setType(xmlService.getType());
			service.setClassName(xmlService.getClassName());
			service.setAllowMultiple(xmlService.getAllowMultiple());
			GXProperties ptys = new GXProperties();
			for (Property xmlPty : xmlService.getProperties().getProperty()) {
				ptys.add(xmlPty.getName(), xmlPty.getValue());
			}
			service.setProperties(ptys);

			if (service.getAllowMultiple()) {
				services.services.put(service.getType() + ":" + service.getName(), service);
			} else {
				services.services.put(service.getType(), service);
			}
		}
	}

	private String configBaseDirectory() {
		String baseDir = "";
		String envVariable = System.getenv("LAMBDA_TASK_ROOT");
		if (envVariable != null && envVariable.length() > 0)
			return envVariable + File.separator;

		if (ModelContext.getModelContext() != null) {
			HttpContext webContext = (HttpContext) ModelContext.getModelContext().getHttpContext();
			if ((webContext != null) && (webContext instanceof HttpContextWeb)) {
				baseDir = com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator
						+ "WEB-INF" + File.separatorChar;
			}
		}
		if (baseDir.equals("")) {
			String servletPath = com.genexus.ApplicationContext.getInstance().getServletEngineDefaultPath();
			if (servletPath != null && !servletPath.equals("")) {
				baseDir = servletPath + File.separator + "WEB-INF" + File.separatorChar;
			}
		}
		return baseDir;
	}

	private void readServices(String basePath) {

		if (basePath.equals(""))
			basePath = configBaseDirectory();
		if (new File(basePath + SERVICES_DEV_FILE).exists()) {
			loadFromFile(basePath, SERVICES_DEV_FILE, this);
		}
		if (new File(basePath + SERVICES_FILE).exists()) {
			loadFromFile(basePath, SERVICES_FILE, this);
		}
	}

	public GXService get(String name) {
		return services.get(name);
	}
}