// $Log: IMailImplementation.java,v $
// Revision 1.1  2001/09/27 18:10:46  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/09/27 18:10:46  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

public interface IMailImplementation
{
	void MAPIChangeFolder(String folder, int newMessages, int markAsRead);
	void MAPIEditWindow(int val);
	void setMode(String mode);

	void MAPILogin(String profile, int newMessages, int markAsRead);
	void POP3Login(String host, int port, String user, String password, int newMessages, int delete, int timeout);
	void SMTPLogin(String host, int port, String name, String address, String user, String password, int timeout, int authentication);

	void send(GXMailMessage msg);
	void receive(GXMailMessage msg);

	void logout();
	void SMTPLogout();
	int getMessageCount();

	void displayMessage(String message);
	//void setAddressFormat(int format);
	void setAttachDir(String dir);

	int getErrCode();
	String getErrDescription();

	void cleanup();
}