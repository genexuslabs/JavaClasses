package com.genexus.db.odata;

import com.genexus.db.service.IODataMap;
import com.genexus.db.service.IQuery;
import com.genexus.db.service.QueryType;
import java.net.URI;
import java.util.List;
import java.util.function.*;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.commons.api.edm.EdmEntityType;

public class ODataQuery implements IQuery
{
    final BiFunction<ODataClientHelper, Object [], ODataClientHelper> query;
    IODataMap[] selectList;
    final QueryType queryType;
    ODataQuery continuation;
    ODataClientHelper clientHelper;
    
    public ODataQuery(BiFunction<ODataClientHelper, Object [], ODataClientHelper> query, IODataMap[] selectList)
    {
        this.query = query;
        this.selectList = selectList;
        this.queryType = QueryType.QUERY;
    }        

    public ODataQuery(BiFunction<ODataClientHelper, Object [], ODataClientHelper> query, String queryType)
    {
        this.query = query;
        this.queryType = QueryType.valueOf(queryType);
    }        

    public ODataQuery(BiFunction<ODataClientHelper, Object[], ODataClientHelper> query, ODataQuery continuation)
    {
        this(query, QueryType.QUERY.name(), continuation);
    }

    public ODataQuery(BiFunction<ODataClientHelper, Object[], ODataClientHelper> query, String queryType, ODataQuery continuation)
    {
        this(query, queryType);
        selectList = new IODataMap[0];
        this.continuation = continuation;
    }
    
    public URI build(ODataConnection con, Object[] parms, EdmEntityType[] oEntityType)
    {
        return build(con, parms, null, oEntityType);
    }
    
    public URI build(ODataConnection con, Object[] parms, ClientEntity[] oEntity)
    {
        return build(con, parms, oEntity, null);
    }
    
    public URI build(ODataConnection con, Object[] parms, ClientEntity[] oEntity, EdmEntityType[] oEntityType)
    {
        clientHelper = new ODataClientHelper(con, oEntity, oEntityType);
		return clientHelper.build(this, parms);
    }
    
    public String getLinkEntity()
    {
        return clientHelper.getLinkEntity();
    }
    
    public boolean isLink()
    {
        return clientHelper.isLink();
    }
    
    public boolean hasKeySegment()
    {
        return clientHelper.hasKeySegment();
    }
    
    public boolean isCurrentOfUpdate()
    {
        return clientHelper.isCurrentOfUpdate();
    }
    
    public List<String> getConstrainedProps()
    {
        return clientHelper.getConstrainedProps();
    }    
    
    public boolean setsNullToNonStringType()
    {
        return clientHelper.getSetsNullToNonStringType();
    }

    @Override
    public QueryType getQueryType()
    {
        return queryType;
    }
}
