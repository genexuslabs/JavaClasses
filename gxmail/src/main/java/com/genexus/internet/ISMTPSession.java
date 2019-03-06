package com.genexus.internet;

public interface ISMTPSession
{
	
  public void login(GXSMTPSession sessionInfo);

  public void send(GXSMTPSession sessionInfo, GXMailMessage msg);

  public void logout(GXSMTPSession sessionInfo);
}