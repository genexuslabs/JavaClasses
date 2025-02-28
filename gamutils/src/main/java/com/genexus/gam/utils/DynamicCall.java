package com.genexus.gam.utils;

import com.genexus.ModelContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unused")
public class DynamicCall {
	private final ModelContext mContext;
	private final Integer mRemoteHandle;
	private static final Logger logger = LogManager.getLogger(DynamicCall.class);

	/********EXTERNAL OBJECT PUBLIC METHODS  - BEGIN ********/

	@SuppressWarnings("unused")
	public DynamicCall(ModelContext context, Integer remoteHandle) {
		mContext = context;
		mRemoteHandle = remoteHandle;
	}

	@SuppressWarnings("unused")
	public boolean execute(String assembly, String typeName, boolean useContext, String method, String jsonParms, String[] jsonOutput) {
		logger.debug("execute");
		return doCall(assembly, typeName, useContext, method, false, "", jsonParms, jsonOutput);
	}

	@SuppressWarnings("unused")
	public boolean executeEventHandler(String assembly, String typeName, boolean useContext, String method, String eventType, String jsonInput, String[] jsonOutput) {
		logger.debug("executeEventHandler");
		return doCall(assembly, typeName, useContext, method, true, eventType, jsonInput, jsonOutput);
	}

	/********EXTERNAL OBJECT PUBLIC METHODS  - END ********/


	private boolean doCall(String assembly, String typeName, boolean useContext, String method, boolean isEventHandler, String parm1, String parm2, String[] jsonOutput) {
		logger.debug("doCall");
		Object[] parms;
		Class<?>[] parmTypes;
		if (isEventHandler) {
			parms = new Object[]{parm1, parm2, new String[]{jsonOutput[0]}};
			parmTypes = new Class[]{String.class, String.class, String[].class};
		} else {
			parms = new Object[]{parm2, new String[]{jsonOutput[0]}};
			parmTypes = new Class[]{String.class, String[].class};
		}

		try {
			Class<?> myClass = Class.forName(typeName);
			Class<?>[] constructorParms;
			Constructor<?> constructor;
			Object instance = null;

			if (useContext && (mContext != null)) {
				try {
					constructorParms = new Class[]{int.class, ModelContext.class};
					constructor = myClass.getConstructor(constructorParms);
					instance = constructor.newInstance(new Object[]{mRemoteHandle, mContext});
				} catch (NoSuchMethodException e) {
					logger.error("doCall", e);
				}
			}

			if (instance == null) {
				constructorParms = new Class[]{int.class};
				constructor = myClass.getConstructor(constructorParms);
				instance = constructor.newInstance(new Object[]{-2});
			}

			myClass.getMethod(method, parmTypes).invoke(instance, parms);
		} catch (ClassNotFoundException e) {
			logger.error("doCall", e);
			jsonOutput[0] = "{\"error\":\"" + " class " + typeName + " not found" + "\"}";
			return false;
		} catch (NoSuchMethodException e) {
			logger.error("doCall", e);
			jsonOutput[0] = "{\"error\":\"" + " method " + method + " not found" + "\"}";
			return false;
		} catch (InstantiationException e) {
			logger.error("doCall", e);
			jsonOutput[0] = "{\"error\":\"" + " cannot instantiate type " + typeName + "\"}";
			return false;
		} catch (IllegalAccessException e) {
			logger.error("doCall", e);
			jsonOutput[0] = "{\"error\":\"" + " cannot access method " + method + "\"}";
			return false;
		} catch (InvocationTargetException e) {
			logger.error("doCall", e);
			jsonOutput[0] = "{\"error\":\"" + " InvocationTargetException in class " + typeName + "\"}";
			return false;
		}
		String[] result = (String[]) parms[parms.length - 1];
		jsonOutput[0] = result[0];
		logger.debug("doCall result {}", result[0]);
		return true;
	}
}
