package com.artech.base.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionAPIHelper {

	public static Object executeAPIMethodStatic(String apiName, String fullClassName, String methodName,
			Object[] params) {
		Class<?> clazz = getClass(fullClassName);
		if (clazz != null) {
			Method method = getMethod(clazz, methodName, params);
			if (method != null)
				return executeMethod(null, method, params);
		}
		return null;
	}

	public static Object executeAPIMethod(String apiName, String fullClassName, String methodName, Object[] params) {
		Class<?> clazz = getClass(fullClassName);
		if (clazz != null) {
			Object instance = createDefaultInstance(clazz);
			Method method = getMethod(clazz, methodName, params);

			if (instance != null && method != null)
				return executeMethod(instance, method, params);
		}
		return null;
	}

	public static Object executeAPIEvent(String apiName, String fullClassName, String methodName, Object[] params) {
		Class<?> clazz = getClass(fullClassName);
		if (clazz != null) {
			// get method to execute EO event
			Object[] allParams = new Object[] { apiName, methodName, params };

			Method method = getMethod(clazz, "fireEvent", allParams);
			if (method != null)
				return executeMethod(null, method, allParams);
		}
		return null;
	}

	public static Class<?> getClass(String fullName) {
		try {
			Class<?> clazz = Class.forName(fullName);
			return clazz;
		} catch (ClassNotFoundException e) {
			System.err.println(String.format("Class '%s' could not be loaded via reflection.", fullName)); //$NON-NLS-1$
			return null;
		}
	}

	public static Object createDefaultInstance(Class<?> clazz) {
		try {
			if (clazz == null) {
				System.err.println("Class not provided to getDefaultInstance()."); //$NON-NLS-1$
				return null;
			}

			Constructor<?> constructor = null;

			constructor = clazz.getConstructor();

			if (constructor == null) {
				System.err.println(String.format("Class '%s' does not have a default constructor.", clazz.getName())); //$NON-NLS-1$
				return null;
			}

			Object instance = null;
			instance = constructor.newInstance();
			// instance = constructor.newInstance(-1);

			return instance;
		} catch (IllegalAccessException | IllegalArgumentException | InstantiationException
			| InvocationTargetException | ExceptionInInitializerError | NoSuchMethodException ex) {
			if (clazz != null)
				System.err.println(
						String.format("Exception creating instance of class '%s' by reflection.", clazz.getName())); //$NON-NLS-1$
			System.err.println(ex.getMessage()); // $NON-NLS-1$
			return null;
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Object[] params) {
		try {
			if (clazz == null) {
				System.err.println("Class not provided to getMethod()."); //$NON-NLS-1$
				return null;
			}
			Class<?>[] paramsTypes = null;
			if (params != null && params.length > 0) {
				paramsTypes = new Class<?>[params.length];
				for (int index = 0; index < params.length; index++) {
					Object param = params[index];
					paramsTypes[index] = param.getClass();
				}
			}
			Method method = clazz.getMethod(name, paramsTypes);
			return method;
		} catch (NoSuchMethodException | SecurityException ex) {
			if (clazz != null) {
				for (Method method : clazz.getMethods()) {
					if (name.equals(method.getName())) {
						boolean validParameters = true;
						for (int index = 0; index < params.length; index++) {
							Object param = params[index];
							if (!method.getParameterTypes()[index].isAssignableFrom(param.getClass())) {
								validParameters = false;
								break;
							}
						}
						if (validParameters)
							return method;
					}
				}
			}

			if (name != null)
				System.err.println(String.format("Exception creating instance of method '%s' by reflection.", name)); //$NON-NLS-1$
			System.err.println(ex.getMessage()); // $NON-NLS-1$
			return null;
		}
	}

	public static Object executeMethod(Object instance, Method method, Object[] params) {
		try {
			return method.invoke(instance, params);
		} catch (InvocationTargetException e) {
			String reason = String.format("Exception was thrown by method '%s'", method.getName());
			throw new RuntimeException(reason, e.getCause());
		} catch (IllegalAccessException e) {
			String reason = String.format("An illegal access when trying to access method '%s", method.getName());
			throw new RuntimeException(reason, e.getCause());
		}
	}
}
