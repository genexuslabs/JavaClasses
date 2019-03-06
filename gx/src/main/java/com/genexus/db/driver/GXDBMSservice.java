package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.genexus.CommonUtil;
import com.genexus.db.service.ServiceError;
import com.genexus.db.service.ServiceException;

public class GXDBMSservice implements GXDBMS
{
    public static final String DATASOURCE_NAME = "servicedatasource";
    
    @Override
    public void setDatabaseName(String dbName)
    {
    }
    @Override
    public String getDatabaseName()
    {
            return "";
    }	
    @Override
    public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
    {
        try
        {
            return stmt.executeQuery();
        }catch(ServiceException e)
        {
            throw new SQLException(e.getMessage(), e.getSQLState(), e.getVendorCode(), e);
        }
    }

    @Override
    public void setInReorg()
    {
    }
    @Override
    public boolean isAlive(GXConnection con)
    {
        try
        {
            return !con.isClosed();
        }catch(SQLException e)
        {
            return false;
        }
    }


    @Override
    public boolean DataTruncation(SQLException e)
    {
        return e.getErrorCode() == ServiceError.DATA_TRUNCATION.getCode();
    }

    private DataSource dataSource;

    @Override
    public void setDataSource(DataSource dataSource)
    {
            this.dataSource = dataSource;
    }

    @Override
    public boolean useReadOnlyConnections()
    {
            return true;
    }

    @Override
    public boolean EndOfFile(SQLException e)
    {
        return e.getErrorCode() == ServiceError.END_OF_FILE.getCode();
    }

    @Override
    public boolean ReferentialIntegrity(SQLException e)
    {
        return e.getErrorCode() == ServiceError.REFERENTIAL_INTEGRITY.getCode();
    }

    @Override
    public boolean DuplicateKeyValue(SQLException e)
    {
        return e.getErrorCode() == ServiceError.DUPLICATE_KEY.getCode();
    }

    @Override
    public boolean ObjectLocked(SQLException e)
    {
        return e.getErrorCode() == ServiceError.OBJECT_LOCKED.getCode();
    }

    @Override
    public boolean ObjectNotFound(SQLException e)
    {
        return e.getErrorCode() == ServiceError.OBJECT_NOT_FOUND.getCode();
    }

    @Override
    public java.util.Date nullDate()
    {
            return CommonUtil.ymdhmsToT_noYL(1, 1, 1, 0, 0, 0);
    }

    @Override
    public boolean useDateTimeInDate()
    {
            return false;
    }

    @Override
    public boolean useCharInDate()
    {
            return false;
    }

    @Override
    public void setConnectionProperties(java.util.Properties props)
    {
        props.setProperty(DATASOURCE_NAME, dataSource.getName());
    }

    @Override
    public void onConnection(GXConnection con) throws SQLException
    {
    }

    @Override
    public java.util.Date serverDateTime(GXConnection con) throws SQLException
    {	
        return CommonUtil.now();
    }

    @Override
    public String serverVersion(GXConnection con) throws SQLException
    {
        return con.getClientInfo("SERVER_VERSION");
    }

    @Override
    public String connectionPhysicalId(GXConnection con)
    {
            return "";
    }

    @Override
    public boolean getSupportsAutocommit()
    {
            return true;
    }

    @Override
    public void commit(Connection con) throws SQLException
    {
            //Las reorgs corren en modo autocommit con lo cual no se debe hacer commit ni rollback implicitos.
            if	(!com.genexus.ApplicationContext.getInstance().getReorganization())
            {
                    con.commit();
            }
    }

    @Override
    public void rollback(Connection con) throws SQLException
    {
            //Las reorgs corren en modo autocommit con lo cual no se debe hacer commit ni rollback implicitos.
            if	(!com.genexus.ApplicationContext.getInstance().getReorganization())
            {		
                    con.rollback();
            }
    }

    @Override
    public boolean ignoreConnectionError(SQLException e)
    {
            return false;
    }

    @Override
    public boolean rePrepareStatement(SQLException e)
    {
        return false;
    }

    @Override
    public boolean getSupportsQueryTimeout()
    {
            return false;
    }

    @Override
    public boolean useStreamsInNullLongVarchar()
    {
            return false;
    }

    @Override
    public boolean useStreamsInLongVarchar()
    {
            return false;
    }

    @Override
    public int getId()
    {
            return DBMS_SERVICE;
    }

    @Override
    public int getLockRetryCount(int lockRetryCount, int waitRecord){
      return lockRetryCount;
    }

    @Override
    public boolean connectionClosed(SQLException e)
    {
        return e.getErrorCode() == ServiceError.CONNECTION_CLOSED.getCode();
    }		        
}
