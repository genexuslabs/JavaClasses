package com.genexus.db;

import com.genexus.db.service.IQuery;


public class ServiceCursor extends ServiceCursorBase implements IForEachCursor
{
    public ServiceCursor(String cursorId, IQuery query, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int cacheableLevel, boolean isForFirst)
    {
        super(cursorId, query, currentOf, errMask, hold, parent, cacheableLevel, isForFirst);
    }

    public ServiceCursor(String cursorId, IQuery query, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int fetchSize, int cacheableLevel, boolean isForFirst)
    {
        super(cursorId, query, currentOf, errMask, hold, parent, fetchSize, cacheableLevel, isForFirst);
    }

    public ServiceCursor(String cursorId, IQuery query, boolean currentOf, int errMask)
    {																						
        super(cursorId, query, currentOf, errMask);
    }

    public ServiceCursor(String cursorId, IQuery query, boolean currentOf)
    {
        super(cursorId, query, currentOf);
    }

    public ServiceCursor(String cursorId, IQuery query, int errMask)
    {																						
        super(cursorId, query, errMask);
    }

    @Override
    public void setStatus(int status)
    {
        super.status = status;
    }

    @Override
    public Object[] getBuffers()
    {
        return buffers;
    }

    @Override
    public int getStatus()
    {
        return status;
    }    
}
