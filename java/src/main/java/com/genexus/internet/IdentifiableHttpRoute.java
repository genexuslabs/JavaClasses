package com.genexus.internet;

import org.apache.http.conn.routing.HttpRoute;

public class IdentifiableHttpRoute {
	private HttpRoute httpRoute;
	private static int instanceCount = 0;
	private int id;

	public IdentifiableHttpRoute(HttpRoute httpRoute) {
		this.httpRoute = httpRoute;
		this.id = instanceCount++;;
	}

	public int getId() {
		return id;
	}

	public HttpRoute getHttpRoute() {
		return httpRoute;
	}

	public String toString() {
		return "IdentifiableHttpRoute{" +
			"target=" + httpRoute.getTargetHost() +
			", localAddress=" + httpRoute.getLocalAddress() +
			", instance id='" + id + '\'' +
			'}';
	}
}


