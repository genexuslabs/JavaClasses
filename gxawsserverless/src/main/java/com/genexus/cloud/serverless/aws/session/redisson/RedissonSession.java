package com.genexus.cloud.serverless.aws.session.redisson;

import com.genexus.cloud.serverless.aws.session.redisson.RedissonSessionManager.UpdateMode;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RedissonSession implements HttpSession {
	private static final String IS_NEW_ATTR = "session:isNew";
	private static final String IS_VALID_ATTR = "session:isValid";
	private static final String THIS_ACCESSED_TIME_ATTR = "session:thisAccessedTime";
	private static final String MAX_INACTIVE_INTERVAL_ATTR = "session:maxInactiveInterval";
	private static final String LAST_ACCESSED_TIME_ATTR = "session:lastAccessedTime";
	private static final String CREATION_TIME_ATTR = "session:creationTime";
	private static final String IS_EXPIRATION_LOCKED = "session:isExpirationLocked";
	private static final String PRINCIPAL_ATTR = "session:principal";
	private static final String AUTHTYPE_ATTR = "session:authtype";


	public static final Set<String> ATTRS = new HashSet<String>(Arrays.asList(
		IS_NEW_ATTR, IS_VALID_ATTR,
		THIS_ACCESSED_TIME_ATTR, MAX_INACTIVE_INTERVAL_ATTR,
		LAST_ACCESSED_TIME_ATTR, CREATION_TIME_ATTR, IS_EXPIRATION_LOCKED,
		PRINCIPAL_ATTR, AUTHTYPE_ATTR
	));

	/**
	 * The session identifier of this Session.
	 */
	protected String id = null;
	private boolean isExpirationLocked;
	private boolean loaded;
	private final RedissonSessionManager redissonManager;
	private final Map<String, Object> attrs;
	private RMap<String, Object> map;
	private final RTopic topic;
	private final RedissonSessionManager.UpdateMode updateMode;

	private final AtomicInteger usages = new AtomicInteger();
	private Map<String, Object> loadedAttributes = Collections.emptyMap();
	private Map<String, Object> updatedAttributes = Collections.emptyMap();
	private Set<String> removedAttributes = Collections.emptySet();

	private final boolean broadcastSessionEvents;
	private final boolean broadcastSessionUpdates;

	/**
	 * Flag indicating whether this session is valid or not.
	 */
	protected volatile boolean isValid = false;

	/**
	 * The time this session was created, in milliseconds since midnight,
	 * January 1, 1970 GMT.
	 */
	protected long creationTime = 0L;

	/**
	 * The last accessed time for this Session.
	 */
	protected volatile long lastAccessedTime = creationTime;

	/**
	 * The current accessed time for this session.
	 */
	protected volatile long thisAccessedTime = creationTime;

	/**
	 * We are currently processing a session expiration, so bypass
	 * certain IllegalStateException tests.  NOTE:  This value is not
	 * included in the serialized version of this object.
	 */
	protected transient volatile boolean expiring = false;

	/**
	 * The maximum time interval, in seconds, between client requests before
	 * the servlet container may invalidate this session.  A negative time
	 * indicates that the session should never time out.
	 */
	protected int maxInactiveInterval = -1;
	/**
	 * Flag indicating whether this session is new or not.
	 */
	protected boolean isNew = false;

	/**
	 * The authenticated Principal associated with this session, if any.
	 * <b>IMPLEMENTATION NOTE:</b>  This object is <i>not</i> saved and
	 * restored across session serializations!
	 */
	protected transient Principal principal = null;

	/**
	 * The string manager for this package.
	 */
	protected static final StringManager sm =
		StringManager.getManager("com.genexus.cloud.serverless.aws.session.redisson");

	/**
	 * The authentication type used to authenticate our cached Principal,
	 * if any.  NOTE:  This value is not included in the serialized
	 * version of this object.
	 */
	protected transient String authType = null;

	public RedissonSession(RedissonSessionManager manager, UpdateMode updateMode, boolean broadcastSessionEvents, boolean broadcastSessionUpdates) {
		this.redissonManager = manager;
		this.updateMode = updateMode;
		this.topic = redissonManager.getTopic();
		this.broadcastSessionEvents = broadcastSessionEvents;
		this.broadcastSessionUpdates = broadcastSessionUpdates;

		if (updateMode == UpdateMode.AFTER_REQUEST) {
			removedAttributes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		}

		try {
			Field attr = StandardSession.class.getDeclaredField("attributes");
			attrs = (Map<String, Object>) attr.get(this);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static final long serialVersionUID = -2518607181636076487L;

	@Override
	public Object getAttribute(String name) {
			if (!isValidInternal()) {
				throw new IllegalStateException(sm.getString("standardSession.getAttribute.ise"));
			}

			if (name == null) {
				return null;
			}

			if (removedAttributes.contains(name)) {
				return getAttribute(name);
			}

			Object value = loadedAttributes.get(name);
			if (value == null) {
				value = map.get(name);
				if (value != null) {
					loadedAttributes.put(name, value);
				}
			}

			return value;
	}

	@Override
	public Object getValue(String name) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {

			if (!isValidInternal()) {
				throw new IllegalStateException
					(sm.getString("standardSession.getAttributeNames.ise"));
			}

			Set<String> attributeKeys = new HashSet<>();
			attributeKeys.addAll(map.readAllKeySet());
			attributeKeys.addAll(loadedAttributes.keySet());
			return Collections.enumeration(attributeKeys);
	}

	@Override
	public String[] getValueNames() {

			if (!isValidInternal()) {
				throw new IllegalStateException
					(sm.getString("standardSession.getAttributeNames.ise"));
			}
			Set<String> keys = map.readAllKeySet();
			return keys.toArray(new String[0]);
	}

	@Override
	public void setAttribute(String name, Object value) {

	}

	@Override
	public void putValue(String name, Object value) {

	}

	@Override
	public void removeAttribute(String name) {

	}

	@Override
	public void removeValue(String name) {

	}

	@Override
	public void invalidate() {

	}

	@Override
	public boolean isNew() {
		return false;
	}

	/**
	 * Return the <code>isValid</code> flag for this session without any expiration
	 * check.
	 */
	protected boolean isValidInternal() {
		return this.isValid;
	}

	public void delete() {
		if (map == null) {
			map = redissonManager.getMap(id);
		}

		if (broadcastSessionEvents) {
			RSet<String> set = redissonManager.getNotifiedNodes(id);
			set.add(redissonManager.getNodeId());
			set.expire(Duration.ofSeconds(60));
			map.fastPut(IS_EXPIRATION_LOCKED, true);
			map.expire(Duration.ofSeconds(60));
		} else {
			map.delete();
		}
		map = null;
		loadedAttributes.clear();
		updatedAttributes.clear();
	}

	/*
	@Override
	public void setCreationTime(long time) {

		if (map != null) {
			Map<String, Object> newMap = new HashMap<String, Object>(3);
			newMap.put(CREATION_TIME_ATTR, creationTime);
			newMap.put(LAST_ACCESSED_TIME_ATTR, lastAccessedTime);
			newMap.put(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
			map.putAll(newMap);
		}
	}*/

	public void access() {
		if (map != null) {
			fastPut(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
			expireSession();
		}
	}

	protected void expireSession() {
		if (isExpirationLocked) {
			return;
		}
		if (maxInactiveInterval >= 0) {
			map.expire(Duration.ofSeconds(maxInactiveInterval + 60));
		}
	}

	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {

		if (map != null) {
			fastPut(MAX_INACTIVE_INTERVAL_ATTR, maxInactiveInterval);
			expireSession();
		}
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	private void fastPut(String name, Object value) {
		if (map == null) {
			return;
		}
		map.fastPut(name, value);
	}

	public void setPrincipal(Principal principal) {
		if (principal == null) {
			removeRedisAttribute(PRINCIPAL_ATTR);
		} else {
			fastPut(PRINCIPAL_ATTR, principal);
		}
	}

	public void setAuthType(String authType) {
		if (authType == null) {
			removeRedisAttribute(AUTHTYPE_ATTR);
		} else {
			fastPut(AUTHTYPE_ATTR, authType);
		}
	}

	public void setValid(boolean isValid) {
		isValid = isValid;
		if (map != null) {
			if (!isValid && !map.isExists()) {
				return;
			}
			fastPut(IS_VALID_ATTR, isValid);
		}
	}
/*
	@Override
	public void setNew(boolean isNew) {
		super.setNew(isNew);

		if (map != null) {
			fastPut(IS_NEW_ATTR, isNew);
		}
	}
*/
	/*
	@Override
	public void endAccess() {
		boolean oldValue = isNew;
		super.endAccess();

		if (map != null) {
			Map<String, Object> newMap = new HashMap<>(3);
			if (isNew != oldValue) {
				newMap.put(IS_NEW_ATTR, isNew);
			}
			newMap.put(LAST_ACCESSED_TIME_ATTR, lastAccessedTime);
			newMap.put(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
			map.putAll(newMap);
			expireSession();
		}
	}

	 */


	public void superSetAttribute(String name, Object value, boolean notify) {
		setAttribute(name, value, notify);
	}

	public void setAttribute(String name, Object value, boolean notify) {

		if (value == null) {
			return;
		}
		if (updateMode == UpdateMode.DEFAULT && map != null) {
			fastPut(name, value);
		}

		loadedAttributes.put(name, value);
		updatedAttributes.put(name, value);

		if (updateMode == UpdateMode.AFTER_REQUEST) {
			removedAttributes.remove(name);
		}
	}

	public void superRemoveAttributeInternal(String name, boolean notify) {
		removeAttributeInternal(name, notify);
	}

	/*@Override
	public long getIdleTimeInternal() {
		long idleTime = super.getIdleTimeInternal();
		if (map != null && readMode == ReadMode.REDIS) {
			if (idleTime >= getMaxInactiveInterval() * 1000) {
				load(map.getAll(RedissonSession.ATTRS));
				idleTime = super.getIdleTimeInternal();
			}
		}
		return idleTime;
	}*/

	protected void removeAttributeInternal(String name, boolean notify) {
		removeRedisAttribute(name);
	}

	private void removeRedisAttribute(String name) {
		if (updateMode == UpdateMode.DEFAULT && map != null) {
			map.fastRemove(name);
		}

			loadedAttributes.remove(name);
			updatedAttributes.remove(name);

		if (updateMode == UpdateMode.AFTER_REQUEST) {
			removedAttributes.add(name);
		}
	}

	public void save() {
		if (map == null) {
			map = redissonManager.getMap(id);
		}

		Map<String, Object> newMap = new HashMap<String, Object>();
		newMap.put(CREATION_TIME_ATTR, creationTime);
		newMap.put(LAST_ACCESSED_TIME_ATTR, lastAccessedTime);
		newMap.put(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
		newMap.put(MAX_INACTIVE_INTERVAL_ATTR, maxInactiveInterval);
		newMap.put(IS_VALID_ATTR, isValid);
		newMap.put(IS_NEW_ATTR, isNew);
		if (principal != null) {
			newMap.put(PRINCIPAL_ATTR, principal);
		}
		if (authType != null) {
			newMap.put(AUTHTYPE_ATTR, authType);
		}
		if (broadcastSessionEvents) {
			newMap.put(IS_EXPIRATION_LOCKED, isExpirationLocked);
		}

		newMap.putAll(updatedAttributes);
		updatedAttributes.clear();

		map.putAll(newMap);
		map.fastRemove(removedAttributes.toArray(new String[0]));

		removedAttributes.clear();

		expireSession();
	}

	public void load(Map<String, Object> attrs) {
		Long creationTime = (Long) attrs.remove(CREATION_TIME_ATTR);
		if (creationTime != null) {
			this.creationTime = creationTime;
		}
		Long lastAccessedTime = (Long) attrs.remove(LAST_ACCESSED_TIME_ATTR);
		if (lastAccessedTime != null) {
			this.lastAccessedTime = lastAccessedTime;
		}
		Integer maxInactiveInterval = (Integer) attrs.remove(MAX_INACTIVE_INTERVAL_ATTR);
		if (maxInactiveInterval != null) {
			this.maxInactiveInterval = maxInactiveInterval;
		}
		Long thisAccessedTime = (Long) attrs.remove(THIS_ACCESSED_TIME_ATTR);
		if (thisAccessedTime != null) {
			this.thisAccessedTime = thisAccessedTime;
		}
		Boolean isValid = (Boolean) attrs.remove(IS_VALID_ATTR);
		if (isValid != null) {
			this.isValid = isValid;
		}
		Boolean isNew = (Boolean) attrs.remove(IS_NEW_ATTR);
		if (isNew != null) {
			this.isNew = isNew;
		}
		Boolean isExpirationLocked = (Boolean) attrs.remove(IS_EXPIRATION_LOCKED);
		if (isExpirationLocked != null) {
			this.isExpirationLocked = isExpirationLocked;
		}
		Principal p = (Principal) attrs.remove(PRINCIPAL_ATTR);
		if (p != null) {
			this.principal = p;
		}
		String authType = (String) attrs.remove(AUTHTYPE_ATTR);
		if (authType != null) {
			this.authType = authType;
		}

	}

	public void recycle() {
		map = null;
		loadedAttributes.clear();
		updatedAttributes.clear();
		removedAttributes.clear();
	}

	public void startUsage() {
		usages.incrementAndGet();
	}

	public void endUsage() {
		// don't decrement usages if startUsage wasn't called
//        if (usages.decrementAndGet() == 0) {
		if (usages.get() == 0 || usages.decrementAndGet() == 0) {
			loadedAttributes.clear();
		}
	}
}
