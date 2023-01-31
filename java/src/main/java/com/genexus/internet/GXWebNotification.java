package com.genexus.internet;
import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.websocket.GXWebSocketService;
import com.genexus.internet.websocket.SendResponseType;
import com.genexus.xml.GXXMLSerializable;

public class GXWebNotification {

	public static final ILogger logger = LogManager.getLogger(GXWebNotification.class);
	private GXWebSocketService wsService;
    private HttpContext httpContext;
	
	private short errCode;
    private String errDescription;

	public short getErrCode()
	{
		return errCode;
	}
	
	public String getErrDescription()
	{
		return errDescription;
	}
	
    public GXWebNotification(ModelContext gxContext)
    {
		wsService = GXWebSocketService.getService();
		httpContext = (HttpContext) gxContext.getHttpContext();
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
		SendResponseType result = wsService.send(clientId.trim(), message);
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

		return errCode;
    }

	public short notify(String message)
    {
		return notifyClient(httpContext.getClientId(), message);
    }
	
	
    public short notify(GXXMLSerializable message)
    {
		return notifyClient(httpContext.getClientId(), message);
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
		wsService.broadcast(message);
		setError((short)0);
    }
	
	private void setError(short i)
	{
		this.errCode = i;
		switch (i)
		{
			case 0:
				errDescription = "OK";
				break;
			case 1:
				errDescription = "WebSocket Server has not been initialized yet. No incoming connections were received";
				break;
			case 2:
				errDescription = "WebSocket Session not found. The client is not connected to socket server";
				break;
			case 3:
				errDescription = "WebSocket Session was found, but it's state was closed or invalid";
				break;
			case 4:
				errDescription = "Message could not be delivered to client because of a connection error";
				break;				
			default:
				errDescription = "Unknown error";
				break;
		}
	}
		
	public String getClientId()
	{
		return httpContext.getClientId();
	}
}
