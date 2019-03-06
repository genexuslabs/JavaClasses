package com.genexus.gxoffice;
import com.genexus.internet.*;

public class MAPISession implements IOfficeMail
{
  static
  {
        System.loadLibrary("gxoffice2");
  }

  public short Index = -1;

  public native short	Login();

  public native short	Logout();

  public native short	Send(GXMailMessage msg);
 
  public native short	Receive(GXMailMessage msg);

  public native short	ChangeFolder(String folder);

  public native short	Delete();

  public native short	MarkAsRead();

  public native void	setAttachDir( String newVal);
  public native String	getAttachDir();

  public native void	setProfile( String newVal);
  public native String	getProfile();

  public native void	setEditWindow( short newVal);
  public native short	getEditWindow();

  public native void	setNewMessages( short newVal);
  public native short	getNewMessages();

  public native long	getCount();

  public native short	getErrCode();

  public native String	getErrDescription();

  public native void	setErrDisplay( short newVal);
  public native short	getErrDisplay();

  public native void	cleanup();
}
