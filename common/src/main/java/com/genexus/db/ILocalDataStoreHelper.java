package com.genexus.db;

import com.genexus.db.IFieldSetter;
import com.genexus.common.classes.IGXPreparedStatement;
import com.genexus.db.IFieldGetter;
import java.sql.SQLException;

public interface ILocalDataStoreHelper extends IDataStoreHelper
{
	void setParameters(int cursor, IFieldSetter stmt, Object[] buffers) throws SQLException;
	void getResults(int cursor, IFieldGetter rslt, Object[] buffers)	throws SQLException;
    void getErrorResults(int cursor, IFieldGetter rslt, Object[] buf) throws SQLException;
}
