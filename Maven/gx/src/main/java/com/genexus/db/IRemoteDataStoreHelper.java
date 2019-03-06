package com.genexus.db;

import java.sql.SQLException;

import com.genexus.GXParameterPacker;
import com.genexus.GXParameterUnpacker;

public interface IRemoteDataStoreHelper extends IDataStoreHelper
{
	void setParameters(int cursor, IFieldSetter stmt, GXParameterUnpacker unpacker) throws SQLException;
	void getResults(int cursor, IFieldGetter sent, GXParameterPacker packer)	throws SQLException;
	int getReadBuffer();
}
