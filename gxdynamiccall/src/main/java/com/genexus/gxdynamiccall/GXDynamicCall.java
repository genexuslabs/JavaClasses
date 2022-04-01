package com.genexus.gxdynamiccall;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.GXSimpleCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.xml.GXXMLSerializable;

public class GXDynamicCall {

	private String defaultMethod;
	private String namespace;
	private String externalName;
	private String packageName;
	private GXXMLSerializable properties;
	private Object object;
	public String ObjectName;

	public GXDynamicCall() {

		namespace = null;
		properties = null;
		externalName = null;
		object = null;
		defaultMethod = "execute";
	}

	public Object getProperties() {
		return properties;
	}

	public void setProperties(GXXMLSerializable props) {
		try {
			properties = props;
			Field f;
			f = props.getClass().getDeclaredField("gxTv_SdtJavaProperties_Externalname");
			f.setAccessible(true);
			externalName = (String) f.get(props);
			f = props.getClass().getDeclaredField("gxTv_SdtJavaProperties_Namespace");
			f.setAccessible(true);
			namespace = (String) f.get(props);
			f = props.getClass().getDeclaredField("gxTv_SdtJavaProperties_Packagename");
			f.setAccessible(true);
			packageName = (String) f.get(props);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public String getObjectName(){
		return ObjectName;
	}

	public void setObjectName(String name){
		this.ObjectName=name;
	}

	private void VerifyDefaultProperties() {
		if (packageName.isEmpty()) {
			packageName = SpecificImplementation.Application.getPACKAGE();
			if (!packageName.equals("")) {
				packageName += ".";
			}
		}

	}

	public void Execute(Object[] parametersArray, Object[] errorsArray) {
		// Take the collection of parameters from de array
	    GXSimpleCollection<Object> parameters = (GXSimpleCollection<Object>) parametersArray[0]; 

		Create(null, errorsArray);
		// Take  the collection of Message from  de array
		GXBaseCollection<SdtMessages_Message> errors = (GXBaseCollection<SdtMessages_Message>) errorsArray[0]; 
		if (errors.size() == 0) {
			try {
				this.ExecuteMethod(this.object, this.defaultMethod, parameters, errors,false);
				parametersArray[0] = parameters;
			} catch (Exception e) {
				CommonUtil.ErrorToMessages("CallMethod Error", e.getMessage(), errors);
			}
		}
	}

	public Object Execute(Object[] parametersArray, GXXMLSerializable methodconfiguration, Object[] errorsArray) {
		// Take  the collection of Message from  de array
		GXBaseCollection<SdtMessages_Message> errors = (GXBaseCollection<SdtMessages_Message>) errorsArray[0]; 
		// Take the collection of parameters from de array
		Object result=null;
		Object objectToInvoke;
	    GXSimpleCollection<Object> parameters = (GXSimpleCollection<Object>) parametersArray[0]; 
		Field f;
		boolean isStatic=false;
		String methodName;
		try {
			f = methodconfiguration.getClass().getDeclaredField("gxTv_Java");
			f.setAccessible(true);
			GXXMLSerializable methodPlatformSubLevel=(GXXMLSerializable) f.get(methodconfiguration);
			f = methodPlatformSubLevel.getClass().getDeclaredField("gxTpr_Methodname");
			f.setAccessible(true);
			methodName = (String)f.get(methodconfiguration);
			f = methodPlatformSubLevel.getClass().getDeclaredField("gxTpr_Methodisstatic");
			f.setAccessible(true);
			isStatic = (boolean)f.get(methodconfiguration);
			if (!isStatic)
			{
				if (this.object != null)
				{
					objectToInvoke = this.object;
					//result = this.ExecuteMethod(this.object, methodName.isEmpty() ? defaultMethod : methodName, parameters, errors, isStatic);
				}
				else
				{
					objectToInvoke=null;
					CommonUtil.ErrorToMessages("NullInstance Error", "You must invoke create method before execute a non static one", errors);
				}
			}
			else
			{
				VerifyDefaultProperties();
				Class<?> auxClass=null;
				try {
					auxClass = loadClass(GXDynamicCall.class.getClassLoader(), this.externalName, packageName);
				} catch (ClassNotFoundException e) {
					CommonUtil.ErrorToMessages("Load class Error", e.getMessage(), errors);
				}
				objectToInvoke=auxClass;
			}
			if(errors.getItemCount()==0 && objectToInvoke!=null){
				result = this.ExecuteMethod(objectToInvoke, methodName.isEmpty() ? defaultMethod : methodName, parameters, errors, isStatic);
			}
			errorsArray[0]=errors;
			parametersArray[0] = parameters;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			CommonUtil.ErrorToMessages("MethodProperties Error", e.getMessage(), errors);
		}
		return result;
	}

	public void Create(GXSimpleCollection<Object> constructParameters, Object[] errors) {
		if (errors == null) {
			errors = (Object[]) Array.newInstance(Object.class, 1); 
			errors[0]=new GXBaseCollection<SdtMessages_Message>();
		}
		String objectNameToInvoke;


		VerifyDefaultProperties();
		if (constructParameters == null) {
			objectNameToInvoke = this.ObjectName;
		} else {
			objectNameToInvoke = this.externalName;
		}
		if (!objectNameToInvoke.isEmpty()) {
			try {
				Class<?> objClass = loadClass(GXDynamicCall.class.getClassLoader(), objectNameToInvoke, packageName);
				Object[] auxConstParameters;
				Class<?>[] auxConstructorTypes;
				if (constructParameters != null && constructParameters.size() > 0) {
					auxConstructorTypes = new Class<?>[constructParameters.size()];
					auxConstParameters = constructParameters.toArray();
					int i = 0;
					for (Object obj : constructParameters) {
						auxConstructorTypes[i] = obj.getClass();
						i++;
					}
				} else {
					auxConstParameters = (Object[]) Array.newInstance(Object.class, 0);
					auxConstructorTypes = new Class[] { int.class,
							SpecificImplementation.Application.getModelContextClass() };
				}
				object = objClass.getConstructor(auxConstructorTypes).newInstance(auxConstParameters);
			} catch (Exception e) {
				CommonUtil.ErrorToMessages("CreateInstance Error", e.getMessage(), (GXBaseCollection<SdtMessages_Message>) errors[0]);
			}
		}
		else{
			CommonUtil.ErrorToMessages("CreateInstance Error", "Object name not set", (GXBaseCollection<SdtMessages_Message>) errors[0]);
		}
	}

	private Object ExecuteMethod(Object objectToInvoke, String method, GXSimpleCollection<Object> params, GXBaseCollection<SdtMessages_Message> errors, boolean isStatic) {
		// Get Method
		Object returnObject = null;
		Method[] methods;
		Method methodToExecute = null;
		Class<?>[] paramsType=new Class<?>[params.getItemCount()];
		Object[] callingParams = new Object[params.getItemCount()]; // Array to pass to method.invoke
		try{
			for (int i=0; i < params.size(); i++) {
				paramsType[0]=params.get(i).getClass();	
			}
			if(isStatic){
				methodToExecute = ((Class<?>)objectToInvoke).getDeclaredMethod(method,paramsType);
			}else{
				methodToExecute = objectToInvoke.getClass().getMethod(method,paramsType);
			}
		}
		catch(NoSuchMethodException e){

			if(isStatic){
				methods = ((Class<?>)objectToInvoke).getDeclaredMethods();
			}else{
				methods = objectToInvoke.getClass().getMethods();
			}
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equalsIgnoreCase(method) && methods[i].getParameterCount() == params.size()) {
					methodToExecute = methods[i];
				}
			}
			if (methodToExecute != null) {
				// Create the parameters with the expected type in the method signe
				Class<?>[] destParmTypes = methodToExecute.getParameterTypes();
				Class<?> destClass;
				int i = 0;
				for (Class<?> parmType : destParmTypes) {
					boolean destIsArray = parmType.isArray();
					Object parm = params.elementAt(i);
	
					try {
						if (destIsArray) {
							destClass = parmType.getComponentType();
							Object[] array = (Object[]) Array.newInstance(destClass, 1);
							if (parm.getClass() != destClass) {
								if (parm.getClass() != String.class) { // To avoid convert from string
									array[0] = CommonUtil.convertObjectTo(parm, destClass);
								} else {
									CommonUtil.ErrorToMessages("CallMethod Error", "IllegalArgumentException - Type does not match", errors);
								}
							} else {
								array[0] = parm;
							}
							callingParams[i] = array;
						} else {
							destClass = parmType.getClass();
							if (parm.getClass() != destClass) {
								if (parm.getClass() != String.class) { // To avoid convert from string
									callingParams[i] = CommonUtil.convertObjectTo(parm, destClass);
								} else {
									CommonUtil.ErrorToMessages("CallMethod Error", "IllegalArgumentException - Type does not match", errors);
								}
							} else {
								callingParams[i] = parm;
							}
						}
						i++;
					} catch (Exception ex) {
						CommonUtil.ErrorToMessages("CallMethod Error", ex.getMessage(), errors);
					}
				}
			}
			else{
				CommonUtil.ErrorToMessages("CallMethod Error","NoSuchMethodException - Cant finde method: " + method + "with " + params.size() + " parameters", errors);
			}
		}
		if (methodToExecute != null) {
			try {
				// Execute the method
				returnObject = methodToExecute.invoke(isStatic?null:objectToInvoke, callingParams);
				updateParams(params, callingParams);
			} catch (Exception e) {
				CommonUtil.ErrorToMessages("CallMethod Error", e.getMessage(), errors);
			}
		} else {
			CommonUtil.ErrorToMessages("CallMethod Error","NoSuchMethodException - Cant finde method: " + method + "with " + params.size() + " parameters", errors);
		}
		return returnObject;
	}

	private static void updateParams(GXSimpleCollection<Object> originalParameter, Object[] callingParams)
			throws Exception {

		for (int i = 0; i < callingParams.length; i++) {
			Object parm = originalParameter.get(i);
			Object objectToUpdate;
			if (callingParams[i].getClass().isArray()) {
				Object[] auxArg = (Object[]) callingParams[i];
				if (auxArg[0].getClass() != parm.getClass()) {
					objectToUpdate = CommonUtil.convertObjectTo(auxArg[0], parm.getClass());
				} else {
					objectToUpdate = auxArg[0];
				}
			} else {
				if (callingParams[i].getClass() != parm.getClass()) {
					objectToUpdate = CommonUtil.convertObjectTo(callingParams[i], parm.getClass());
				} else {
					objectToUpdate = callingParams[i];
				}
			}
			originalParameter.set(i, objectToUpdate);
		}
	}

	private Class<?> loadClass(ClassLoader cLoader, String className, String sPackage) throws ClassNotFoundException {
		String classPackage = SpecificImplementation.Application.getPACKAGE();
		if (!classPackage.equals(""))
			classPackage += ".";
		String pgmName = CommonUtil.getObjectName(sPackage, className);
		Class<?> c = null;
		if (!classPackage.equals(sPackage)) {

			try {
				c = Class.forName(pgmName);
			} catch (ClassNotFoundException e) {
				pgmName = CommonUtil.getObjectName(classPackage, className);
			}
		}

		if (c == null) {
			if (cLoader == null) {
				c = Class.forName(pgmName);
			} else {

				c = cLoader.loadClass(pgmName);
			}
		}
		return c;
	}

}