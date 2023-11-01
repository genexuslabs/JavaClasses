package com.genexus.management;

import com.genexus.internet.IdentifiableHttpRoute;
import org.apache.logging.log4j.Logger;

public class HTTPConnectionJMX implements HTTPConnectionJMXBean{

	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HTTPConnectionJMX.class);

	IdentifiableHttpRoute identifiableHttpRoute;

	public HTTPConnectionJMX(IdentifiableHttpRoute httpRoute) {
		this.identifiableHttpRoute = httpRoute;
	}

	static public void CreateHTTPConnectionJMX(IdentifiableHttpRoute connection) {
		try {
			MBeanUtils.createMBean(connection);
		}
		catch (Exception e) {
			log.error("Failed to register HTTP connection MBean.", e);
		}
	}

	static public void DestroyHTTPConnectionJMX(IdentifiableHttpRoute connection) {
		try {
			MBeanUtils.destroyMBean(connection);
		}
		catch (Exception e) {
			log.error("Failed to destroy HTTP connection MBean.", e);
		}
	}

	public int getPort() {
		return identifiableHttpRoute.getHttpRoute().getTargetHost().getPort();
	}

	public String getHost() {
		return identifiableHttpRoute.getHttpRoute().getTargetHost().getHostName();
	}
}
