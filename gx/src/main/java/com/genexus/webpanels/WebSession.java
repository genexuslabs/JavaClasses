package com.genexus.webpanels;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.genexus.CommonUtil;
import com.genexus.internet.HttpContext;
import com.genexus.util.Encryption;
import com.genexus.util.SubmitThreadPool;

public class WebSession
{
	private HttpServletRequest request;
        private Hashtable sessionValues;
        private boolean invalidated;

	public WebSession(HttpServletRequest request)
	{
		this.request = request;
                sessionValues = null;
                updateSessionInvalidated();
	}

	private HttpSession getSession() throws Exception
	{
		return getSession(true);
	}

	private HttpSession getSession(boolean createIfNotExists) throws Exception
	{
		if (invalidated)
			throw new Exception("WebSession is invalid");
		return request.getSession(createIfNotExists);
	}

        private void updateSessionInvalidated()
        {
          invalidated = Thread.currentThread().getName().startsWith(SubmitThreadPool.SUBMIT_THREAD);
        }

        private void putHashValue(String key, Object value)
        {
          if (sessionValues == null)
          {
            sessionValues = new Hashtable();
          }
          sessionValues.put(key, value);
        }

        private Object getHashValue(String key)
        {
          if (sessionValues != null)
          {
            return sessionValues.get(key);
          }
          return null;
        }

        private void removeHashValue(String key)
        {
          if (sessionValues != null)
          {
            sessionValues.remove(key);
          }
        }

        private void clearHashValues()
        {
          if (sessionValues != null)
          {
            sessionValues.clear();
            sessionValues = null;
          }
        }

        public void invalidate()
        {
            invalidated = true;
        }

	public String getId()
	{
                try
                {
                  return getSession().getId();
                }
                catch(Throwable e) {} //Invalidated
		return "";
	}

	public void setValue(String key, String value)
	{
		setAttribute(key, value);
	}

	public String getValue(String key)
	{
		return getAttribute(key);
	}

	public void remove(String key)
	{
		removeAttribute(key);
	}

	public void setObjectAttribute(String key, Object value)
	{
        updateSessionInvalidated();
        key = normalizeKey(key);
		try
		{
	        getSession(true).setAttribute(key, value);
		}
		catch(Throwable e) // Invalidated
        {
	        putHashValue(key, value);
        }
	}

	public void setAttribute(String key, String value)
	{
		setObjectAttribute(key, value);
	}

	public String getAttribute(String key)
	{
		Object out = getObjectAttribute(key);
		if	(out == null)
			return "";
		else
	 		return (String)out;
	}

	public Object getObjectAttribute(String key)
	{
        updateSessionInvalidated();
        key = normalizeKey(key);
		Object out = null;
		try
		{
			HttpSession session = getSession(false);
			if (session != null)
			{
				out = session.getAttribute(key);				
			}					
		} catch (Throwable e) // Invalidated
		{
			out = getHashValue(key);
        }
		return out;
	}

	public void removeAttribute(String key)
	{
			updateSessionInvalidated();
                key = normalizeKey(key);
		try
		{
			HttpSession session = getSession(false);
			if (session != null)
			{
				session.removeAttribute(key);
			}			
		} catch (Throwable e) // Invalidated
		{
			removeHashValue(key);
		}
	}

	public void destroy()
	{
		updateSessionInvalidated();
		try
		{
			HttpSession session = getSession(false);
			if (session != null)
			{
				session.invalidate();
			}	
		} catch (Throwable e) // Invalidated
		{
			clear();
		}
	}

	public void clear()
	{
		updateSessionInvalidated();
		try
		{
			
			HttpSession session = getSession(false);
			if (session != null)
			{
				Vector toRemove = new Vector();
				Enumeration e = session.getAttributeNames();
				while (e.hasMoreElements())
				{
					String key = (String) e.nextElement();
					if (!key.equals(Encryption.AJAX_ENCRYPTION_KEY) && !key.equals(HttpContext.GX_NAV_HELPER))
					{
						toRemove.add(key);
					}
				}
				e = toRemove.elements();
				while (e.hasMoreElements())
				{
					remove((String) e.nextElement());
				}
				toRemove.clear();
			}
		} catch (Throwable ex) // Invalidated
		{
			clearHashValues();
		}
	}

	private String normalizeKey(String key)
	{
		return CommonUtil.rtrim(CommonUtil.upper(key));
	}

	public static boolean isSessionExpired(HttpServletRequest request)
	{		
		return request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid();		    //
	}
}

