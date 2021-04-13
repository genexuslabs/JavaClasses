package com.genexus.servlet.http;

public abstract class SessionEventListenerWrapper implements javax.servlet.http.HttpSessionListener, javax.servlet.http.HttpSessionActivationListener, javax.servlet.http.HttpSessionBindingListener{
	public abstract void sessionCreated(HttpSessionEvent se);
	public abstract void sessionDestroyed(HttpSessionEvent se);
	public abstract void sessionDidActivate(HttpSessionEvent se);
	public abstract void sessionWillPassivate(HttpSessionEvent se);
	public abstract void valueBound(HttpSessionBindingEvent se);
	public abstract void valueUnbound(HttpSessionBindingEvent se);

	public void sessionCreated(javax.servlet.http.HttpSessionEvent se)
	{
		sessionCreated(new HttpSessionEvent(se));
	}

	public void sessionDestroyed(javax.servlet.http.HttpSessionEvent se)
	{
		sessionDestroyed(new HttpSessionEvent(se));
	}

	public void sessionDidActivate(javax.servlet.http.HttpSessionEvent se)
	{
		sessionDidActivate(new HttpSessionEvent(se));
	}

	public void sessionWillPassivate(javax.servlet.http.HttpSessionEvent se)
	{
		sessionWillPassivate(new HttpSessionEvent(se));
	}

	public void valueBound(javax.servlet.http.HttpSessionBindingEvent se)
	{
		valueBound((HttpSessionBindingEvent)se);
	}

	public void valueUnbound(javax.servlet.http.HttpSessionBindingEvent se)
	{
		valueUnbound((HttpSessionBindingEvent)se);
	}
}
