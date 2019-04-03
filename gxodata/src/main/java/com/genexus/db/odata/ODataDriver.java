package com.genexus.db.odata;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class ODataDriver implements Driver
{
    private static int MAJOR_VERSION = 1;
    private static int MINOR_VERSION = 0;
    private static String DRIVER_ID = "odata:";
    
    private static final ODataDriver ODATA_DRIVER;
    static
    {
        ODATA_DRIVER = new ODataDriver();
        try
        {
            DriverManager.registerDriver(ODATA_DRIVER);
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public ODataDriver()
    {
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException
    {
        if(!acceptsURL(url))
            return null;
        return new ODataConnection(url.substring(DRIVER_ID.length()), info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException
    {
        return url.startsWith(DRIVER_ID);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
    {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant()
    {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
