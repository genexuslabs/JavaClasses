package com.genexus.gxdynamiccall.test;
import com.genexus.Application;
import com.genexus.ModelContext;
import com.genexus.SdtMessages_Message;
import com.genexus.gxdynamiccall.GXDynCallMethodConf;

import com.genexus.gxdynamiccall.GXDynamicCall;

import org.junit.Assert;
import org.junit.Test;

import java.util.Vector;

public class GxDynamicCallTest  {

    @Test
    public void callGxNativeObject(){
        Application.init(com.genexus.gxdynamiccall.test.GXcfg.class);
        GXDynamicCall call = new GXDynamicCall(-1, new ModelContext(GxDynamicCallTest.class));
        call.setExternalName("com.genexus.gxdynamiccall.test.DynamicCallTestProcedure");
        Vector<Object> paramArray = new Vector<>();
        paramArray.add(Double.parseDouble("3"));
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
        Application.init(com.genexus.gxdynamiccall.test.GXcfg.class);
        GXDynamicCall call = new GXDynamicCall(-1, new ModelContext(GxDynamicCallTest.class));
		Vector<SdtMessages_Message> errorsArray= new Vector<>();
        call.setExternalName("com.genexus.gxdynamiccall.test.DynamicCallExternalTestProcedure");
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
        Application.init(com.genexus.gxdynamiccall.test.GXcfg.class);
        GXDynamicCall call = new GXDynamicCall(-1, new ModelContext(GxDynamicCallTest.class));
        call.setExternalName("com.genexus.gxdynamiccall.test.DynamicCallExternalTestProcedure");
		Vector<SdtMessages_Message> errorsArray= new Vector<>();
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