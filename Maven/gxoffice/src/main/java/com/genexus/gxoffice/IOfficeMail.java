package com.genexus.gxoffice;
import com.genexus.internet.*;

public interface IOfficeMail
{
  	public short	Send(GXMailMessage msg);
  	public short	Receive(GXMailMessage msg);
  	public short	ChangeFolder(String folder);
  	public short	Delete();
  	public short	MarkAsRead();
  	public void		setAttachDir( String newVal);
  	public String 	getAttachDir();
  	public void		setEditWindow( short newVal);
  	public short	getEditWindow();
  	public void		setNewMessages( short newVal);
  	public short	getNewMessages();
  	public long		getCount();
  	public short	getErrCode();
  	public String 	getErrDescription();
  	public void		setErrDisplay( short newVal);
  	public short	getErrDisplay();
  	public void		cleanup();
}
