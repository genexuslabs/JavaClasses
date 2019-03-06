package com.genexus.internet;

public interface IPOP3Session
{
  public int getMessageCount() throws GXMailException;
  public void login(GXPOP3Session sessionInfo);
  public void logout(GXPOP3Session sessionInfo);
  public void skip(GXPOP3Session sessionInfo);
  public String getNextUID() throws GXMailException;
  public void receive(GXPOP3Session sessionInfo, GXMailMessage gxmessage);
  public void delete(GXPOP3Session sessionInfo);
}