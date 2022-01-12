package com.genexus.common.interfaces;

public interface IGXWSAddressing
{

	String getTo();
	void setTo(String to);
	String getAction();
	void setAction(String action);
	String getMessageID();
	void setMessageID(String messageID);
	String getRelatesTo();
	void setRelatesTo(String relatesTo);
	IGXWSAddressingEndPoint getFrom();
	void setFrom(IGXWSAddressingEndPoint from);
	IGXWSAddressingEndPoint getReplyTo();
	void setReplyTo(IGXWSAddressingEndPoint replyTo);
	IGXWSAddressingEndPoint getFaultTo();
	void setFaultTo(IGXWSAddressingEndPoint faultTo);
}

