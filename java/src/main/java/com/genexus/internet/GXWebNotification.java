package com.genexus.internet;
import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.websocket.IGXWebSocketAsync;
import com.genexus.internet.websocket.SendResponseType;
import com.genexus.xml.GXXMLSerializable;

public class GXWebNotification {

	public static final ILogger logger = LogManager.getLogger(GXWebNotification.class);
	
	private static IGXWebSocketAsync ws;
    private HttpContext _ctx;
	
	private short _errCode;
    private String _errDescription;
		
		
	public short getErrCode()
	{
		return _errCode;
	}
	
	public String getErrDescription()
	{
		return _errDescription;
	}
	
    public GXWebNotification(ModelContext gxContext)
    {    	
        _ctx = gxContext.getHttpContext();
        if (ws == null)
        {        			
			try {
				setError((short)1);
				Class c = Class.forName("com.genexus.internet.websocket.GXWebSocket");
				java.lang.reflect.Method method = c.getDeclaredMethod("getInstance", (Class[])null);
				Object o = method.invoke(null, (Object[])null);	
				ws = (IGXWebSocketAsync)o;		
				if (ws != null)
					setError((short)0);
			} catch (Exception e) {
				logger.error("GXWebNotification", e);
			}
        } 
		if (_errCode != 0)
		{
			logger.error("Could not create com.genexus.internet.GXWebSocket instance. Check whether WebServer requirements are met and WebNotifications Provider Generator Property is set");
		}
    }
		
	public short notifyClient(String clientId, String message)
	{            		    
		return notifyClientImpl(clientId, message);
	}
	
	public short notifyClient(String clientId, GXXMLSerializable message)
	{            
		String msg = message.toJSonString();            
		return notifyClientImpl(clientId, msg);
	}
	
    public short notifyClientImpl(String clientId, String message)
    {        	
		if (ws != null) 
		{
			SendResponseType result = ws.send(clientId.trim(), message);
			switch (result)
			{
				case OK:
					setError((short)0);
					break;
				case SessionNotFound:
					setError((short)2);
					break;
				case SessionInvalid:
					setError((short)3);
					break;   			
				case SendFailed:
					setError((short)4);
					break;   
				default:
					break;
			}
		}
		else
			setError((short)1);
		
		return _errCode;
    }

	public short notify(String message)
    {
		return notifyClient(_ctx.getClientId(), message);   
    }
	
	
    public short notify(GXXMLSerializable message)
    {
		return notifyClient(_ctx.getClientId(), message);   
    }
	

    public void broadcast(GXXMLSerializable message)
    {               	
		broadcastImpl(message.toJSonString());           	
    }
	
	public void broadcast(String message)
    {                	
        broadcastImpl(message);           
    }
	
	private void broadcastImpl(String message)
    {                	
        if (ws != null)
		{
			ws.broadcast(message);   
			setError((short)0);
		}
		else
			setError((short)1);        
    }
	
	private void setError(short i)
	{
		this._errCode = i;
		switch (i)
		{
			case 0:
				_errDescription = "OK";
				break;
			case 1:
				_errDescription = "Could not start WebSocket Server";
				break;
			case 2:
				_errDescription = "WebSocket Session not found";
				break;
			case 3:
				_errDescription = "WebSocket Session is closed or invalid";
				break;
			case 4:
				_errDescription = "Message could not be delivered to client";
				break;				
			default:
				_errDescription = "Unknown error";
				break;
		}
	}
		
	public String getClientId()
	{
		return _ctx.getClientId();
	}
}
