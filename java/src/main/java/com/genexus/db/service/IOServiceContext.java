package com.genexus.db.service;

import java.sql.Connection;

public interface IOServiceContext
{
    public Connection getConnection();
    public Object entity(String entity);
}
