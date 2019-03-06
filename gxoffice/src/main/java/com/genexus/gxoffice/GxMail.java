package com.genexus.gxoffice;

public class GxMail
{
  //static
  //{
  //	System.loadLibrary("gxoffice");
  //}

  public native short GetHandle( short[] Handle);

  public native short CloseHandle( short Handle);

  public native short LoginMAPI(short Handle, String Profile, short NewMessages, short MarkAsRead);

  public native short Logout(short Handle);

  public native short Send(short Handle, String To, String CC, String BCC, String Subject, String Text, String Attachs);

  public native void setEditWindow(short Handle, short _jcomparam_0);

  public native short getError(short Handle);

  public native void setDisplayMessages(short Handle, short _jcomparam_0);

  public native short Receive(short Handle, String[] From, String[] To, String[] CC, String[] Subject, String[] Text, String[] Attachs, java.util.Date[] DateSent, java.util.Date[] DateReceived);

  public native void setAttachDir(short Handle, String _jcomparam_0);

  public native void setAddressFormat(short Handle, short _jcomparam_0);

  public native void setMode(short Handle, String _jcomparam_0);

  public native short LoginSMTP(short Handle, String Server, String FromName, String FromAddress, String UserName, String Password, short Timeout);

  public native short LoginPOP3(short Handle, String Server, String UserName, String Password, short NewMessages, short Delete, short Timeout);

  public native short ChangeFolder(short Handle, String FolderName, short SkipRead, short MarkAsRead);

  public native int getCount(short Handle);

  public native void cleanup();
}
