package com.genexus.webpanels;


import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.genexus.CommonUtil;
import com.genexus.internet.HttpContext;
import com.genexus.util.Encryption;
import com.genexus.util.SubmitThreadPool;

public class WebSession {
	private HttpServletRequest request;
	private Hashtable<String, Object> sessionValues;
	private boolean useLocalSession;
	private static Set<String> internalKeys = new HashSet<String>(Arrays.asList(Encryption.AJAX_ENCRYPTION_KEY, HttpContext.GX_NAV_HELPER, HttpContext.GXTheme, HttpContext.GXLanguage));

	public WebSession(HttpServletRequest request) {
		this.request = request;
		sessionValues = null;
		updateSessionInvalidated();
	}

	private HttpSession getSession() {
		return getSession(true);
	}

	private HttpSession getSession(boolean createIfNotExists) {
		if (useLocalSession || request == null)
			return null;
		return request.getSession(createIfNotExists);
	}

	private void updateSessionInvalidated() {
		useLocalSession = useLocalSession || Thread.currentThread().getName().startsWith(SubmitThreadPool.SUBMIT_THREAD) || this.request == null;
	}

	private void putHashValue(String key, Object value) {
		if (sessionValues == null) {
			sessionValues = new Hashtable<>();
		}
		sessionValues.put(key, value);
	}

	private Object getHashValue(String key) {
		if (sessionValues != null) {
			return sessionValues.get(key);
		}
		return null;
	}

	private void removeHashValue(String key) {
		if (sessionValues != null) {
			sessionValues.remove(key);
		}
	}

	private void clearHashValues() {
		if (sessionValues != null) {
			sessionValues.clear();
			sessionValues = null;
		}
	}

	public void invalidate() {
		useLocalSession = true;
	}

	public String getId() {
		if (!useLocalSession) {
			return getSession().getId();
		}
		return "";
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
		updateSessionInvalidated();
		key = normalizeKey(key);
		HttpSession session = getSession(true);
		if (useLocalSession || session == null) {
			putHashValue(key, value);
			return;
		}
		session.setAttribute(key, value);
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
		updateSessionInvalidated();
		key = normalizeKey(key);
		Object out = null;
		if (useLocalSession) {
			return getHashValue(key);
		}
		HttpSession session = getSession(false);
		if (session != null) {
			out = session.getAttribute(key);
		}
		return out;
	}

	public void removeAttribute(String key) {
		updateSessionInvalidated();
		key = normalizeKey(key);
		if (useLocalSession) {
			removeHashValue(key);
			return;
		}
		HttpSession session = getSession(false);
		if (session != null) {
			session.removeAttribute(key);
		}
	}

	public void destroy() {
		updateSessionInvalidated();
		if (!useLocalSession) {
			HttpSession session = getSession(false);
			if (session != null) {
				session.invalidate();
			}
		} else {
			clear();
		}
	}

	public void renew() {
		updateSessionInvalidated();
		if (!useLocalSession) {
			HttpSession session = getSession(false);
			if (session != null) {
				Map<String, Object> internalValues = backupInternalKeys(session);
				session.invalidate();
				restoreInternalKeys(internalValues);
			}
		} else {
			clear();
		}
	}
	private Map<String, Object> backupInternalKeys(HttpSession session) {
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
	private void restoreInternalKeys(Map<String, Object> internalValues) {
		Iterator e = internalValues.keySet().iterator();
		while (e.hasNext()) {
			String key = (String) e.next();
			setObjectAttribute(key, internalValues.get(key));
		}
	}

	public void clear() {
		updateSessionInvalidated();
		if (!useLocalSession) {
			HttpSession session = getSession(false);
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
		} else {
			clearHashValues();
		}
	}

	private String normalizeKey(String key) {
		return CommonUtil.rtrim(CommonUtil.upper(key));
	}

	public static boolean isSessionExpired(HttpServletRequest request) {
		return request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid();            //
	}
}

