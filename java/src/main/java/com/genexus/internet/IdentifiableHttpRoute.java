package com.genexus.internet;

import org.apache.http.conn.routing.HttpRoute;

public class IdentifiableHttpRoute {
	private HttpRoute httpRoute;
	private static int instanceCount = 0;
	private long id;

	public IdentifiableHttpRoute(HttpRoute httpRoute) {
		this.httpRoute = httpRoute;
		this.id = instanceCount++;;
	}

	public long getId() {
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

	@Override
	public final boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof HttpRoute))
			return false;
		HttpRoute that = (HttpRoute) o;
		Boolean sameHost = this.getHttpRoute().getTargetHost().getHostName().equals(that.getTargetHost().getHostName());
		Boolean samePort = this.getHttpRoute().getTargetHost().getPort() == that.getTargetHost().getPort();
		return sameHost && samePort;
	}
}


