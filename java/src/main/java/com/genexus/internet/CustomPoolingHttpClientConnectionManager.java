package com.genexus.internet;

import org.apache.http.config.Registry;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CustomPoolingHttpClientConnectionManager extends PoolingHttpClientConnectionManager {
	private final List<IConnectionObserver> observers = new ArrayList<>();

	private Set<IdentifiableHttpRoute> storedRoutes = this.getRoutes().stream().map(r -> new IdentifiableHttpRoute(r)).collect(Collectors.toSet());
	PoolStats storedStats = this.getTotalStats();

	public CustomPoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
		super(socketFactoryRegistry);
	}

	public void addObserver(IConnectionObserver observer) {
		observers.add(observer);
	}

	@Override
	public ConnectionRequest requestConnection(HttpRoute route, Object state) {
		PoolStats statsBefore = storedStats;
		ConnectionRequest connectionRequest = super.requestConnection(route, state);
		PoolStats statsAfter = this.getTotalStats();
		if (statsBefore.getAvailable() < statsAfter.getAvailable() || statsBefore.getLeased() < statsAfter.getLeased()) {
			IdentifiableHttpRoute identifiableHttpRoute = new IdentifiableHttpRoute(route);
			storedRoutes.add(identifiableHttpRoute);
			notifyConnectionCreated(identifiableHttpRoute);
			storedStats = statsAfter;
		}
		return connectionRequest;
	}

	@Override
	public void closeExpiredConnections() {
		Set<IdentifiableHttpRoute> beforeClosing = storedRoutes;
		Set<IdentifiableHttpRoute> commonRoutes = new HashSet<>();
		super.closeExpiredConnections();
		Set<HttpRoute> afterClosing = this.getRoutes();

		for (IdentifiableHttpRoute identifiableHttpRoute : beforeClosing){
			Boolean found = false;
			for (HttpRoute httpRoute : afterClosing){
				if (identifiableHttpRoute.equals(httpRoute)){
					found = true;
					commonRoutes.add(identifiableHttpRoute);
					break;
				}
			}
			if (!found) notifyConnectionDestroyed(identifiableHttpRoute);
		}

		storedRoutes = commonRoutes;
	}

	@Override
	public void closeIdleConnections(long idletime, TimeUnit tunit) {
		Set<IdentifiableHttpRoute> beforeClosing = storedRoutes;
		Set<IdentifiableHttpRoute> commonRoutes = new HashSet<>();
		super.closeIdleConnections(idletime, tunit);
		Set<HttpRoute> afterClosing = this.getRoutes();

		for (IdentifiableHttpRoute identifiableHttpRoute : beforeClosing){
			Boolean found = false;
			for (HttpRoute httpRoute : afterClosing){
				if (identifiableHttpRoute.equals(httpRoute)){
					found = true;
					commonRoutes.add(identifiableHttpRoute);
					break;
				}
			}
			if (!found) notifyConnectionDestroyed(identifiableHttpRoute);
		}

		storedRoutes = commonRoutes;
	}

	private void notifyConnectionCreated(IdentifiableHttpRoute route) {
		for (IConnectionObserver observer : observers)
			observer.onConnectionCreated(route);
	}

	private void notifyConnectionDestroyed(IdentifiableHttpRoute route) {
		for (IConnectionObserver observer : observers)
			observer.onConnectionDestroyed(route);
	}

	protected void finalize() {
		for (IdentifiableHttpRoute route : storedRoutes)
			for (IConnectionObserver observer : observers)
				observer.onConnectionDestroyed(route);
	}
}


