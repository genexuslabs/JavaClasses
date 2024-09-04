package com.genexus.util;

import java.io.File;
import java.util.Hashtable;

import com.genexus.ApplicationContext;
import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.xml.XMLReader;

public class GXServices {
	private static final ILogger logger = LogManager.getLogger(GXServices.class);
	public static final String WEBNOTIFICATIONS_SERVICE = "WebNotifications";
	public static final String STORAGE_SERVICE = "Storage";
	public static final String STORAGE_APISERVICE = "StorageAPI";
	public static final String CACHE_SERVICE = "Cache";
	public static final String DATA_ACCESS_SERVICE = "DataAccess";
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

	public static void loadFromFile(String basePath, String fileName, GXServices services){
		if (basePath.equals("")) {
			basePath = services.configBaseDirectory();
		}
		String fullPath = basePath + fileName;
		XMLReader reader = new XMLReader();
		reader.open(fullPath);
		reader.readType(1, "Services");
		reader.read();
		if (reader.getErrCode() == 0) {
			while (!reader.getName().equals("Services")) {
				services.processService(reader);
				reader.read();
				if (reader.getName().equals("Service") && reader.getNodeType() == 2) //</Service>
					reader.read();
			}
			reader.close();
		}
		else
		{
			if (!ApplicationContext.getInstance().getReorganization())
			{
				logger.debug("GXServices - Could not load Services Config: " + fullPath + " - " + reader.getErrDescription());
			}
		}
	}

	public String configBaseDirectory() {
		if (ApplicationContext.getInstance().isSpringBootApp())
			return "";
		
		String baseDir = "";
		String envVariable = System.getenv("LAMBDA_TASK_ROOT");
		if (envVariable != null && envVariable.length() > 0)
			return envVariable + File.separator;

		if (ModelContext.getModelContext() != null) {
			HttpContext webContext = (HttpContext) ModelContext.getModelContext().getHttpContext();
			if ((webContext != null) && (webContext instanceof HttpContextWeb)) {
				baseDir = com.genexus.ModelContext.getModelContext()
					.getHttpContext().getDefaultPath()
					+ File.separator + "WEB-INF" + File.separatorChar;
			}
		}
		if (baseDir.equals("")) {
			String servletPath = com.genexus.ApplicationContext.getInstance().getServletEngineDefaultPath();
			if (servletPath != null && !servletPath.equals(""))
			{
				baseDir = servletPath + File.separator + "WEB-INF" + File.separatorChar;
			}
		}
		return baseDir;
	}

	private void readServices(String basePath) {

		if (basePath.equals(""))
			basePath = configBaseDirectory();
		if (ApplicationContext.getInstance().checkIfResourceExist(basePath + SERVICES_DEV_FILE)){
			loadFromFile(basePath, SERVICES_DEV_FILE, this);
		}
		if (ApplicationContext.getInstance().checkIfResourceExist(basePath + SERVICES_FILE)){
			loadFromFile(basePath, SERVICES_FILE, this);
		}
	}

	private void processService(XMLReader reader) {
		short result;
		result = reader.readType(1, "Name");
		String name = new String(reader.getValue());

		result = reader.readType(1, "Type");
		String type = new String(reader.getValue());

		result = reader.readType(1, "ClassName");
		String className = new String(reader.getValue());

		boolean allowMultiple = false;
		reader.read();
		if (reader.getName() == "AllowMultiple")
		{
			allowMultiple = Boolean.parseBoolean(reader.getValue());
			reader.read();
		}
		GXProperties properties = processProperties(reader);

		GXService service = new GXService();
		service.setName(name);
		service.setType(type);
		service.setClassName(className);
		service.setAllowMultiple(allowMultiple);
		service.setProperties(properties);
		if (service.getAllowMultiple()){
			services.put(service.getType() + ":" + service.getName(), service);
		}
		else{
			services.put(type, service);
		}
	}

	private GXProperties processProperties(XMLReader reader) {
		short result;
		GXProperties properties = new GXProperties();
		reader.read();
		while (reader.getName().equals("Property")) {
			result = reader.readType(1, "Name");
			String name = new String(reader.getValue());
			result = reader.readType(1, "Value");
			String value = new String(reader.getValue());
			properties.add(name, value);
			reader.read();
			reader.read();
		}
		return properties;
	}

	public GXService get(String name) {
		return services.get(name);
	}

}