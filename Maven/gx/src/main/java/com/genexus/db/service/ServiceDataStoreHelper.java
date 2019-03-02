package com.genexus.db.service;

import com.genexus.db.*;
import java.math.BigDecimal;
import java.util.UUID;

public abstract class ServiceDataStoreHelper extends DataStoreHelperBase implements IServiceHelper
{
    public IODataMapName Map(String name)
    {
        return new IODataMapName(name);
    }
    
    public IODataMapExt Ext(String entity, String name)
    {
        return new IODataMapExt(entity, Map(name));
    }
    
    public IODataMapExt Ext(String entity, IODataMap map)
    {
        return new IODataMapExt(entity, map);
    }
    
    public IODataMap MapCol(String entity, String name)
    {
        return Ext(entity, name);
    }
    
    // Parametros
    public Integer GetParmInt(Object parm)
    {
        if(parm == null)
            return null;
        else if(parm instanceof Byte)
            return ((Byte)parm).intValue();
        else if(parm instanceof Short)
            return ((Short)parm).intValue();
        else if(parm instanceof Long)
            return ((Long)parm).intValue();
        else if(parm instanceof BigDecimal)
            return ((BigDecimal)parm).intValue();
        return ((Integer)parm);
    }
    
    public Double GetParmFP(Object parm)
    {
        if(parm == null)
            return null;
        else if(parm instanceof Double)
            return ((Double)parm);
        else if(parm instanceof Float)
            return ((Float)parm).doubleValue();
        else if(parm instanceof BigDecimal)
            return ((BigDecimal)parm).doubleValue();
        else return GetParmInt(parm).doubleValue();
    }   
    
    public String GetParmStr(Object parm)
    {
        return parm == null ? null : parm.toString();
    }
    
    public java.util.Date GetParmDate(Object parm)
    {
        return parm == null ? null : (java.util.Date)parm;
    }

    public java.util.Date GetParmTime(Object parm)
    {
        return parm == null ? null : (java.util.Date)parm;
    }
    
    public Object GetParmGuid(Object parm)
    {
        return parm == null ? null : (parm instanceof UUID ? (UUID) parm : UUID.fromString(parm.toString()));
    }
    
    public Object GetParmObj(Object parm)
    {
        return parm;
    }
    
    public Object GetParmDateTime(Object parm)
    {
        return GetParmDate(parm);
    }

// Parametros en setparameters:
    public Integer GetParmUInt(Object parm)
    {
    	return GetParmInt(parm);
    }
    
    public Double GetParmUFP(Object parm)
    {
    	return GetParmFP(parm);      
    }   
    
    public String GetParmUStr(Object parm)
    {
    	return GetParmStr(parm);
    }
    
    public java.util.Date GetParmUDate(Object parm)
    {
    	return GetParmDate(parm);
    }

    public java.util.Date GetParmUTime(Object parm)
    {
    	return GetParmTime(parm);
    }
    
    public Object GetParmUGuid(Object parm)
    {
    	return GetParmGuid(parm);
    }
    
    public Object GetParmUObj(Object parm)
    {
    	return GetParmObj(parm);
    }    
    
    public Object GetParmUDateTime(Object parm)
    {
        return GetParmUDate(parm);
    }

}
