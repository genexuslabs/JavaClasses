package com.genexus.gxdynamiccall;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.GXSimpleCollection;
import com.genexus.SdtMessages_Message;
import com.genexus.common.interfaces.SpecificImplementation;

public class GXDynamicCall {

	private String externalName;
	private String packageName;
	private GXDynCallProperties properties;
	private Object instanceObject;
	private String objectName;

	
	public GXDynCallProperties getProperties() {
		return properties;
	}

	public void setProperties(GXDynCallProperties properties) {
		this.properties = properties;
		packageName = properties.getPackageName()==null?SpecificImplementation.Application.getPACKAGE():packageName;
		externalName = properties.getExternalName();
	}

	public String getObjectName(){
		return objectName;
	}

	public void setObjectName(String name){
		objectName=name;
	}

	public void execute(Object[] parametersArray, Object[] errorsArray) {
		//Create the instance with default constructor
		create(null, errorsArray);
		//Create methodconfiguration
		GXDynCallMethodConf method = new GXDynCallMethodConf();
		//Execute with thefault method configuration  
		execute(parametersArray, method, errorsArray);
	}

	public Object execute(Object[] parametersArray, GXDynCallMethodConf methodConfiguration, Object[] errorsArray) {
		
		GXBaseCollection<SdtMessages_Message> errors =new GXBaseCollection<SdtMessages_Message>();
		// Take the collection of parameters from de array
		GXSimpleCollection<Object> parameters = (GXSimpleCollection<Object>) parametersArray[0]; 
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
				return null;
			}
		}
		else
		{
			Class<?> auxClass=null;
			try {
				auxClass = loadClass(externalName, packageName);
			} catch (ClassNotFoundException e) {
				CommonUtil.ErrorToMessages("Load class Error", e.getMessage(), errors);
				errorsArray[0]=errors;
				return null;
			}
			objectToInvoke=auxClass;
		}
		result = executeMethod(objectToInvoke,methodConfiguration.getMethodName(), parameters, errors, methodConfiguration.getIsStatic());
		errorsArray[0]=errors;
		parametersArray[0] = parameters;
		return result;
	}

	public void create(GXSimpleCollection<Object> constructParameters, Object[] errors) {
		GXBaseCollection<SdtMessages_Message> error =new GXBaseCollection<SdtMessages_Message>();
		String objectNameToInvoke;
		objectNameToInvoke = constructParameters==null?objectName:externalName;
		if (!objectNameToInvoke.isEmpty()) {
			try {
				Class<?> objClass = loadClass(objectNameToInvoke, packageName);
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
				instanceObject = objClass.getConstructor(auxConstructorTypes).newInstance(auxConstParameters);
			} catch (Exception e) {
				CommonUtil.ErrorToMessages("CreateInstance Error", e.getMessage(), (GXBaseCollection<SdtMessages_Message>) error);
			}
		}
		else{
			CommonUtil.ErrorToMessages("CreateInstance Error", "Object name not set", (GXBaseCollection<SdtMessages_Message>) error);
		}
		errors[0]=error;
	}

	private Object executeMethod(Object objectToInvoke, String method, GXSimpleCollection<Object> params, GXBaseCollection<SdtMessages_Message> errors, boolean isStatic) {
		
		Object returnObject = null;
		Method[] methods;
		Method methodToExecute = null;
		Class<?>[] paramsType=new Class<?>[params.size()];
		Object[] callingParams = new Object[params.size()]; // Array to pass to method.invoke
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
									return null;
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

	private Class<?> loadClass(String className, String sPackage) throws ClassNotFoundException {
		String classPackage = sPackage + "." + className;
		Class<?> c = Class.forName(classPackage);;
		return c;
	}
//usar .size
//returns y no preguntar por los null
}