package com.genexus.session;

import com.genexus.ICacheService2;
import com.genexus.servlet.http.IHttpSession;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Enumeration;


public class DistributedHttpSession implements IHttpSession {
	static String CACHE_PREFIX = "GX_SESSION_"; //Should be obtained from Config..
	private ICacheService2 cache;
	private String cacheId;
	private int duration = 10; //Should be obtained from Config..

	public DistributedHttpSession(ICacheService2 cacheService, String sessionId) {
		cache = cacheService;
		cacheId = sessionId;
	}

	public Object getAttribute(String name) {
		return cache.get(cacheId, name, String.class);
	}

	public void setAttribute(String name, Object value) {
		cache.set(cacheId, name, value, duration);
	}

	public void removeAttribute(String name) {
		cache.clear(cacheId, name);
	}

	public Enumeration<String> getAttributeNames() {
		throw new NotImplementedException();
	}

	public String getId() {
		return cacheId;
	}

	public void invalidate() {
		cache.clearCache(cacheId);
	}
}
