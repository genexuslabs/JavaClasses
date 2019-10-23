package com.genexus.db.odata;

import com.genexus.DebugFlag;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.driver.GXDBDebug;
import com.genexus.db.service.ServiceConnection;
import com.genexus.db.service.ServiceError;
import com.genexus.db.service.ServicePreparedStatement;
import com.genexus.diagnostics.core.LogManager;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.communication.request.cud.*;
import org.apache.olingo.client.api.communication.response.ODataDeleteResponse;
import org.apache.olingo.client.api.communication.response.ODataEntityCreateResponse;
import org.apache.olingo.client.api.communication.response.ODataEntityUpdateResponse;
import org.apache.olingo.client.api.communication.response.ODataReferenceAddingResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

class ODataPreparedStatement extends ServicePreparedStatement
{
    final ODataQuery query;
    final ServiceCursorBase cursor;

    ODataPreparedStatement(Connection con, ODataQuery query, ServiceCursorBase cursor, Object[] parms, GXConnection gxCon)
    {
        super(con, parms, gxCon);
        this.query = query;
        this.cursor = cursor;
    }
    
    protected ServiceCursorBase getCursor()
    {
        return cursor;
    }

    @Override
    public ResultSet executeQuery() throws SQLException
    {
        return executeThisQuery(cursor.getParent() != null);
    }
    
    public ODataResultSet executeThisQuery(boolean trackCurrentOf) throws SQLException
    {
        ODataResultSet resultSet = new ODataResultSet(this, trackCurrentOf);
        if(query.continuation != null)
        {
            ClientEntity entity = resultSet.firstEntity();
            if(entity == null)
            {
                throw new SQLException(ServiceError.OBJECT_NOT_FOUND.toString(), ServiceError.OBJECT_NOT_FOUND.getSqlState(), ServiceError.OBJECT_NOT_FOUND.getCode());
            }
            executeContinuation(entity, query.continuation);
        }
        return resultSet;
    }
    
    ODataClient getClient() throws SQLException
    {
        return ((ODataConnection)getConnection()).client;
    }
    
    private int executeContinuation(ClientEntity entity, ODataQuery continuation) throws SQLException
    {
        int retCode = executeUpdate(continuation, entity, query.clientHelper.baseEntitySet);
        if(continuation.continuation != null)
            return executeContinuation(entity, continuation.continuation);
        return retCode;
    }
    
    private int executeUpdate(ODataQuery updQuery, ClientEntity filledEntityOrNull, String baseEntitySetOrNull) throws SQLException
    {
        ClientEntity []oEntity = new ClientEntity[]{ filledEntityOrNull };
        URI updURI = updQuery.build((ODataConnection)getConnection(), parms, oEntity);
        boolean setsNullToNonStringType = updQuery.setsNullToNonStringType();
        if(DebugFlag.DEBUG)
            log.logComment(GXDBDebug.LOG_MIN, this, handle, String.format("update(%s)=%s", updQuery.queryType, updURI));
        ClientEntity entity = oEntity[0];
        int resCode = 0;
        try
        {
            switch(updQuery.queryType)
            {
                case INS: 
                {
                    ODataEntityCreateRequest<ClientEntity> request = getClient().getCUDRequestFactory().getEntityCreateRequest(updURI, entity);
                    ODataEntityCreateResponse<ClientEntity> res = request.execute();
                    resCode = res.getStatusCode();
                    if(updQuery.continuation != null)
                        executeThisQuery(false);
                    return resCode;
                }
                case UPD:
                {
                    if(updQuery.isCurrentOfUpdate() || canOptimizeUpd(updQuery, updURI))
                    {
                        resCode = updateOneEntity(updURI, entity, setsNullToNonStringType);
                        if(updQuery.continuation != null)
                            executeThisQuery(false);
                        return resCode;
                    }else
                    {
                        resCode = 404;
                        ODataResultSet resultSet = new ODataResultSet(this);
                        for(ClientEntity currentEntity = resultSet.nextEntity(); currentEntity != null; currentEntity = resultSet.nextEntity())
                        {
                            resCode = updateOneEntity(getEditLink(currentEntity, updURI), entity, setsNullToNonStringType);
                            if(resCode >= 400)
                                return resCode;
                            if(updQuery.continuation != null)
                                resCode = executeContinuation(currentEntity, query.continuation);
                        }
                        return resCode;
                    }
                }
                case DLT:
                {
                    if(updQuery.hasKeySegment())
                        return executeDelete(updURI);
                    else
                    {
                        resCode = 404;
                        ODataResultSet resultSet = new ODataResultSet(this);
                        for(ClientEntity currentEntity = resultSet.nextEntity(); currentEntity != null; currentEntity = resultSet.nextEntity())
                        {
                            resCode = executeDelete(getEditLink(currentEntity, updURI));
                            if(resCode >= 400)
                                return resCode;
                        }
                        return resCode;
                    }
                }
                case LINK:
                {                    
                    // Puede ser Link o Unlink                    
                    try
                    {
                        URI fromUri = new ODataClientHelper((ODataConnection)getConnection(), oEntity, new EdmEntityType[1])
                                    .get(baseEntitySetOrNull)
                                    .keyRef(updQuery.getLinkEntity())
                                    .build(null, parms);
                        if(updQuery.isLink())
                        {
                            ODataReferenceAddingRequest request = getClient().getCUDRequestFactory().getReferenceSingleChangeRequest(new URI(((ServiceConnection)con).url), fromUri, updURI);
                            ODataReferenceAddingResponse res = request.execute();
                            return res.getStatusCode();
                        }else return executeDelete(fromUri);                        
                    } catch (URISyntaxException ex)
                    {
                        throw new SQLException(ex.getMessage(), ServiceError.INVALID_QUERY.getSqlState(), ServiceError.INVALID_QUERY.getCode(), ex);
                    }
                }                                    
            }
        }catch(ODataClientErrorException ex)
        {
            switch(ex.getStatusLine().getStatusCode())
            {
                case 404: resCode = 404; break;
                default:  throw new SQLException(ex.getMessage(), ServiceError.INVALID_QUERY.getSqlState(), ServiceError.INVALID_QUERY.getCode(), ex);
            }
        }
        catch(ODataRuntimeException ex)
        {            
            throw new SQLException(ex.getMessage(), ServiceError.INVALID_QUERY.getSqlState(), ServiceError.INVALID_QUERY.getCode(), ex);
        }
        
        return resCode;
    }
    
    private URI getEditLink(ClientEntity entity, URI updURI)
    { // @hack: si la editLink retorna un schema distinto lo compatibilizo con el de la updURI porque sino luego Olingo da error en la checkRequest de AbstractRequest.java 
        URI editLink = entity.getEditLink();
        if(!editLink.getScheme().equals(updURI.getScheme()))
        {
            try
            {
                editLink = new URI(updURI.getScheme(), editLink.getUserInfo(), editLink.getHost(), editLink.getPort(), editLink.getPath(), editLink.getQuery(), editLink.getFragment());
            }catch(URISyntaxException ex)
			{
				LogManager.getLogger(ODataPreparedStatement.class).warn(String.format("Could not update editLink: %s", updURI ), ex);
			}
        }
        return editLink;
    }
    
    private boolean canOptimizeUpd(ODataQuery updQuery, URI updURI) throws SQLException
    {
        return updQuery.hasKeySegment() && ((ODataConnection)getConnection()).needsCheckOptimisticConcurrency(updURI); // No se puede optimizar si no tiene optimistic concurrency (que agrega el header If-Match porque el PATCH del lado del server puede ingresar un registro cuando el mismo no existe
    }
    
    private int updateOneEntity(URI updURI, ClientEntity entity, boolean setsNullToNonStringType) throws SQLException
    {
        ContentType oldContentType = getClient().getConfiguration().getDefaultFormat();
        try
        {
            if(setsNullToNonStringType)
                getClient().getConfiguration().setDefaultPubFormat(ContentType.JSON_NO_METADATA);
            ODataEntityUpdateRequest<ClientEntity> request = getClient().getCUDRequestFactory().getEntityUpdateRequest(updURI, UpdateType.PATCH, entity);

            if(((ODataConnection)getConnection()).needsCheckOptimisticConcurrency(updURI))
                request.setIfMatch("*");
            ODataEntityUpdateResponse<ClientEntity> res = request.execute();
            return res.getStatusCode();        
        }finally
        {
            if(setsNullToNonStringType)
                getClient().getConfiguration().setDefaultPubFormat(oldContentType);            
        }
    }
    
    private int executeDelete(URI updURI) throws ODataRuntimeException, SQLException
    {
        ODataDeleteRequest request = getClient().getCUDRequestFactory().getDeleteRequest(updURI);
        if(((ODataConnection)getConnection()).needsCheckOptimisticConcurrency(updURI))
            request.setIfMatch("*");
        ODataDeleteResponse res = request.execute();
        return res.getStatusCode();
    }

    @Override
    public int executeUpdate() throws SQLException
    {
        return mapResCodeToCursorStatus(executeUpdate(query, null, null));
    }    
    
    private int mapResCodeToCursorStatus(int resCode)
    {
        switch(resCode)
        {
            case 404: return com.genexus.db.Cursor.EOF;
            default: return 0;
        }
    }
    
/// JDK8
    @Override
    public void closeOnCompletion()
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isCloseOnCompletion()
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }    
}
