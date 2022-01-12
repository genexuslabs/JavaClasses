package com.genexus.ws.addressing;

import com.genexus.common.interfaces.IGXWSAddressing;
import com.genexus.common.interfaces.IGXWSAddressingEndPoint;

public class GXWSAddressing implements IGXWSAddressing
{
	private String to;
	private String action;
	private String messageID;
	private String relatesTo;
	private IGXWSAddressingEndPoint from;
	private IGXWSAddressingEndPoint replyTo;
	private IGXWSAddressingEndPoint faultTo;
	

	public GXWSAddressing()
	{
		to = "";
		action = "";
		messageID = "";
		relatesTo = "";
		from = new GXWSAddressingEndPoint();
		replyTo = new GXWSAddressingEndPoint();
		faultTo = new GXWSAddressingEndPoint();
	}

	public String getTo()
	{
		return to;
	}

	public void setTo(String to)
	{
		this.to = to.trim();
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action.trim();
	}

	public String getMessageID()
	{
		return messageID;
	}

	public void setMessageID(String messageID)
	{
		this.messageID = messageID.trim();
	}	
	
	public String getRelatesTo()
	{
		return relatesTo;
	}

	public void setRelatesTo(String relatesTo)
	{
		this.relatesTo = relatesTo.trim();
	}	
	
	public IGXWSAddressingEndPoint getFrom()
	{
		return from;
	}

	public void setFrom(IGXWSAddressingEndPoint from)
	{
		this.from = from;
	}	
	
	public IGXWSAddressingEndPoint getReplyTo()
	{
		return replyTo;
	}

	public void setReplyTo(IGXWSAddressingEndPoint replyTo)
	{
		this.replyTo = replyTo;
	}
	
	public IGXWSAddressingEndPoint getFaultTo()
	{
		return faultTo;
	}

	public void setFaultTo(IGXWSAddressingEndPoint faultTo)
	{
		this.faultTo = faultTo;
	}		


}

