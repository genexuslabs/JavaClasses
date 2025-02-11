package com.genexus.springboot;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.core.io.ClassPathResource;

public class GXServletContextInitializer implements ServletContextInitializer {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GXServletContextInitializer.class);

	@Override
	public void onStartup(ServletContext container) {
		try {
			//Add QueryViewer Servlets mappings
			if (new ClassPathResource("qviewer/services/agxpl_get.class").exists()) {
				ServletRegistration.Dynamic dispatcherQuery1 = container.addServlet("agxpl_get", (Servlet) Class.forName("qviewer.services.agxpl_get").getConstructor().newInstance());
				dispatcherQuery1.addMapping("/qviewer.services.agxpl_get");
			}

			if (new ClassPathResource("qviewer/services/agxpl_get_debug.class").exists()) {
				ServletRegistration.Dynamic dispatcherQuery2 = container.addServlet("agxpl_get_debug", (Servlet) Class.forName("qviewer.services.agxpl_get_debug").getConstructor().newInstance());
				dispatcherQuery2.addMapping("/qviewer.services.agxpl_get_debug");
			}

			if (new ClassPathResource("qviewer/services/gxqueryviewerforsd.class").exists()) {
				ServletRegistration.Dynamic dispatcherQuery3 = container.addServlet("gxqueryviewerforsd", (Servlet) Class.forName("qviewer.services.gxqueryviewerforsd").getConstructor().newInstance());
				dispatcherQuery3.addMapping("/qviewer.services.gxqueryviewerforsd");
			}

			try {
				Class<?> wsServlet = Class.forName("com.sun.xml.ws.transport.http.servlet.WSServlet");
				ServletRegistration.Dynamic dispatcherQuery3 = container.addServlet("JaxWSServlet", (Servlet) wsServlet.getConstructor().newInstance());
				dispatcherQuery3.setLoadOnStartup(1);
				dispatcherQuery3.addMapping("/servlet/ws/*");
				dispatcherQuery3.addMapping("/ws/*");
			}
			catch (ClassNotFoundException e) {
				log.info("Declare servlet mapping for com.sun.xml.ws.transport.http.servlet.WSServlet is not necessary");
			}
		}
		catch(Exception e) {
			log.error("Error executing ServletContextInitializer", e);
		}
	}
}
