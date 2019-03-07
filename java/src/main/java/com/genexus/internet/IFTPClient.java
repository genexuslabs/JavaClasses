// $Log: IFTPClient.java,v $
// Revision 1.2  2002/10/15 18:47:45  aaguiar
// - Se agrego una funcion gxftpcmd para ejecutar un comando dado
//
// Revision 1.1.1.1  2001/10/26 18:09:56  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/10/26 18:09:56  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

public interface IFTPClient
{
	int connect(String host, String user, String password);
	int disconnect();
	String status();
	void status(String[] status);
	int get(String source, String target, String mode);
	int put(String source, String target, String mode);
	int command(String command);
	int delete(String source);
	int mkdir(String path);
	void lastError(int[] lastError);
	void setPassive(boolean passive);
}