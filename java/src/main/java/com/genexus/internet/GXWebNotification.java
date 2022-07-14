package com.genexus.internet;
import com.genexus.Application;
import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.websocket.IGXWebSocketService;
import com.genexus.internet.websocket.SendResponseType;
import com.genexus.xml.GXXMLSerializable;

public class GXWebNotification {

	public static final ILogger logger = LogManager.getLogger(GXWebNotification.class);
	private static IGXWebSocketService _ws;
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
        _ctx = (HttpContext) gxContext.getHttpContext();
    }

	private IGXWebSocketService getWSService() {
		if (_ws == null)
		{
			_ws = Application.getSocketService();
		}
		return _ws;
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
		IGXWebSocketService ws = getWSService();
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
		else {
			setError((short) 1);
		}
		
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
		IGXWebSocketService ws = getWSService();
        if (ws != null)
		{
			ws.broadcast(message);   
			setError((short)0);
		}
		else {
			setError((short) 1);
		}
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
				_errDescription = "WebSocket Server has not been initialized yet. No incoming connections were received";
				break;
			case 2:
				_errDescription = "WebSocket Session not found. The client is not connected to socket server";
				break;
			case 3:
				_errDescription = "WebSocket Session was found, but it's state was closed or invalid";
				break;
			case 4:
				_errDescription = "Message could not be delivered to client because of a connection error";
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
