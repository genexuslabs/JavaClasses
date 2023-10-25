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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CustomPoolingHttpClientConnectionManager extends PoolingHttpClientConnectionManager {
	private final List<IConnectionObserver> observers = new ArrayList<>();

	public CustomPoolingHttpClientConnectionManager(Registry<ConnectionSocketFactory> socketFactoryRegistry){
		super(socketFactoryRegistry);
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

				if (connection != null && !connection.isOpen())
					notifyConnectionCreated(route);

				return connection;
			}
		};
	}

	@Override
	public void closeExpiredConnections() {
		Set<HttpRoute> beforeClosing = new HashSet<>();
		Set<HttpRoute> afterClosing = new HashSet<>();

		super.enumAvailable(entry -> {
			if (entry.isExpired(System.currentTimeMillis())) {
				beforeClosing.add(entry.getRoute());
			}
		});
		super.closeExpiredConnections();
		super.enumAvailable(entry -> afterClosing.add(entry.getRoute()));
		beforeClosing.removeAll(afterClosing);

		for (HttpRoute route : beforeClosing)
			notifyConnectionDestroyed(route);
	}

	@Override
	public void closeIdleConnections(long idletime, TimeUnit tunit) {
		Set<HttpRoute> beforeClosing = new HashSet<>();
		Set<HttpRoute> afterClosing = new HashSet<>();
		long idleTimeoutMillis = tunit.toMillis(idletime);

		super.enumAvailable(entry -> {
			if (entry.getUpdated() + idleTimeoutMillis < System.currentTimeMillis()) {
				beforeClosing.add(entry.getRoute());
			}
		});
		super.closeIdleConnections(idletime, tunit);
		super.enumAvailable(entry -> afterClosing.add(entry.getRoute()));
		beforeClosing.removeAll(afterClosing);

		for (HttpRoute route : beforeClosing)
			notifyConnectionDestroyed(route);
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


