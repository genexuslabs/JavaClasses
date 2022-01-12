package com.genexus.servlet;

public abstract class ServletContextListener implements javax.servlet.ServletContextListener {
	public abstract void contextDestroyedWrapper();
	public abstract void contextInitializedWrapper(String basePath, String gxcfg);

	public void contextDestroyed(javax.servlet.ServletContextEvent event){
		contextDestroyedWrapper();
	}

	public void contextInitialized(javax.servlet.ServletContextEvent event){
		javax.servlet.ServletContext context = event.getServletContext();
		String basePath = context.getRealPath("/");
		String gxcfg = context.getInitParameter("gxcfg");
		contextInitializedWrapper(basePath, gxcfg);
	}
}
