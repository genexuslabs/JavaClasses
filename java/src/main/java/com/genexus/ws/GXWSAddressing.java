package com.genexus.ws;

public class GXWSAddressing
{
	private String to;
	private String action;
	private String messageID;
	private String relatesTo;
	private GXWSAddressingEndPoint from;
	private GXWSAddressingEndPoint replyTo;
	private GXWSAddressingEndPoint faultTo;
	

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
	
	public GXWSAddressingEndPoint getFrom()
	{
		return from;
	}

	public void setFrom(GXWSAddressingEndPoint from)
	{
		this.from = from;
	}	
	
	public GXWSAddressingEndPoint getReplyTo()
	{
		return replyTo;
	}

	public void setReplyTo(GXWSAddressingEndPoint replyTo)
	{
		this.replyTo = replyTo;
	}
	
	public GXWSAddressingEndPoint getFaultTo()
	{
		return faultTo;
	}

	public void setFaultTo(GXWSAddressingEndPoint faultTo)
	{
		this.faultTo = faultTo;
	}		


}

