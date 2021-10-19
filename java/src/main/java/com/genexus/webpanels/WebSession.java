package com.genexus.webpanels;


import java.util.*;

import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpSession;

import com.genexus.CommonUtil;
import com.genexus.internet.HttpContext;
import com.genexus.session.LocalSession;
import com.genexus.util.Encryption;
import com.genexus.util.SubmitThreadPool;

public class WebSession {
	private IHttpServletRequest request;
	private IHttpSession session;
	private boolean useLocalSession;
	private static Set<String> internalKeys = new HashSet<String>(Arrays.asList(Encryption.AJAX_ENCRYPTION_KEY, HttpContext.GX_NAV_HELPER, HttpContext.GXTheme, HttpContext.GXLanguage));
	private Object syncObject = new Object();

	public WebSession(IHttpServletRequest request) {
		this.request = request;
		sessionValues = null;
		updateSessionInvalidated();
	}

	private IHttpSession getSession() {
		return getSession(true);
	}

	private IHttpSession getSession(boolean createIfNotExists) {
		if (this.request == null || Thread.currentThread().getName().startsWith(SubmitThreadPool.SUBMIT_THREAD)) {
			if (session == null) {
				synchronized (syncObject) {
					if (session == null) {
						session = new LocalSession();
					}
				}
			}
			return session;
		}

		if (session == null && request != null){
			if (session == null) {
				synchronized (syncObject) {
					if (session == null) {
						session = request.getSession(createIfNotExists);
					}
				}
			}
		}
		return session;
	}

	public void invalidate() {
		session = new LocalSession();
	}

	public String getId() {
		return getSession().getId();
	}

	public void setValue(String key, String value) {
		setAttribute(key, value);
	}

	public String getValue(String key) {

		return getAttribute(key);
	}

	public void remove(String key) {

		removeAttribute(key);
	}

	public void setObjectAttribute(String key, Object value) {
		getSession(true).setAttribute(normalizeKey(key), value);
	}

	public void setAttribute(String key, String value) {
		setObjectAttribute(key, value);
	}

	public String getAttribute(String key) {
		Object out = getObjectAttribute(key);
		if (out == null)
			return "";
		else
			return (String) out;
	}

	public Object getObjectAttribute(String key) {
		return getSession(false).getAttribute(normalizeKey(key));
	}

	public void removeAttribute(String key) {
		getSession(false).removeAttribute(normalizeKey(key));
	}

	public void destroy() {
		getSession(false).invalidate();
	}

	public void renew() {
		IHttpSession s = getSession();
		Map<String, Object> internalValues = backupInternalKeys(s);
		s.invalidate();
		restoreInternalKeys(internalValues);
	}



	private void restoreInternalKeys(Map<String, Object> internalValues) {
		Iterator e = internalValues.keySet().iterator();
		while (e.hasNext()) {
			String key = (String) e.next();
			setObjectAttribute(key, internalValues.get(key));
		}
	}

	public void clear() {

			IHttpSession session = getSession(false);
			if (session != null) {
				Vector<String> toRemove = new Vector<>();
				Enumeration e = session.getAttributeNames();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					if (!internalKeys.contains(key)) {
						toRemove.add(key);
					}
				}
				e = toRemove.elements();
				while (e.hasMoreElements()) {
					remove((String) e.nextElement());
				}
				toRemove.clear();
			}

	}


	private Map<String, Object> backupInternalKeys(IHttpSession session) {
		Map<String, Object> internalValues = new HashMap<>();
		Enumeration e = session.getAttributeNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (internalKeys.contains(key)) {
				Object value = getObjectAttribute(key);
				if (value != null)
					internalValues.put(key, value);
			}
		}
		return internalValues;
	}

	private String normalizeKey(String key) {
		return CommonUtil.rtrim(CommonUtil.upper(key));
	}

	public static boolean isSessionExpired(IHttpServletRequest request) {
		return request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid();            //
	}
}

