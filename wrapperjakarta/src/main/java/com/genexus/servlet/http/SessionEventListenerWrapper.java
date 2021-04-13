package com.genexus.servlet.http;

public abstract class SessionEventListenerWrapper implements jakarta.servlet.http.HttpSessionListener, jakarta.servlet.http.HttpSessionActivationListener, jakarta.servlet.http.HttpSessionBindingListener{
	public abstract void sessionCreated(HttpSessionEvent se);
	public abstract void sessionDestroyed(HttpSessionEvent se);
	public abstract void sessionDidActivate(HttpSessionEvent se);
	public abstract void sessionWillPassivate(HttpSessionEvent se);
	public abstract void valueBound(HttpSessionBindingEvent se);
	public abstract void valueUnbound(HttpSessionBindingEvent se);

	public void sessionCreated(jakarta.servlet.http.HttpSessionEvent se)
	{
		sessionCreated(new HttpSessionEvent(se));
	}

	public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent se)
	{
		sessionDestroyed(new HttpSessionEvent(se));
	}

	public void sessionDidActivate(jakarta.servlet.http.HttpSessionEvent se)
	{
		sessionDidActivate(new HttpSessionEvent(se));
	}

	public void sessionWillPassivate(jakarta.servlet.http.HttpSessionEvent se)
	{
		sessionWillPassivate(new HttpSessionEvent(se));
	}

	public void valueBound(jakarta.servlet.http.HttpSessionBindingEvent se)
	{
		valueBound((HttpSessionBindingEvent)se);
	}

	public void valueUnbound(jakarta.servlet.http.HttpSessionBindingEvent se)
	{
		valueUnbound((HttpSessionBindingEvent)se);
	}
}
