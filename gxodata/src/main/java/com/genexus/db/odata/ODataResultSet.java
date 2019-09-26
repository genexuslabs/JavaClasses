package com.genexus.db.odata;

import com.genexus.DebugFlag;
import com.genexus.db.driver.GXDBDebug;
import com.genexus.db.service.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.*;
import org.apache.olingo.client.api.*;
import org.apache.olingo.client.api.communication.ODataServerErrorException;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;

class ODataResultSet extends ServiceResultSet<ClientValue>
{
    private ODataPreparedStatement stmt;
    private boolean skipBaseProperties = true;
    private EdmEntityType baseEntityType = null;
    private boolean trackCurrentOf;
    private HashSet<String> allSelectedKeys = null;

    public ODataResultSet(ODataPreparedStatement stmt) throws SQLException
    {
        this(stmt, false);
    }
    
    public ODataResultSet(ODataPreparedStatement stmt, boolean trackCurrentOf) throws SQLException
    {
        execute(stmt);
        this.trackCurrentOf = trackCurrentOf;
        if(trackCurrentOf)
            ((DataStoreHelperOData)stmt.getCursor().getParent()).getCurrentOfManager().addQuery(stmt.getCursor().getCursorId(), this);
    }
    
    protected ClientEntity getCurrentEntity()
    {
        return ((ODataRecordIterator)iterator).currentEntity;
    }
    
    private void execute(IServicePreparedStatement iStmt) throws SQLException
    {
        this.stmt = (ODataPreparedStatement)iStmt;
        if(stmt.query.selectList != null)
        {
            for(IODataMap selectItem : stmt.query.selectList)
            {
                if(selectItem instanceof IODataMapName)
                {
                    skipBaseProperties = false;
                    break;
                }
            }
        }
        execute(stmt.query, stmt.parms);
    }
    
    
    ODataClient getClient() throws SQLException
    {
        return ((ODataConnection)stmt.getConnection()).client;
    }
    
    List<String> constrainedProps;
    private void execute(ODataQuery query, Object[] parms) throws SQLException
    {
        EdmEntityType[] oEntityType = baseEntityType == null ? new EdmEntityType[1] : null;
        nextURI = query.build((ODataConnection)stmt.getConnection(), parms, oEntityType);
        constrainedProps = query.getConstrainedProps();
        if(baseEntityType == null && oEntityType != null)
            baseEntityType = oEntityType[0];
    }
    
    URI nextURI;
    
    @Override
    public <T> T getAs(Class<T> reference, int columnIndex, T defaultValue) throws SQLException
    {
        try
        {
            value = (ClientValue)stmt.query.selectList[columnIndex-1].getValue(newServiceContext(), currentEntry);
            if(value == null)
                return defaultValue;
            T retValue = value.asPrimitive().toCastValue(reference);
            return retValue != null ? retValue : defaultValue;
        }catch(ClassCastException mayBeGeoSpatial)
        { // el generador manda un getVarchar para los geopoint
            try
            {
                if(value.asPrimitive().getTypeKind().isGeospatial())
                {
                    String wkt = value.toString();
                    wkt = wkt.substring(wkt.indexOf(';')+1).toUpperCase();
                    wkt = wkt.substring(0, wkt.lastIndexOf('\''));
                    return reference.cast(wkt);
                }else throw mayBeGeoSpatial;
            }catch(Exception e)
            {
                throw mayBeGeoSpatial;
            }            
        } catch (EdmPrimitiveTypeException ex)
        {
            try
            {
                if(ex.getCause() instanceof IllegalArgumentException &&
                   ex.getCause().getCause() instanceof ArithmeticException)
                { //@Hack: permito que si el modelo dice que retorna numerico sin decimales pero del server retornan con decimales se trunque
                    BigDecimal retVal = value.asPrimitive().toCastValue(BigDecimal.class).setScale(0, RoundingMode.FLOOR);
                    if(reference.isAssignableFrom(Long.class))
                        return reference.cast(retVal.longValueExact());
                    else if(reference.isAssignableFrom(Integer.class))
                        return reference.cast(retVal.intValueExact());
                    else if(reference.isAssignableFrom(Short.class))
                        return reference.cast(retVal.shortValueExact());
                    else if(reference.isAssignableFrom(Byte.class))
                        return reference.cast(retVal.byteValueExact());
                }                
            }catch(Exception ignored){}
            if(value.asPrimitive().getTypeKind() == EdmPrimitiveTypeKind.Guid)  // Caso particular de los Guid que en GX vienen como getString
                return reference.cast(value.toString()); 
            Logger.getLogger(ODataResultSet.class.getName()).log(Level.SEVERE, null, ex);
            return defaultValue;
        }
    }
    
    private ODataServiceContext newServiceContext() throws SQLException
    {
        return new ODataServiceContext(stmt.getConnection(), baseEntityType);
    }
    
    @Override
    public void close() throws SQLException
    {
        nextURI = null;
        if(trackCurrentOf)
            ((DataStoreHelperOData)stmt.getCursor().getParent()).getCurrentOfManager().removeQuery(stmt.getCursor().getCursorId());                
        super.close();
    }
    
    @Override
    public boolean wasNull()
	{
        return value == null || 
                (value.isPrimitive() && value.asPrimitive().toValue() == null);
    }    
    
    @Override
    public boolean isClosed() throws SQLException
    {
        return super.isClosed() && nextURI == null;
    }
            
    private ODataRecordIterator flattenRecords(ClientEntitySetIterator<ClientEntitySet, ClientEntity> odataIterator)
    {
        ArrayList<ClientEntity> entities = new ArrayList<>();
        ArrayList<Iterator<HashMap<String, Object>>> iterators = new ArrayList<>();
        while(odataIterator.hasNext())
        {
            ClientEntity entity = odataIterator.next();
			List<HashMap<String, Object>> records = new ArrayList<>(flattenRecord(flattenRecordSkip(entity, skipBaseProperties), true));
            entities.add(entity);
            iterators.add(records.iterator());
        }
        return new ODataRecordIterator(entities, iterators);
    }
    
    private HashMap<String, Object> flattenRecord(ClientEntity entity)
    {
        return flattenRecordSkip(entity, false);
    }

    private HashMap<String, Object> flattenRecordSkip(ClientEntity entity, boolean skipProperties)
    {
        for(String constrainedProp:constrainedProps)
        {
            ClientLink clientLink = entity.getNavigationLink(constrainedProp);
            // Si la query tiene una constraint sobre una entidad expandida y el resultado no trae la entidad, salteo el registro
            if(clientLink != null && 
               (clientLink.asInlineEntity() == null && 
                (clientLink.asInlineEntitySet() == null || clientLink.asInlineEntitySet().getEntitySet().getEntities().isEmpty())
               ))
            {
                return null;
            }
        }
        HashMap<String, Object> record = new HashMap<>();
        if(!skipProperties)
        {
            for(ClientProperty prop:entity.getProperties())
                record.put(prop.getName(), prop.getValue());
        }
        
        for(ClientLink link:entity.getNavigationLinks())            
        {
            ClientInlineEntity inlineEntity = link.asInlineEntity();            
            record.put(link.getName(), inlineEntity != null ? inlineEntity : link.asInlineEntitySet());
        }
        return record;
    }

    private Collection<? extends HashMap<String, Object>> flattenRecord(HashMap<String, Object> record)
    {
        return flattenRecord(record, false);
    }
    
    private Collection<? extends HashMap<String, Object>> flattenRecord(HashMap<String, Object> record, boolean isBase)
    {
        List<HashMap<String, Object>> records = new ArrayList<>();
        String toFlattenKey = null;
        Object toFlattenValue = null;
        boolean anyValue = false;
        if(record == null)
            return records;
        for(String key:record.keySet())
        {
            Object curValue = record.get(key);
            anyValue |= curValue != null;
            if(curValue instanceof ClientCollectionValue ||
               curValue instanceof ClientInlineEntity ||
               curValue instanceof ClientComplexValue ||
               curValue instanceof ClientInlineEntitySet)
            {
                toFlattenKey = key;
                toFlattenValue = curValue;
                break;
            }
        }
        if(toFlattenValue == null)
        {
            if(anyValue)
                records.add(record);
        }else
        {
            record.remove(toFlattenKey);
            if(toFlattenValue instanceof ClientCollectionValue)
            {
                @SuppressWarnings("unchecked")
                ClientCollectionValue<ClientValue> values = (ClientCollectionValue<ClientValue>)toFlattenValue;
                if(values.isEmpty())
                {
                    if(isBase && selectsEntity(toFlattenKey))
                        return records;
                    else
                    {
                        record.put(toFlattenKey, null);
                        return flattenRecord(record, false);
                    }
                }else
                {
                    List<HashMap<String, Object>> newRecords = new ArrayList<>();
                    for(ClientValue curValue:values)
                    {
                        HashMap<String, Object> newRecord = new HashMap<>(record);
                        newRecord.put(toFlattenKey, curValue);
                        newRecords.addAll(flattenRecord(newRecord));
                    }
                    for(HashMap<String, Object> newRecord:newRecords)
                        records.addAll(flattenRecord(newRecord));
                }
            }else if(toFlattenValue instanceof ClientInlineEntitySet)
            {
                ClientEntitySet entitySet = ((ClientInlineEntitySet)toFlattenValue).getEntitySet();
                if(entitySet.getEntities().isEmpty())
                {
                    if(isBase && selectsEntity(toFlattenKey))
                        return records;
                    else
                    {
                        record.put(toFlattenKey, null);
                        return flattenRecord(record);
                    }
                }else
                {
                    List<HashMap<String, Object>> newRecords = new ArrayList<>();
                    for(ClientEntity entity:entitySet.getEntities())
                    {
                        HashMap<String, Object> inlineRecord = flattenRecord(entity);
                        for(HashMap<String, Object> flattenedValue:flattenRecord(inlineRecord))
                        {
                            HashMap<String, Object> newRecord = new HashMap<>(record);
                            newRecord.put(toFlattenKey, flattenedValue);
                            newRecords.addAll(flattenRecord(newRecord));
                        }
                    }
                    for(HashMap<String, Object> newRecord:newRecords)
                        records.addAll(flattenRecord(newRecord));
                }
            }else if(toFlattenValue instanceof ClientInlineEntity)
            {
                ClientInlineEntity inlineEntity = (ClientInlineEntity)toFlattenValue;
                HashMap<String, Object> inlineRecord = flattenRecord(inlineEntity.getEntity());
                List<HashMap<String, Object>> newRecords = new ArrayList<>();
                for(HashMap<String, Object> flattenedValue:flattenRecord(inlineRecord))
                {
                    HashMap<String, Object> newRecord = new HashMap<>(record);
                    newRecord.put(toFlattenKey, flattenedValue);
                    newRecords.addAll(flattenRecord(newRecord));
                }
                for(HashMap<String, Object> newRecord:newRecords)
                    records.addAll(flattenRecord(newRecord));
            }else
            {
                ClientComplexValue complexValue = (ClientComplexValue)toFlattenValue;
                List<HashMap<String, Object>> newRecords = new ArrayList<>();
                HashMap<String, Object> complexRecord = new HashMap<>();
                for(String key:complexValue.asJavaMap().keySet())
                    complexRecord.put(key, complexValue.get(key).getValue());
                for(HashMap<String, Object> flattenedValue:flattenRecord(complexRecord))
                {
                    HashMap<String, Object> newRecord = new HashMap<>(record);
                    newRecord.put(toFlattenKey, flattenedValue);
                    newRecords.addAll(flattenRecord(newRecord));
                }
                for(HashMap<String, Object> newRecord:newRecords)
                    records.addAll(flattenRecord(newRecord));
            }
        }
        return records;
    }
    
    private String getEntityName(String key)
    {
        try
        {
            return (String)newServiceContext().entity(key);
        }catch(SQLException e)
        {
            return key;
        }
    }
    
    private boolean selectsEntity(String key)
    {
        if(allSelectedKeys == null && stmt.query.selectList != null)
        {
            allSelectedKeys = new HashSet<>();
            for(int idx = 0; idx < stmt.query.selectList.length; idx++ )
            {
                IODataMap dataMap = stmt.query.selectList[idx];
                if(dataMap instanceof IODataMapName)
                    allSelectedKeys.add(dataMap.getName());
                else allSelectedKeys.add(getEntityName(dataMap.getName()));
            }
        }
        // Si se selecciona un campo que es collection pero en la entidad esta vacio, se saltea el registro
        // (lo hacemos para las propiedades de la entidad base
        return(allSelectedKeys != null && allSelectedKeys.contains(key));
    }
    
    ClientEntitySetIterator<ClientEntitySet, ClientEntity> odataEntityIterator = null; 
    public ClientEntity nextEntity() throws SQLException
    {
        while(odataEntityIterator == null || !odataEntityIterator.hasNext())
        {
            if(odataEntityIterator != null)
                nextURI = odataEntityIterator.getNext();
            if(nextURI == null)
                return null;
            ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = fetchNext();
            odataEntityIterator = response.getBody(); 
        }
        return odataEntityIterator.next();
    }

    ClientEntity firstEntity() throws SQLException
    {
        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = fetchNext();
        ClientEntitySetIterator<ClientEntitySet, ClientEntity> odataIterator = response.getBody(); 
        if(odataIterator.hasNext())
            return odataIterator.next();
        else return null;
    }
    
    private ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> fetchNext() throws SQLException
    {
        ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response;
        try
        {
            if(DebugFlag.DEBUG)
                    stmt.log.logComment(GXDBDebug.LOG_MIN, this, stmt.handle, "query=" + nextURI);
            response = getClient().getRetrieveRequestFactory().getEntitySetIteratorRequest(nextURI).execute();
        }catch(ODataServerErrorException ex)
        {
            String resStr = "";
            try
            {
                URLConnection conn = nextURI.toURL().openConnection();
                if(conn instanceof HttpURLConnection)
                {
                    conn.setRequestProperty("Accept", "*/*");
                    InputStream rawResponse;
                    try
                    {
                        rawResponse = conn.getInputStream();
                    }catch(IOException err){ rawResponse = ((HttpURLConnection) conn).getErrorStream(); }
                    resStr = new BufferedReader(new InputStreamReader(rawResponse)).lines().collect(java.util.stream.Collectors.joining("\n"));
                    rawResponse.close();
                }
            }catch(Exception ignored)
            {
            }
            throw new SQLException(String.format("%s%n%s", ex.getMessage(), resStr), ServiceError.INVALID_QUERY.getSqlState(), ServiceError.INVALID_QUERY.getCode(), ex);

        }catch(ODataRuntimeException ex)
        {
            throw new SQLException(ex.getMessage(), ServiceError.INVALID_QUERY.getSqlState(), ServiceError.INVALID_QUERY.getCode(), ex);
        }
        return response;
    }

    @Override
    public boolean next() throws SQLException
    {
        while((iterator == null || !iterator.hasNext()) && nextURI != null)
        {
            ODataRetrieveResponse<ClientEntitySetIterator<ClientEntitySet, ClientEntity>> response = fetchNext();
            ClientEntitySetIterator<ClientEntitySet, ClientEntity> odataIterator = response.getBody(); 
            iterator = flattenRecords(odataIterator);
            nextURI = odataIterator.getNext();
        }
        if(iterator != null && iterator.hasNext())
        {
            currentEntry = iterator.next();
            return true;
        }else
        {
            return false;
        }
    }
    
    class ODataRecordIterator implements Iterator<HashMap<String, Object>>
    {
        private final Iterator<ClientEntity> entities;
        private final Iterator<Iterator<HashMap<String, Object>>> iterators;
        
        private ClientEntity currentEntity = null;
        private Iterator<HashMap<String, Object>> currentIterator = null;
        
        public ODataRecordIterator(ArrayList<ClientEntity> entities, ArrayList<Iterator<HashMap<String, Object>>> iterators)
        {
            this.entities = entities.iterator();
            this.iterators = iterators.iterator();
        }

        @Override
        public boolean hasNext()
        {
            while(currentIterator == null || !currentIterator.hasNext())
            {
                if(!iterators.hasNext())
                    return false;
                currentIterator = iterators.next();
                currentEntity = entities.next();
            }
            return true;
        }

        @Override
        public HashMap<String, Object> next()
        {
            return currentIterator.next();
        }
    }
    
// JDK8
    @Override
    public <T> T getObject(int columnIndex, Class<T> type)
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type)
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    } 
}
