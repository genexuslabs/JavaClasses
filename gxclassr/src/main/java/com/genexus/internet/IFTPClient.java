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