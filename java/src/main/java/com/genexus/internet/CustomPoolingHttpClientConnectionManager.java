package com.genexus.internet;

import org.apache.http.HttpClientConnection;
import org.apache.http.config.Registry;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolEntry;
import org.apache.http.pool.PoolEntryCallback;

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

				if (connection != null && !connection.isOpen()) {
					notifyConnectionCreated(route);
				}

				return connection;
			}
		};
	}

	@Override
	public void closeExpiredConnections() {
		Set<HttpRoute> closedRoutes = new HashSet<>();

		// Enumerate over all connections and collect routes of the expired ones
		super.enumAvailable(new PoolEntryCallback<HttpRoute, ManagedHttpClientConnection>() {
			@Override
			public void process(PoolEntry<HttpRoute, ManagedHttpClientConnection> entry) {
				if (entry.isExpired(System.currentTimeMillis())) {
					closedRoutes.add(entry.getRoute());
				}
			}
		});

		// Close the expired connections
		super.closeExpiredConnections();

		// Notify for each closed connection
		for (HttpRoute route : closedRoutes) {
			notifyConnectionDestroyed(route);
		}
	}

	@Override
	public void closeIdleConnections(long idletime, TimeUnit tunit) {
		Set<HttpRoute> closedRoutes = new HashSet<>();
		long idleTimeoutMillis = tunit.toMillis(idletime);

		// Enumerate over all connections and collect routes of the idle ones
		super.enumAvailable(new PoolEntryCallback<HttpRoute, ManagedHttpClientConnection>() {
			@Override
			public void process(PoolEntry<HttpRoute, ManagedHttpClientConnection> entry) {
				if (entry.getUpdated() + idleTimeoutMillis < System.currentTimeMillis()) {
					closedRoutes.add(entry.getRoute());
				}
			}
		});

		// Close the idle connections
		super.closeIdleConnections(idletime, tunit);

		// Notify for each closed connection
		for (HttpRoute route : closedRoutes) {
			notifyConnectionDestroyed(route);
		}
	}


	private void notifyConnectionCreated(HttpRoute route) {
		for (IConnectionObserver observer : observers) {
			observer.onConnectionCreated(route);
		}
	}

	private void notifyConnectionDestroyed(HttpRoute route) {
		for (IConnectionObserver observer : observers) {
			observer.onConnectionDestroyed(route);
		}
	}
}


