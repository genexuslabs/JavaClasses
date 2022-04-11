package com.genexus.gxdynamiccall.test;
import com.genexus.GXBaseCollection;
import com.genexus.GXSimpleCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.gxdynamiccall.GXDynCallMethodConf;
import com.genexus.gxdynamiccall.GXDynCallProperties;
import com.genexus.gxdynamiccall.GXDynamicCall;
import com.genexus.specific.java.Connect;

import org.junit.Assert;
import org.junit.Test;

import java.util.Vector;

public class GxDynamicCallTest  {

    @Test
    public void callGxNativeObject(){
        Connect.init();
        GXDynamicCall call = new GXDynamicCall();
        call.setObjectName("com.genexus.gxdynamiccall.test.DynamicCallTestProcedure");
        Vector<Object> paramArray = new Vector<>();
        paramArray.add((short)3);
        paramArray.add((short)4);
        paramArray.add(new String());
        Vector<SdtMessages_Message> errorsArray= new Vector<>();
        call.execute(paramArray, errorsArray);
        Assert.assertTrue(errorsArray.size()==0);
        String parm = ((String)paramArray.get(2)).trim();
        Assert.assertTrue(parm.equals("7"));
    }

    @Test
    public void callExternalClass(){
        Connect.init();
        GXDynamicCall call = new GXDynamicCall();
        GXDynCallProperties props = new GXDynCallProperties();
		Vector<SdtMessages_Message> errorsArray= new Vector<>();
        props.setExternalName("DynamicCallExternalTestProcedure");
        props.setPackageName("com.genexus.gxdynamiccall.test");
        call.setProperties(props);
        //Constructor 
        Vector<Object> constructParamArray = new Vector<>();
        constructParamArray.add((int)3); 
        call.create(constructParamArray, errorsArray);
        //Parameters
        Vector<Object> paramArray = new Vector<>();
        paramArray.add((short)3);
        paramArray.add((short)4);
        //MethodConfiguration
        GXDynCallMethodConf method = new GXDynCallMethodConf();
        method.setIsStatic(false);
        method.setMethodName("calculateAsString");
        String result = (String)call.execute(paramArray, method, errorsArray);
        Assert.assertTrue(errorsArray.size()==0);
        Assert.assertTrue(result.trim().equals("21"));
        paramArray.clear();
        paramArray.add((short)4);
        paramArray.add((short)4);
        result = (String)call.execute(paramArray, method, errorsArray);
        Assert.assertTrue(errorsArray.size()==0);
        Assert.assertTrue(result.trim().equals("24"));        
    }

    @Test
    public void callExternalClassWithStaticMethod(){
        Connect.init();
        GXDynamicCall call = new GXDynamicCall();
        GXDynCallProperties props = new GXDynCallProperties();
		Vector<SdtMessages_Message> errorsArray= new Vector<>();
        props.setExternalName("DynamicCallExternalTestProcedure");
        props.setPackageName("com.genexus.gxdynamiccall.test");
        call.setProperties(props);
        //Parameters
		Vector<Object> paramArray = new Vector<>();
        paramArray.add((short)3);
        paramArray.add((short)4);
        //MethodConfiguration
        GXDynCallMethodConf method = new GXDynCallMethodConf();
        method.setIsStatic(true);
        method.setMethodName("sumAsString");
        String result = (String)call.execute(paramArray, method, errorsArray);
        Assert.assertTrue(errorsArray.size()==0);
        Assert.assertTrue(result.trim().equals("7"));       
    }
    
}