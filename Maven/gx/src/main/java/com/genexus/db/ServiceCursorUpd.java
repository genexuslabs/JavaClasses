package com.genexus.db;

import com.genexus.db.service.IQuery;


public class ServiceCursorUpd extends ServiceCursorBase
{
    public ServiceCursorUpd(String cursorId, IQuery query, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int cacheableLevel, boolean isForFirst)
    {
        super(cursorId, query, currentOf, errMask, hold, parent, cacheableLevel, isForFirst);
    }

    public ServiceCursorUpd(String cursorId, IQuery query, boolean currentOf, int errMask, boolean hold, IDataStoreHelper parent, int fetchSize, int cacheableLevel, boolean isForFirst)
    {
        super(cursorId, query, currentOf, errMask, hold, parent, fetchSize, cacheableLevel, isForFirst);
    }

    public ServiceCursorUpd(String cursorId, IQuery query, boolean currentOf, int errMask)
    {																						
        super(cursorId, query, currentOf, errMask);
    }

    public ServiceCursorUpd(String cursorId, IQuery query, boolean currentOf)
    {
        super(cursorId, query, currentOf);
    }

    public ServiceCursorUpd(String cursorId, IQuery query, int errMask)
    {																						
        super(cursorId, query, errMask);
    }        
}
