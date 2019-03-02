
package com.genexus.db.service;

public enum ServiceError
{
    INVALID_QUERY("Invalid Query", 1),
    DUPLICATE_KEY("Duplicate Key", 2),
    REFERENTIAL_INTEGRITY("Referential Integrity", 3),
    DATA_TRUNCATION("Data Truncation", 4),
    END_OF_FILE("End of File", 5), 
    OBJECT_LOCKED("Object Locked", 6), 
    OBJECT_NOT_FOUND("Object not Found", 7),
    CONNECTION_CLOSED("Connection Closed", 8);

    private final String desc;
    private final int code;
    private ServiceError(final String desc, final int code)
    {
        this.desc = desc;
        this.code = code;
    }

    @Override
    public String toString()
    {
        return desc;
    }

    public String getSqlState()
    {
        return desc;
    }

    public int getCode()
    {
        return code;
    }
}

