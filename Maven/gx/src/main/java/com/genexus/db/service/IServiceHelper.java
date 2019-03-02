package com.genexus.db.service;

import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.*;

public interface IServiceHelper
{
    public GXPreparedStatement getPreparedStatement(GXConnection con, IQuery query, ServiceCursorBase cursor, int cursorNum, boolean currentOf, Object[] parms);
}
