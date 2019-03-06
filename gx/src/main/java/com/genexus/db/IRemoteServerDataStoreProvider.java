package com.genexus.db;

public interface IRemoteServerDataStoreProvider
{
	public byte[] execute(byte[] parms);
	public byte[] readNext(int cursor) ;
	public void close(int cursor) ;
	public void release();
}
