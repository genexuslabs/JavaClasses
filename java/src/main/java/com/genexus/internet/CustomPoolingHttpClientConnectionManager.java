package com.genexus.internet;

import org.apache.http.HttpClientConnection;
import org.apache.http.config.Registry;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomPoolingHttpClientConnectionManager extends PoolingHttpClientConnectionManager {
	private final List<IConnectionObserver> observers = new ArrayList<>();

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	HashSet<HttpRoute> activeRoutes = new HashSet<>(this.getRoutes());

	public CustomPoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry){
		super(socketFactoryRegistry);
		initializePeriodicPoolCheck();
	}

	private void initializePeriodicPoolCheck() {
		Runnable task = () -> periodicPoolCheck();
		scheduler.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS);
	}

	private void periodicPoolCheck() {
		if (activeRoutes.size() > this.getRoutes().size()){
			activeRoutes.removeAll(this.getRoutes());
			for (HttpRoute route : activeRoutes)
				notifyConnectionDestroyed(route);
		}
		activeRoutes = new HashSet<>(this.getRoutes());
	}

	public void addObserver(IConnectionObserver observer) {
		observers.add(observer);
	}

	@Override
	public ConnectionRequest requestConnection(HttpRoute route, Object state) {
		final ConnectionRequest originalRequest = super.requestConnection(route, state);

		return new ConnectionRequest() {
			@Override
			public boolean cancel() {
				return originalRequest.cancel();
			}

			@Override
			public HttpClientConnection get(long timeout, TimeUnit tunit) throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
				HttpClientConnection connection = originalRequest.get(timeout, tunit);

				if (connection != null && !connection.isOpen()){
					notifyConnectionCreated(route);
					activeRoutes.add(route);
				}

				return connection;
			}
		};
	}

	private void notifyConnectionCreated(HttpRoute route) {
		for (IConnectionObserver observer : observers)
			observer.onConnectionCreated(route);
	}

	private void notifyConnectionDestroyed(HttpRoute route) {
		for (IConnectionObserver observer : observers)
			observer.onConnectionDestroyed(route);
	}
}


