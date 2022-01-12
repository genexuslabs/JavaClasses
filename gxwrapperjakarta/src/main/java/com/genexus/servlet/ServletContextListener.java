package com.genexus.servlet;

public abstract class ServletContextListener implements jakarta.servlet.ServletContextListener {
	public abstract void contextDestroyedWrapper();
	public abstract void contextInitializedWrapper(String basePath, String gxcfg);

	public void contextDestroyed(jakarta.servlet.ServletContextEvent event){
		contextDestroyedWrapper();
	}

	public void contextInitialized(jakarta.servlet.ServletContextEvent event){
		jakarta.servlet.ServletContext context = event.getServletContext();
		String basePath = context.getRealPath("/");
		String gxcfg = context.getInitParameter("gxcfg");
		contextInitializedWrapper(basePath, gxcfg);
	}
}
