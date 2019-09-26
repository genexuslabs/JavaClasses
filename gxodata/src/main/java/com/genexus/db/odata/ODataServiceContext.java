package com.genexus.db.odata;

import com.genexus.db.service.IOServiceContext;
import java.sql.Connection;
import org.apache.olingo.commons.api.edm.EdmEntityType;

public class ODataServiceContext implements IOServiceContext
{
    final EdmEntityType []currentType;
    final Connection con;
    public ODataServiceContext(Connection con, EdmEntityType baseType)
    {
        this.con = con;
        this.currentType = new EdmEntityType[]{ baseType };
    }

    @Override
    public Connection getConnection()
    {
        return con;
    }    

    @Override
    public Object entity(String entity)
    {
        return ((ODataConnection)con).entity(currentType, entity);
    }
}
