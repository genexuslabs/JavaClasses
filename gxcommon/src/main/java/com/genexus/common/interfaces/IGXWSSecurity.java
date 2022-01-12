package com.genexus.common.interfaces;

public interface IGXWSSecurity
{
	public IGXWSSignature getSignature();
	void setSignature(IGXWSSignature signature);
	IGXWSEncryption getEncryption();
	void setEncryption(IGXWSEncryption encryption);
}

