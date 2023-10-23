package com.genexus.management;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.logging.log4j.Logger;

public class HTTPConnectionJMX implements HTTPConnectionJMXBean{

	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HTTPPoolJMX.class);

	HttpRoute connection;

	public HTTPConnectionJMX(HttpRoute connection)
	{
		this.connection = connection;
	}

	static public void CreateHTTPConnectionJMX(HttpRoute connection)
	{
		try
		{
			MBeanUtils.createMBean(connection);
		}
		catch(Exception e)
		{
			log.error("Cannot register HTTP connection MBean.", e);
		}
	}

	static public void DestroyHTTPConnectionJMX(HttpRoute connection)
	{
		try
		{
			MBeanUtils.destroyMBean(connection);
		}
		catch(Exception e)
		{
			log.error("Cannot destroy connection MBean.", e);
		}
	}

	public int getPort()
	{
		return connection.getTargetHost().getPort();
	}

	public String getHost()
	{
		return connection.getTargetHost().getHostName();
	}
}
