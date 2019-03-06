package com.genexus.db;

import com.genexus.db.driver.*;
import com.genexus.CacheFactory;
import com.genexus.CommonUtil;
import com.genexus.Preferences;
import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.db.service.IServiceHelper;
import com.genexus.db.service.IQuery;

import java.sql.SQLException;
import java.util.Enumeration;

public class ServiceCursorBase extends Cursor
{

    boolean currentOf;
    boolean hold = false;

    Object[] buffers;

    private GXResultSet rslt;
    IDataStoreHelper parent;

    private boolean isCaching = false;
    private int cacheableLevel = Preferences.CHANGE_PRETTY_OFTEN;
    private boolean isForFirst = false;
    private boolean isCachingEnabled;
    private int fetchSize;
    private final Object syncResultSet = new Object();
    public IQuery query;

    public ServiceCursorBase(String cursorId, IQuery query, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int cacheableLevel, boolean isForFirst)
    {
        this(cursorId, query, currentOf, errMask, hold, parent, 0, cacheableLevel, isForFirst);
    }

    public ServiceCursorBase(String cursorId, IQuery query, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int fetchSize, int cacheableLevel, boolean isForFirst)
    {
        this(cursorId, query, currentOf, errMask);
        if(com.genexus.ClientContext.getModelContext() != null)
        {
            isCachingEnabled = Preferences.getDefaultPreferences().getCACHING() || CacheFactory.getForceHighestTimetoLive();
        }else
        {
            isCachingEnabled = false;
        }
        this.hold = hold;
        this.parent = parent;
//		dynStatement = sqlSentence.equalsIgnoreCase("scmdbuf");
        this.cacheableLevel = getCacheableLevel(cacheableLevel);
        this.fetchSize = fetchSize;
        this.isForFirst = isForFirst;
        this.query = query;
    }

    public ServiceCursorBase(String cursorId, IQuery query, boolean currentOf, int errMask)
    {
        super(cursorId, queryId(cursorId, query), errMask);
        this.currentOf = currentOf;
        this.query = query;
    }

    public ServiceCursorBase(String cursorId, IQuery query, boolean currentOf)
    {
        this(cursorId, query, currentOf, 0);
    }
    
    public IDataStoreHelper getParent()
    {
        return parent;
    }

    //Constructor requerido, constructor equivalente al de UpdateCursor, sentencias insert autonumber generadas como for each cursor.
    public ServiceCursorBase(String cursorId, IQuery query, int errMask)
    {
        this(cursorId, query, false, errMask);
    }

    private static String queryId(String name, Object query)
    {
        return String.format("Service:%s_%d", name, query.hashCode());
    }

    public void setOutputBuffers(Object[] buffers)
    {
        this.buffers = buffers;
    }

    protected boolean isCacheable()
    {
        return isCachingEnabled && (!currentOf && (cacheableLevel != Preferences.CHANGE_PRETTY_OFTEN));
    }

    @Override
    protected boolean isForFirst()
    {
        return isForFirst;
    }

    protected int getCacheableLevel()
    {
        return cacheableLevel;
    }

    @Override
    byte[] preExecute(int cursorNum, AbstractDataStoreProviderBase abstractProvider, AbstractDataSource abstractDS, Object[] params) throws SQLException
    {
    	DataStoreProviderBase connectionProvider = (DataStoreProviderBase) abstractProvider;
    	DataSource ds = (DataSource) abstractDS;

        close();
        Object[] dynStmt;
        byte[] hasValues = null;
        String sentence = mSQLSentence;
        if(dynStatement)
        { // Si la sentencia tiene constraints din�micos
            dynStmt = parent.getDynamicStatement(cursorNum, connectionProvider.context, connectionProvider.remoteHandle, connectionProvider.context.getHttpContext(), connectionProvider.getDynConstraints());
            sentence = (String) dynStmt[0];
            hasValues = (byte[]) dynStmt[1];
        }

        isCaching = false;
        if(isCacheable())
        { // Si el ResultSet es cacheable, tengo que ver si ya la tengo cacheada
            if(connectionProvider.cacheValue == null)
            {
                connectionProvider.cacheValue = new CacheValue[connectionProvider.cursors.length];
                connectionProvider.cacheIterator = new java.util.Enumeration[connectionProvider.cursors.length];
            }
            CacheKey ckey = new CacheKey(sentence, params);
            CacheValue cacheValue = CacheFactory.getInstance().<CacheValue>get(CacheFactory.CACHE_DB, ckey.toString(), CacheValue.class);
            Enumeration cacheIterator = null;

            if(cacheValue != null && ckey.equals(cacheValue.getKey())) //Chequeo extra por probabilidad de colision en algoritmo de encriptado SHA1
            {
                cacheIterator = cacheValue.getIterator();
            }

            connectionProvider.cacheIterator[cursorNum] = cacheIterator;
            if(connectionProvider.cacheIterator[cursorNum] != null)
            {
                if(com.genexus.DebugFlag.DEBUG)
                {
                    ds.getLog().logComment(this, "Using cached data for " + sentence);
                }
                isCaching = true;
                return hasValues;
            }else
            {
                connectionProvider.cacheValue[cursorNum] = CacheFactory.createCacheValue(sentence, params, cacheableLevel);
            }
        }

        GXConnection con = (GXConnection) SentenceProvider.acquireConnection(connectionProvider);
        mPreparedStatement = ((IServiceHelper) connectionProvider.getHelper()).getPreparedStatement(con, query, this, cursorNum, currentOf, params);

//		mPreparedStatement = SentenceProvider.getPreparedStatement(connectionProvider, mCursorId, sentence, currentOf);
        if(fetchSize != 0)
        {
            mPreparedStatement.setFetchSize(fetchSize);
        }
        return hasValues;
    }

    @Override
    void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
    {
        switch (query.getQueryType())
        {
            case QUERY:
            {
                if(isCaching)
                { // Si estoy utilizando datos cacheados, retorno
                    return;
                }
                GXResultSet newSet = (GXResultSet) mPreparedStatement.executeQuery(hold);
                synchronized (syncResultSet)
                {
                    rslt = newSet;
                }
            }
            break;
            case INS:
            case UPD:
            case DLT:
            case LINK:
            {
                status = mPreparedStatement.executeUpdate();
            }
            break;
            default:
                throw new RuntimeException("Not implemented");
        }

        //if(currentOf)
        //{ // Si tengo currentof marco uncommited changes para hacer un rollback dado que en algunos casos
        // de error (Bantotal) les estaban quedando locks en la bd 
        //	connectionProvider.getConnection().setUncommitedChanges();
        //}
    }

    public boolean next(AbstractDataSource ds) throws SQLException
    {
        return isCaching || rslt.next();
    }

    @Override
    protected void close() throws SQLException
    {
        synchronized (syncResultSet)
        {
            if(rslt != null)
            {
                rslt.close();
                rslt = null;
            }
        }
    }

    /**
     * Esto es necesario porque en algunos casos se genera algo del estilo a:
     * cursor1.execute while !eof cursor2.execute ..
     *
     * El cursor2.execute podria o no encontrar datos. Si no encuentra, estaba
     * dejando los valores anteriores del fetch..
     */
    public void clearBuffers()
    {
        for(int i = buffers.length - 1; i >= 0; i--)
        {
            if(buffers[i] instanceof byte[])
            {
                ((byte[]) buffers[i])[0] = 0;
            }else if(buffers[i] instanceof short[])
            {
                ((short[]) buffers[i])[0] = 0;
            }else if(buffers[i] instanceof int[])
            {
                ((int[]) buffers[i])[0] = 0;
            }else if(buffers[i] instanceof long[])
            {
                ((long[]) buffers[i])[0] = 0;
            }else if(buffers[i] instanceof float[])
            {
                ((float[]) buffers[i])[0] = 0;
            }else if(buffers[i] instanceof double[])
            {
                ((double[]) buffers[i])[0] = 0;
            }else if(buffers[i] instanceof String[])
            {
                ((String[]) buffers[i])[0] = "";
            }else if(buffers[i] instanceof java.util.Date[])
            {
                ((java.util.Date[]) buffers[i])[0] = CommonUtil.nullDate();
            }
        }
    }

    public boolean hasResult()
    {
        return rslt != null;
    }

    @Override
    public boolean isCurrentOf()
    {
        return currentOf;
    }

    public GXResultSet getResultSet()
    {
        synchronized (rslt)
        {
            return rslt;
        }
    }
}
