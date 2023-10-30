package com.genexus.internet;

import org.apache.http.config.Registry;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CustomPoolingHttpClientConnectionManager extends PoolingHttpClientConnectionManager {
	private final List<IConnectionObserver> observers = new ArrayList<>();

	private Set<IdentifiableHttpRoute> storedRoutes = this.getRoutes().stream().map(r -> new IdentifiableHttpRoute(r)).collect(Collectors.toSet());

	public CustomPoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry) {
		super(socketFactoryRegistry);
	}

	public void addObserver(IConnectionObserver observer) {
		observers.add(observer);
	}

	@Override
	public ConnectionRequest requestConnection(HttpRoute route, Object state) {
		ConnectionRequest originalRequest;
		originalRequest = super.requestConnection(route, state);
		if (originalRequest != null){
			IdentifiableHttpRoute identifiableHttpRoute = new IdentifiableHttpRoute(route);
			storedRoutes.add(identifiableHttpRoute);
			notifyConnectionCreated(identifiableHttpRoute);
		}
		return originalRequest;
	}

	@Override
	public void closeExpiredConnections() {
		Set<IdentifiableHttpRoute> beforeClosing = storedRoutes;
		super.closeExpiredConnections();
		Set<HttpRoute> afterClosing = this.getRoutes();

		for (IdentifiableHttpRoute route : beforeClosing)
			if (!afterClosing.contains(route.getHttpRoute()))
				notifyConnectionDestroyed(route);

		storedRoutes = afterClosing.stream().map(r -> new IdentifiableHttpRoute(r)).collect(Collectors.toSet());
	}

	@Override
	public void closeIdleConnections(long idletime, TimeUnit tunit) {
		Set<IdentifiableHttpRoute> beforeClosing = storedRoutes;
		super.closeIdleConnections(idletime, tunit);
		Set<HttpRoute> afterClosing = this.getRoutes();

		for (IdentifiableHttpRoute route : beforeClosing)
			if (!afterClosing.contains(route.getHttpRoute()))
				notifyConnectionDestroyed(route);

		storedRoutes = afterClosing.stream().map(r -> new IdentifiableHttpRoute(r)).collect(Collectors.toSet());
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


