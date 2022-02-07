package com.genexus.gxdynamiccall;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.xml.GXXMLSerializable;



public class GXDynamicCall {

	private static Class<?>[] constructorClass = new Class[] {int.class, SpecificImplementation.Application.getModelContextClass()};
    public GXDynamicCall(){}
    public static void invoke(String objectToInvokeParm, Object[] parametersArray, String packa){


		GXBaseCollection<GXXMLSerializable> parameters = (GXBaseCollection<GXXMLSerializable>)parametersArray[0]; // Take the collection of parameters from de array
		Object [] callingParams = new Object[parameters.getItemCount()]; // Array to pass to method.invoke 
		// Get class 
		String objectToInvoke = getDynamicPgmName(GXDynamicCall.class, packa, objectToInvokeParm);
		Class<?> myClass = tryLoadClass(GXDynamicCall.class.getClassLoader(), objectToInvoke, true);

		// Get Method
		Method [] methods = myClass.getMethods();
		Method methodToExecute = null; 
		Class<?> destClass = null;
		for(int i = 0; i < methods.length; i++)
		{
			if(methods[i].getName().equalsIgnoreCase("execute")){
				methodToExecute = methods[i];
			}
		}
		if(methodToExecute!=null){
			//Create the parameters with the expected type in the method signe
			Class<?> [] destParmTypes = methodToExecute.getParameterTypes();
			if (destParmTypes.length==parameters.getItemCount()){
				int i=0;
				String value=null;
				for (Class<?> parmType : destParmTypes) {
					boolean destIsArray = parmType.isArray();
					GXXMLSerializable var=parameters.elementAt(i);
					try{
						//Get the value from the sdt pased to the EXO
						Field f = var.getClass().getDeclaredField("gxTv_SdtDynamicCallParameter_Value");
						f.setAccessible(true);
						value = (String) f.get(var);
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						
						if(destIsArray){
							destClass=parmType.getComponentType();
							Object[] array=(Object[])Array.newInstance(destClass, 1);
							array[0]=CommonUtil.convertObjectTo(value,destClass); 
							callingParams[i]=array;
						}
						else{
							destClass=parmType;
							callingParams[i]=CommonUtil.convertObjectTo(value,destClass);
						}	
						i++;
					}catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try
				{
					// Execute the method 
					methodToExecute.invoke(myClass.getConstructor(constructorClass).newInstance(new Object[] {-1, SpecificImplementation.Application.getModelContext(SpecificImplementation.Application.getModelContextClass())}), callingParams);
					updateParams(parameters, callingParams);
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException("IllegalAccessException Can't execute dynamic call " + myClass.getName() + " - " + e.getMessage());
				}
				catch (NoSuchMethodException e)
				{
					throw new RuntimeException("NoSuchMethodException Can't execute dynamic call " + myClass.getName() + " - " + e.getMessage());
				}
				catch (java.lang.reflect.InvocationTargetException e)
				{
					throw new RuntimeException("java.lang.reflect.InvocationTargetException Can't execute dynamic call " +  myClass.getName() + " - " + e.getTargetException().getMessage());
				}
				catch (InstantiationException e){
		
					throw new RuntimeException("InstantiationException Can't execute dynamic call " +  myClass.getName() + e.getMessage());
				}
				
			}
			else{
				throw new RuntimeException("InvalidArguments: Can't execute dynamic call " + objectToInvoke + " - number of parameters not match, recived (" + destParmTypes.length +") expected (" + parameters.getItemCount() + ")");
			}
		}
		else{
			throw new RuntimeException("NoSuchMethodException Can't execute dynamic call " + objectToInvoke);
		}
    }





    private static void updateParams(GXBaseCollection<GXXMLSerializable> originalParameter, Object [] callingParams)
	{

		for(int i=0; i< callingParams.length; i++){
            GXXMLSerializable parm = originalParameter.elementAt(i);
            Field f;
            try {
                f = parm.getClass().getDeclaredField("gxTv_SdtDynamicCallParameter_Value");
                f.setAccessible(true);
				if(callingParams[i].getClass().isArray()){
					Object [] auxArg=(Object[])callingParams[i];
                	f.set(parm, auxArg[0].toString());
				}else{
					Object auxArg=(Object)callingParams[i];
                	f.set(parm, auxArg.toString());
				}
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            	
        }
	}
	
	private static String getDynamicPgmName(Class<?> servlet, String sPackage, String sPgmName)
	{
		String classPackage = SpecificImplementation.Application.getPACKAGE();
		if	(!classPackage.equals(""))
			classPackage += ".";
		String pgmName = CommonUtil.getObjectName( sPackage , sPgmName );
		if (!classPackage.equals(sPackage))
		{
			try
			{
				Class pgmClass = Class.forName(pgmName);
			}
			catch (ClassNotFoundException e)
			{
				pgmName = CommonUtil.getObjectName( classPackage , sPgmName );
			}
		}

		ClassLoader cLoader = servlet.getClassLoader();
		Class<?> c = tryLoadClass(cLoader, pgmName, false);
		if (c == null)
		{
			c = tryLoadClass(cLoader, sPgmName, false);
			if (c != null)
			{
				pgmName = sPgmName;
			}
		}
		if (c == null)
		{
			throw new RuntimeException("ClassNotFoundException Can't execute dynamic call " + pgmName);
		}

		return pgmName;
	}


    public static Class<?> tryLoadClass(ClassLoader cLoader, String className, Boolean throwException)
	{
		Class<?> c = null;
		try
		{
			if (cLoader == null)
			{
				c = Class.forName(className);
			}
			else
			{
				c = cLoader.loadClass(className);
			}
		}
		catch (ClassNotFoundException e)
		{
			if (throwException)
			{
				throw new RuntimeException("ClassNotFoundException Can't execute dynamic call " + className + " - " + e.getMessage());
			}
		}
		return c;
	}


   
}