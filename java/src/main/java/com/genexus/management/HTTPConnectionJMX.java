package com.genexus.management;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.logging.log4j.Logger;

public class HTTPConnectionJMX implements HTTPConnectionJMXBean{

	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HTTPConnectionJMX.class);

	HttpRoute httpRoute;

	public HTTPConnectionJMX(HttpRoute httpRoute) {
		this.httpRoute = httpRoute;
	}

	static public void CreateHTTPConnectionJMX(HttpRoute connection) {
		try {
			MBeanUtils.createMBean(connection);
		}
		catch(Exception e) {
			log.error("Failed to register HTTP connection MBean.", e);
		}
	}

	static public void DestroyHTTPConnectionJMX(HttpRoute connection) {
		try {
			MBeanUtils.destroyMBean(connection);
		}
		catch(Exception e) {
			log.error("Failed to destroy HTTP connection MBean.", e);
		}
	}

	public int getPort() {
		return httpRoute.getTargetHost().getPort();
	}

	public String getHost() {
		return httpRoute.getTargetHost().getHostName();
	}
}
