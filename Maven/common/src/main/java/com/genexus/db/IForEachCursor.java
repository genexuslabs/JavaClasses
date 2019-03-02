package com.genexus.db;

import com.genexus.common.classes.AbstractDataSource;
import java.sql.SQLException;

interface IForEachCursor
{
    public void setStatus(int status);    
    public Object[] getBuffers();

    public IGXResultSet getResultSet();

    public boolean next(AbstractDataSource dataSource) throws SQLException;

    public int getStatus();

    public void setOutputBuffers(Object[] buffers);
    
    public void clearBuffers();
}
