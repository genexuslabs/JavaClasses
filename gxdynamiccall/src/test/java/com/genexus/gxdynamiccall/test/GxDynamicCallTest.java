package com.genexus.gxdynamiccall.test;
import com.genexus.GXBaseCollection;
import com.genexus.GXSimpleCollection;
import com.genexus.gxdynamiccall.GXDynCallMethodConf;
import com.genexus.gxdynamiccall.GXDynCallProperties;
import com.genexus.gxdynamiccall.GXDynamicCall;
import com.genexus.specific.java.Connect;

import org.junit.Assert;
import org.junit.Test;

public class GxDynamicCallTest  {

    @Test
    public void callGxNativeObject(){
        Connect.init();
        GXDynamicCall call = new GXDynamicCall();
        call.setObjectName("com.genexus.gxdynamiccall.test.DynamicCallTestProcedure");
        GXSimpleCollection<Object> paramArray = new GXSimpleCollection<Object>();
        paramArray.add((short)3);
        paramArray.add((short)4);
        paramArray.add(new String());
        Object[] parametersArray = {paramArray};
        Object[] errorsArray= new Object[1];
        call.execute(parametersArray, errorsArray);
        Assert.assertTrue(((GXBaseCollection)errorsArray[0]).size()==0);
        GXSimpleCollection<Object> parms= (GXSimpleCollection<Object>)parametersArray[0];
        String parm = ((String)parms.get(2)).trim();
        Assert.assertTrue(parm.equals("7"));
    }

    @Test
    public void callExternalClass(){
        Connect.init();
        GXDynamicCall call = new GXDynamicCall();
        GXDynCallProperties props = new GXDynCallProperties();
        Object[] errorsArray= new Object[1];
        props.setExternalName("DynamicCallExternalTestProcedure");
        props.setPackageName("com.genexus.gxdynamiccall.test");
        call.setProperties(props);
        //Constructor 
        GXSimpleCollection<Object> constructParamArray = new GXSimpleCollection<Object>();
        constructParamArray.add((int)3); 
        call.create(constructParamArray, errorsArray);
        //Parameters
        GXSimpleCollection<Object> paramArray = new GXSimpleCollection<Object>();
        paramArray.add((short)3);
        paramArray.add((short)4);
        Object[] parametersArray = {paramArray};
        //MethodConfiguration
        GXDynCallMethodConf method = new GXDynCallMethodConf();
        method.setIsStatic(false);
        method.setMethodName("calculateAsString");
        String result = (String)call.execute(parametersArray, method, errorsArray);
        Assert.assertTrue(((GXBaseCollection)errorsArray[0]).size()==0);
        Assert.assertTrue(result.trim().equals("21"));
        paramArray.clear();
        paramArray.add((short)4);
        paramArray.add((short)4);
        parametersArray[0]=paramArray;
        result = (String)call.execute(parametersArray, method, errorsArray);
        Assert.assertTrue(((GXBaseCollection)errorsArray[0]).size()==0);
        Assert.assertTrue(result.trim().equals("24"));        
    }

    @Test
    public void callExternalClassWithStaticMethod(){
        Connect.init();
        GXDynamicCall call = new GXDynamicCall();
        GXDynCallProperties props = new GXDynCallProperties();
        Object[] errorsArray= new Object[1];
        props.setExternalName("DynamicCallExternalTestProcedure");
        props.setPackageName("com.genexus.gxdynamiccall.test");
        call.setProperties(props);
        //Parameters
        GXSimpleCollection<Object> paramArray = new GXSimpleCollection<Object>();
        paramArray.add((short)3);
        paramArray.add((short)4);
        Object[] parametersArray = {paramArray};
        //MethodConfiguration
        GXDynCallMethodConf method = new GXDynCallMethodConf();
        method.setIsStatic(true);
        method.setMethodName("sumAsString");
        String result = (String)call.execute(parametersArray, method, errorsArray);
        Assert.assertTrue(((GXBaseCollection)errorsArray[0]).size()==0);
        Assert.assertTrue(result.trim().equals("7"));       
    }
    
}