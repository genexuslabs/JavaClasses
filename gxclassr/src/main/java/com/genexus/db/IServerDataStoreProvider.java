package com.genexus.db;

import com.genexus.GXDBException;

public interface IServerDataStoreProvider
{
	public byte[] execute(byte[] parms) throws GXDBException;
	public byte[] readNext(int cursor) throws GXDBException;
	public void close(int cursor) throws GXDBException;
}
