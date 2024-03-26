package com.genexus.springboot.jaxws;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;

@WebListener
public class GXWSServletContextListener implements ServletContextListener {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GXWSServletContextListener.class);
	private com.sun.xml.ws.transport.http.servlet.WSServletContextListener listener = null;

	public static final String JAXWS_METADATA_FILE = "sun-jaxws.xml";
	public static final String JAXWS_METADATA_PATH = "WEB-INF/sun-jaxws.xml";

	public GXWSServletContextListener() {
		if (new ClassPathResource(JAXWS_METADATA_FILE).exists())
			this.listener = new com.sun.xml.ws.transport.http.servlet.WSServletContextListener();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (listener != null) {
			try {
				FileUtils.copyInputStreamToFile(new ClassPathResource(JAXWS_METADATA_FILE).getInputStream(),
					new java.io.File(sce.getServletContext().getRealPath("") + JAXWS_METADATA_PATH));
			} catch (IOException e) {
				log.error(String.format("File %s not found", JAXWS_METADATA_FILE), e);
			}

			listener.contextInitialized(sce);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (listener != null)
			listener.contextDestroyed(sce);
	}
}
