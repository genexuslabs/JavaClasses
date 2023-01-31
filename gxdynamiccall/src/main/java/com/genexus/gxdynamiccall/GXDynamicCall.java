package com.genexus.gxdynamiccall;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.common.interfaces.SpecificImplementation;

public class GXDynamicCall {

	private GXDynCallProperties properties;
	private Object instanceObject;
	private String objectName;

	public GXDynamicCall(){
		properties = new GXDynCallProperties();
		properties.setPackageName(SpecificImplementation.Application.getPACKAGE());
	}

	public GXDynCallProperties getProperties() {
		return properties;
	}

	public void setProperties(GXDynCallProperties properties) {
		this.properties = properties;
	}

	public String getObjectName(){
		return objectName;
		
	}

	public void setObjectName(String name){
		objectName=name;
		properties.setExternalName(name);
	}

	public void execute(Vector<Object> parameters, Vector<SdtMessages_Message> errorsArray) {
		//Create the instance with default constructor
		create(null, errorsArray);
		//Create methodconfiguration
		if(errorsArray.size()==0){
			GXDynCallMethodConf method = new GXDynCallMethodConf();
			//Execute with default method configuration  
			execute(parameters, method, errorsArray);
		}
	}

	public Object execute(Vector<Object> parameters, GXDynCallMethodConf methodConfiguration, Vector<SdtMessages_Message> errorsArray) {
		
		GXBaseCollection<SdtMessages_Message> errors =new GXBaseCollection<SdtMessages_Message>();
		Object result=null;
		Object objectToInvoke;
		if (!methodConfiguration.getIsStatic())
		{
			if (instanceObject != null)
			{
				objectToInvoke = instanceObject;
			}
			else
			{
				objectToInvoke=null;
				CommonUtil.ErrorToMessages("NullInstance Error", "You must invoke create method before execute a non static one", errors);
				errorsArray.addAll(errors.getStruct());
				return null;
			}
		}
		else
		{
			Class<?> auxClass=null;
			try {
				auxClass = loadClass(properties.getExternalName(),properties.getPackageName());
			} catch (ClassNotFoundException e) {
				CommonUtil.ErrorToMessages("Load class Error", e.getMessage(), errors);
				errorsArray.addAll(errors.getStruct());
				return null;
			}
			objectToInvoke=auxClass;
		}
		result = executeMethod(objectToInvoke,methodConfiguration.getMethodName(), parameters, errors, methodConfiguration.getIsStatic());
		errorsArray.addAll(errors.getStruct());
		return result;
	}

	public void create(Vector<Object> constructParameters, Vector<SdtMessages_Message> errors) {
		GXBaseCollection<SdtMessages_Message> error =new GXBaseCollection<SdtMessages_Message>();
		String objectNameToInvoke;
		Constructor<?> constructor=null;
		objectNameToInvoke = constructParameters==null?objectName:properties.getExternalName();
		if (!objectNameToInvoke.isEmpty()) {
			try {
				Class<?> objClass = loadClass(objectNameToInvoke, properties.getPackageName());
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
					auxConstParameters = new Object[] {Integer.valueOf(-1)};
					auxConstructorTypes = new Class[] {int.class};
				}
				try{
				 	constructor =  objClass.getConstructor(auxConstructorTypes);
				}catch(Exception e1){
					Constructor<?> [] constructors = objClass.getConstructors();
					for (Constructor<?> acutualCons : constructors) {
						if(acutualCons.getParameterCount() == Array.getLength(auxConstParameters)){
							constructor=acutualCons;
							
						}
					}
				}
				if(constructor != null){
					instanceObject=constructor.newInstance(auxConstParameters);
				}
				else{
					CommonUtil.ErrorToMessages("CreateInstance Error", "None constructor found", error);
					errors.addAll(error.getStruct());
					return;
				}
			} catch (Exception e) {
				CommonUtil.ErrorToMessages("CreateInstance Error", e.getMessage(), error);
				e.printStackTrace();
				errors.addAll(error.getStruct());
				return;
			}
		}
		else{
			CommonUtil.ErrorToMessages("CreateInstance Error", "Object name not set", error);
			errors.addAll(error.getStruct());
			return;
		}
		
	}

	private Object executeMethod(Object objectToInvoke, String method, Vector<Object> params, GXBaseCollection<SdtMessages_Message> errors, boolean isStatic) {
		
		Object returnObject = null;
		Method[] methods;
		Method methodToExecute = null;
		Class<?>[] paramsType=new Class<?>[params.size()];
		Object[] callingParams = new Object[params.size()]; // Array to pass to method.invoke
		try{
			for (int i=0; i < params.size(); i++) {
				paramsType[i]=params.get(i).getClass();	
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
									return null;
								}
							} else {
								array[0] = parm;
							}
							callingParams[i] = array;
						} else {
							if (parm.getClass() != parmType) {
								if (parm.getClass() != String.class) { // To avoid convert from string
									callingParams[i] = CommonUtil.convertObjectTo(parm,parmType);
								} else {
									CommonUtil.ErrorToMessages("CallMethod Error", "IllegalArgumentException - Type does not match", errors);
									return null;
								}
							} else {
								callingParams[i] = parm;
							}
						}
						i++;
					} catch (Exception ex) {
						CommonUtil.ErrorToMessages("CallMethod Error", ex.getMessage(), errors);
						return null;
					}
				}
			}
			else{
				CommonUtil.ErrorToMessages("CallMethod Error","NoSuchMethodException - Cant finde method: " + method + "with " + params.size() + " parameters", errors);
				return null;
			}
		}
		
		try {
			// Execute the method
			returnObject = methodToExecute.invoke(isStatic?null:objectToInvoke, callingParams);
			updateParams(params, callingParams);
		} catch (Exception e) {
			CommonUtil.ErrorToMessages("CallMethod Error", e.getMessage(), errors);
			return null;
		}
		
		return returnObject;
	}

	private static void updateParams(Vector<Object> originalParameter, Object[] callingParams)
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

	private Class<?> loadClass(String className, String sPackage) throws ClassNotFoundException {
		String classPackage="";
		if(sPackage != null)
		 classPackage+=  sPackage + ".";
		classPackage+= className;
		Class<?> c = Class.forName(classPackage);;
		return c;
	}
}