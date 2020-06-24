package com.genexus.db;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.CommonUtil;
import java.math.BigDecimal;
import java.lang.reflect.*;

public class DynamicExecute
{
	private static Class[] intClass = new Class[] {int.class};
	private static Class[] constructorClass = new Class[] {int.class, SpecificImplementation.Application.getModelContextClass()};

	public static void dynamicExecute(int handle, String pgmName)
	{
		dynamicExecute(SpecificImplementation.Application.getModelContext(SpecificImplementation.Application.getModelContextClass()), handle, SpecificImplementation.Application.getApplicationClass(), SpecificImplementation.GXutil.getClassName(pgmName.toLowerCase()), new Object[0]);
	}

	public static String dynamicWebExecute(ModelContext context, int handle, Class servlet, String wjLoc, String wjAuxLoc, String sPackage, String sPgmName, Object[] parms)
	{
		String pgmName = getDynamicPgmName(servlet, sPackage, sPgmName);
		ClassLoader cLoader = servlet.getClassLoader();
		Class c = tryLoadClass(cLoader, pgmName, false);

		boolean isWebContext = false;
		if (SpecificImplementation.DynamicExecute != null)
			isWebContext = SpecificImplementation.DynamicExecute.getIsWebContext(context);

		if (isWebContext && c.getSuperclass().equals(SpecificImplementation.Application.getGXWebObjectStubClass()))
		{
			// Es un call a un webpanel
			String objetName;
			if (wjAuxLoc.startsWith(sPackage))
			{
				sPackage = "";
			}
			int idx = wjAuxLoc.indexOf('?');
			if (idx >= 0)
			{
				objetName = (sPackage + wjAuxLoc.substring(0, idx)).toLowerCase();
				return objetName + wjAuxLoc.substring(idx);
			}
			else
			{
				return (sPackage + wjAuxLoc).toLowerCase();
			}
		}
		else
		{
			dynamicExecute(context, handle, servlet, pgmName, parms);
		}
		return wjLoc;
	}

	private static String getDynamicPgmName(Class servlet, String sPackage, String sPgmName)
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
		Class c = tryLoadClass(cLoader, pgmName, false);
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

	public static Class tryLoadClass(ClassLoader cLoader, String className, Boolean throwException)
	{
		Class c = null;
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
    public static boolean dynamicInstaceExecute(Object instance, String method, Object[] parms) {

        Class[] parmTypes = null;
        if (parms != null) {
            parmTypes = new Class[parms.length];

            for (int i = parms.length - 1; i >= 0; i--)
                parmTypes[i] = parms[i].getClass();
        }
        Class<?> myClass = instance.getClass();
        String pgmName = myClass.getName();

        try {
            myClass.getMethod(method,parmTypes).invoke(instance, parms);
        } catch (IllegalAccessException e) {
        	SpecificImplementation.Application.printWarning(
                    "IllegalAccessException Can't execute dynamic call " +
                    pgmName + " - " + e.getMessage(), e);
            return false;
        } catch (NoSuchMethodException e) {
        	SpecificImplementation.Application.printWarning(
                    "NoSuchMethodException Can't execute dynamic call " +
                    pgmName + " - " + e.getMessage(), e);
            return false;
        } catch (java.lang.reflect.InvocationTargetException e) {
        	SpecificImplementation.Application.printWarning(
                    "java.lang.reflect.InvocationTargetException Can't execute dynamic call " +
                    pgmName + " - " + e.getTargetException().getMessage(),
                    e);
            return false;
        }
        return true;
    }
	private static boolean dynamicExecute2(ModelContext context, int handle, Class caller, String pgmName, Object[] parms)
	{
		Class[] parmTypes = new Class [parms.length];

		for (int i = parms.length - 1; i >= 0; i--)
			parmTypes[i] = parms[i].getClass();

		Class<?> myClass;

		try
		{
			if	(caller.getClassLoader() == null)
				myClass = Class.forName(pgmName);
			else
				myClass = caller.getClassLoader().loadClass(pgmName);
		}
		catch (ClassNotFoundException e)
		{
			SpecificImplementation.Application.printWarning("ClassNotFoundException Can't execute dynamic call " + pgmName + " - " + e.getMessage(), e);
			return false;
		}

		try
		{
		    myClass.getMethod("execute", parmTypes).invoke( myClass.getConstructor(constructorClass).newInstance(new Object[] {new Integer(handle), context}), parms);
		}
		catch (IllegalAccessException e)
		{
			SpecificImplementation.Application.printWarning("IllegalAccessException Can't execute dynamic call " + pgmName + " - " + e.getMessage(), e);
			return false;
		}
		catch (NoSuchMethodException e)
		{
			SpecificImplementation.Application.printWarning("NoSuchMethodException Can't execute dynamic call " + pgmName + " - " + e.getMessage(), e);
			return false;
		}
		catch (java.lang.reflect.InvocationTargetException e)
		{
			SpecificImplementation.Application.printWarning("java.lang.reflect.InvocationTargetException Can't execute dynamic call " + pgmName + " - " + e.getTargetException().getMessage(), e);
			return false;
		}
		catch (InstantiationException e)
		{
			SpecificImplementation.Application.printWarning("InstantiationException Can't execute dynamic call " + pgmName + e.getMessage(), e);
			return false;
		}
		return true;
	}

	private final static String METHOD_EXECUTE = "execute"; // El m�todo a ejecutar en la clase
	public static boolean dynamicExecute(ModelContext context, int handle, Class caller, String sPackage, String sPgmName, Object[] params)
	{
		String pgmName = getDynamicPgmName(caller, sPackage, sPgmName);
		return dynamicExecute(context, handle, caller, pgmName, params);
	}

	public static boolean dynamicExecute(ModelContext context, int handle, Class caller, String className, Object[] params)
	{
		Object [] callingParams = new Object[params.length]; // Contiene el verdadero array a pasarle a la clase
		boolean [] needToUpdateParams = new boolean[params.length]; // Indica si hay que actualizar el array de parametros(params) al terminar la invocaci�n del m�todo. Solo se deben actualizar los parametros que en destino son 'arrays', que son los que pueden sufrir modificaci�n

		// Primero obtengo la clase a ejecutar
		Class<?> myClass = tryLoadClass(caller.getClassLoader(), className, true);

		// Ahora matcheo los parametros
		Method [] methods = myClass.getMethods();
		Method methodToExecute = null; // method va a contener el m�todo a ejecutar
		Class<?> sourceClass = null, destClass = null;
nextMethod:
		for(int i = 0; i < methods.length; i++)
		{
			Method method = methods[i];
			if(!method.getName().equalsIgnoreCase(METHOD_EXECUTE))continue;
			Class<?> [] parameters = method.getParameterTypes();

			// Primero verificamos que la cantidad de parametros sea la misma
			if(parameters.length != params.length)continue;

			// Ahora vemos si podemos matchear uno a uno los parametros
			for(int j = 0; j < parameters.length ; j++)
			{
				sourceClass = params[j].getClass();
				needToUpdateParams[j] = false; // Por defecto no es necesario actualizar este par�metro
				if(parameters[j].equals(sourceClass) || parameters[j].isAssignableFrom(sourceClass))
				{ // Si matchean directamente, no tengo que hacer nada, simplemente copio los datos de entrada a los callingParams
				  // e indico que los datos de entrada para este par�metro no tienen que ser actualizados
				  // a la salida de la invocaci�n
					callingParams[j] = params[j];
				}
				else
				{ // Si directamente no matchean, tengo que ver las conversiones
				  // Para eso discrimino entre los tipos de dato

					// DestIsArray me indica si el destino es array
					// DestClass es un Class que indica el tipo del destino (independientemente de si es array o no)
					boolean destIsArray = parameters[j].isArray();
					if(destIsArray)
					{
						destClass = parameters[j].getComponentType();
					}
					else
					{
						destClass = parameters[j];
					}

					boolean sourceIsArray = sourceClass.isArray();
					int sourceArraySize = 1;
					if(sourceIsArray)
					{
						sourceClass = sourceClass.getComponentType();
						sourceArraySize = Array.getLength(params[j]);
					}

					// Si el dest era array, entonces es necesario actualizar los parametros al terminar la invocaci�n del m�todo
					// Nota: Otra posibilidad es pedir que destIsArray y que sourceIsArray a la vez!
					if(destIsArray)needToUpdateParams[j] = true;

					if(destClass.equals(sourceClass))
					{
						// Si los tipos de datos son compatibles, solo debo compatibilizar los arrays
						if(destIsArray)
						{ // Si el destino es array, copio los datos de entrada al callingParams
							callingParams[j] = Array.newInstance(destClass, sourceArraySize);
							if(sourceIsArray)
							{
								for(int arrayIndex = sourceArraySize-1; arrayIndex >= 0; arrayIndex--)
									Array.set(callingParams[j], arrayIndex, ((Object[])params[j])[arrayIndex]);
							}
							else Array.set(callingParams[j], 0, params[j]);
						}
						else
						{ // Si el destino NO es array, intento compatibilizar los arrays de source metiendo solo el primer item
							if(sourceIsArray)callingParams[j] = Array.get(params[j], 0);
							else callingParams[j] = params[j];
						}
					}
					else
					{ // Si las clases no matchean, intento hacer una conversion

						// Primero creo el objeto que con el que voy a pasar los parametros
						// si es que es un array
						if(destIsArray)
						{
							switch(getPrimitiveType(destClass))
							{
							case TYPE_BYTE: callingParams[j] = new byte[sourceArraySize]; break;
							case TYPE_CHARACTER: callingParams[j] = new char[sourceArraySize]; break;
							case TYPE_SHORT: callingParams[j] = new short[sourceArraySize]; break;
							case TYPE_INTEGER: callingParams[j] = new int[sourceArraySize]; break;
							case TYPE_LONG: callingParams[j] = new long[sourceArraySize]; break;
							case TYPE_FLOAT: callingParams[j] = new float[sourceArraySize]; break;
							case TYPE_DOUBLE: callingParams[j] = new double[sourceArraySize]; break;
							case TYPE_BIG_DECIMAL: callingParams[j] = new BigDecimal[sourceArraySize]; break;
							}
						}

						// Y ahora le asigno los valores
						for(int arrayIndex = sourceArraySize-1; arrayIndex >= 0; arrayIndex--)
						{
							boolean destIsBigDecimal = destClass.isAssignableFrom(BigDecimal.class);
							if(destClass.isPrimitive() || destIsBigDecimal)
							{	// Si la clase destino es del tipo primitivo
								double arg = -1;
								BigDecimal bigDecimalArg = null; //solo es usado si destIsBigDecimal
								if(sourceClass.isPrimitive())
								{
									if(sourceIsArray)
									{
										arg = Array.getDouble(params[j], arrayIndex);
									}
								}
								else
								{
									if(sourceClass.isAssignableFrom(BigDecimal.class))
									{ // Aqui no queremos perder precisi�n si el destIsBigDecimal
										if(sourceIsArray)
										{
											if(destIsBigDecimal)bigDecimalArg = ((BigDecimal[])params[j])[arrayIndex];
											else arg = (((BigDecimal[])params[j])[arrayIndex]).doubleValue();
										}
										else
										{
											if(destIsBigDecimal)bigDecimalArg = ((BigDecimal)params[j]);
											else arg = ((BigDecimal)params[j]).doubleValue();
										}
									}
									else
										if(sourceClass.isAssignableFrom(Byte.class) || sourceClass.isAssignableFrom(Character.class) ||
										   sourceClass.isAssignableFrom(Short.class) || sourceClass.isAssignableFrom(Integer.class) ||
										   sourceClass.isAssignableFrom(Long.class) || sourceClass.isAssignableFrom(Float.class) || sourceClass.isAssignableFrom(Double.class))
										{
											arg = Double.valueOf(params[j].toString()).doubleValue();
										}
										else
											if (sourceClass.isAssignableFrom(Boolean.class))
											{
												arg = ((Boolean)params[j]).booleanValue()?1.0:0.0;
											}
											else
											{ // Si el tipo de source no es compatible, el Method no me sirve...
												break nextMethod;
											}
								}

								// Ahora solo debo castear 'arg' al tipo de datos primitivos que recibe la funci�n
								if(destIsArray)
								{ // Si soy array, meto en el �ndice correcto el arg casteado al tipo esperado
									switch(getPrimitiveType(destClass))
									{
									case TYPE_BYTE: ((byte[])callingParams[j])[arrayIndex] = (byte)arg; break;
									case TYPE_CHARACTER: ((char[])callingParams[j])[arrayIndex] = (char)arg; break;
									case TYPE_SHORT: ((short[])callingParams[j])[arrayIndex] = (short)arg; break;
									case TYPE_INTEGER: ((int[])callingParams[j])[arrayIndex] = (int)arg; break;
									case TYPE_LONG: ((long[])callingParams[j])[arrayIndex] = (long)arg; break;
									case TYPE_FLOAT: ((float[])callingParams[j])[arrayIndex] = (float)arg; break;
									case TYPE_DOUBLE: ((double[])callingParams[j])[arrayIndex] = (double)arg; break;
									case TYPE_BIG_DECIMAL:
													  if(sourceClass.isAssignableFrom(BigDecimal.class))((BigDecimal[])callingParams[j])[arrayIndex] = bigDecimalArg;
													  else ((BigDecimal[])callingParams[j])[arrayIndex] = new BigDecimal(arg);
													  break;
									}
								}
								else
								{ // En el caso en que NO sea array, debo encapsularlo
									switch(getPrimitiveType(destClass))
									{
									case TYPE_BYTE: callingParams[j] = new Byte((byte)arg); break;
									case TYPE_CHARACTER: callingParams[j] = new Character((char)arg); break;
									case TYPE_SHORT: callingParams[j] = new Short((short)arg); break;
									case TYPE_INTEGER: callingParams[j] = new Integer((int)arg); break;
									case TYPE_LONG: callingParams[j] = new Long((long)arg); break;
									case TYPE_FLOAT: callingParams[j] = new Float((float)arg); break;
									case TYPE_DOUBLE: callingParams[j] = new Double((double)arg); break;
									case TYPE_BOOLEAN: callingParams[j] = new Boolean(arg!=0.0); break;
									case TYPE_BIG_DECIMAL:
															if(sourceClass.isAssignableFrom(BigDecimal.class))callingParams[j] = bigDecimalArg;
															else callingParams[j] = new BigDecimal(arg);
															break;
									}
								}
							}
							else
							{ // SI el tipo de destino NO es primitivo, y sourceClass y destClass no matcheaban
							  // entonces NO se puede realizar ninguna conversion, y este Method no sirve
								break nextMethod;
							}
					  }
					}


					if(sourceClass.isArray())
					{ // Por ahora siempre son arrays, pero en el futuro podr�a cambiar ?

					}
				}
			}
			methodToExecute = method;
			break;
		}

		if(methodToExecute == null)
		{// Si entro aqui es porque NO encontr� un m�todo compatible con los datos de source
			if (sourceClass == null || destClass == null)
				//Application.printWarning("NoSuchMethodException Can't execute dynamic call " + className, new NoSuchMethodException("method not found"));
				throw new RuntimeException("NoSuchMethodException Can't execute dynamic call " + className);
			else
				//Application.printWarning("InvalidArguments: Can't execute dynamic call " + className + " - source argument(" + sourceClass.toString() + ") not compatible with expected function argument (" + destClass.toString() + ")", new IllegalArgumentException("source argument(" + sourceClass.toString() + ") not compatible with expected function argument (" + destClass.toString() + ")"));
				throw new RuntimeException("InvalidArguments: Can't execute dynamic call " + className + " - source argument(" + sourceClass.toString() + ") not compatible with expected function argument (" + destClass.toString() + ")");
			//return false;
		}

		try
		{
			// OK, ahora ejecuto el m�todo
			methodToExecute.invoke(myClass.getConstructor(constructorClass).newInstance(new Object[] {new Integer(handle), context}), callingParams);

			// Luego de ejecutar el m�todo tengo que copiar los items de cada argumento de los callingParams
			// al de params. Esto es para emular el hecho de que pueden haber sido modificados en la llamada
			// Adem�s esto s�lo hay que hacerlo para los argumentos que tenian destIsArray
			// Los parametros a actualizar son los que en destino eran arrays
			// Nuevamente aqui es posible que haya que hacer conversiones/compatibilizaciones
			updateParamsAfterInvoke(params, callingParams, needToUpdateParams);
		}
		catch (IllegalAccessException e)
		{
			//Application.printWarning("IllegalAccessException Can't execute dynamic call " + className + " - " + e.getMessage(), e);
			//return false;
			throw new RuntimeException("IllegalAccessException Can't execute dynamic call " + className + " - " + e.getMessage());
		}
		catch (NoSuchMethodException e)
		{
			//Application.printWarning("NoSuchMethodException Can't execute dynamic call " + className + " - " + e.getMessage(), e);
			//return false;
			throw new RuntimeException("NoSuchMethodException Can't execute dynamic call " + className + " - " + e.getMessage());
		}
		catch (java.lang.reflect.InvocationTargetException e)
		{
			//Application.printWarning("java.lang.reflect.InvocationTargetException Can't execute dynamic call " + className + " - " + e.getTargetException().getMessage(), e);
			//return false;
			throw new RuntimeException("java.lang.reflect.InvocationTargetException Can't execute dynamic call " + className + " - " + e.getTargetException().getMessage());
		}
		catch (InstantiationException e)
		{
			//Application.printWarning("InstantiationException Can't execute dynamic call " + className + e.getMessage(), e);
			//return false;
			throw new RuntimeException("InstantiationException Can't execute dynamic call " + className + e.getMessage());
		}
		return true;
	}

	private static void updateParamsAfterInvoke(Object [] params, Object [] callingParams, boolean [] needToUpdateParams)
	{
		for(int j = 0; j < needToUpdateParams.length; j++)
		{
			if(needToUpdateParams[j])
			{
				Class paramClass = params[j].getClass();
				Class<?> calledClass = callingParams[j].getClass().getComponentType(); // pues ya se que el destIsArray
				boolean sourceIsArray = paramClass.isArray();
				int sourceArraySize = 1;
				if(sourceIsArray)
				{
					sourceArraySize = Array.getLength(params[j]);
					paramClass = paramClass.getComponentType();

				}

				for(int arrayIndex = 0; arrayIndex < sourceArraySize; arrayIndex++)
				{
					if(calledClass.equals(paramClass))
					{ // Si los tipos de datos son los mismos
						if(sourceIsArray)Array.set(params[j], arrayIndex, Array.get(callingParams[j], arrayIndex));
						else params[j] = Array.get(callingParams[j], arrayIndex);
					}
					else
					{ // Sino tengo que volver para atras la compatibilizaci�n que hab�a hecho
						double arg = -1;

						if(calledClass.isPrimitive())
						{
							arg = Array.getDouble(callingParams[j], arrayIndex);
						}
						else
						{
							if(calledClass.isAssignableFrom(BigDecimal.class))
							{
								arg = (((BigDecimal[])callingParams[j])[arrayIndex]).doubleValue();
							}
							else if(calledClass.isAssignableFrom(Byte.class) || calledClass.isAssignableFrom(Character.class) ||
								   calledClass.isAssignableFrom(Short.class) || calledClass.isAssignableFrom(Integer.class) ||
								   calledClass.isAssignableFrom(Long.class) || calledClass.isAssignableFrom(Float.class) || calledClass.isAssignableFrom(Double.class))
								{
									arg = Double.valueOf(Array.get(callingParams[j], arrayIndex).toString()).doubleValue();
							} else System.err.println("updateParamsAfterInvoke error: " + calledClass.toString() + " - " + paramClass.toString());
						}

						// Ok, ahora que tengo el valor, lo debo poner en el resultado...
						if(sourceIsArray)
						{ // Si soy array, meto en el �ndice correcto el arg casteado al tipo esperado
							switch(getPrimitiveType(paramClass))
							{
							case TYPE_BYTE: ((byte[])params[j])[arrayIndex] = (byte)arg; break;
							case TYPE_CHARACTER: ((char[])params[j])[arrayIndex] = (char)arg; break;
							case TYPE_SHORT: ((short[])params[j])[arrayIndex] = (short)arg; break;
							case TYPE_INTEGER: ((int[])params[j])[arrayIndex] = (int)arg; break;
							case TYPE_LONG: ((long[])params[j])[arrayIndex] = (long)arg; break;
							case TYPE_FLOAT: ((float[])params[j])[arrayIndex] = (float)arg; break;
							case TYPE_DOUBLE: ((double[])params[j])[arrayIndex] = (double)arg; break;
							case TYPE_BIG_DECIMAL: ((BigDecimal[])params[j])[arrayIndex] = new BigDecimal(arg); break;
							}
						}
						else
						{ // En el caso en que NO sea array, debo encapsularlo
							switch(getPrimitiveType(paramClass))
							{
							case TYPE_BYTE: params[j] = new Byte((byte)arg); break;
							case TYPE_CHARACTER: params[j] = new Character((char)arg); break;
							case TYPE_SHORT: params[j] = new Short((short)arg); break;
							case TYPE_INTEGER: params[j] = new Integer((int)arg); break;
							case TYPE_LONG: params[j] = new Long((long)arg); break;
							case TYPE_FLOAT: params[j] = new Float((float)arg); break;
							case TYPE_DOUBLE: params[j] = new Double((double)arg); break;
							case TYPE_BIG_DECIMAL: params[j] = new BigDecimal(arg);
							}
						}
					}
				}
			}
		}
	}

	protected static final int TYPE_BYTE = 1;
	protected static final int TYPE_CHARACTER = 2;
	protected static final int TYPE_SHORT = 3;
	protected static final int TYPE_INTEGER = 4;
	protected static final int TYPE_LONG = 5;
	protected static final int TYPE_FLOAT = 6;
	protected static final int TYPE_DOUBLE = 7;
	protected static final int TYPE_BOOLEAN = 8;
	protected static final int TYPE_BIG_DECIMAL = 40;
	protected static final int TYPE_NOT_PRIMITIVE = -1;


	// El getPrimitiveType devuelve un codigo para cada tipo de dato primitivo Y para el BigDecimal
	// pues para el tambi�n realizamos casts
	protected static int getPrimitiveType(Class<?> clase)
	{
		if(clase.isAssignableFrom(Byte.class) ||
		   clase.isAssignableFrom(byte.class))
		{
			return TYPE_BYTE;
		}
		else if(clase.isAssignableFrom(Character.class) ||
		   clase.isAssignableFrom(char.class))
		{
			return TYPE_CHARACTER;
		}
		else if(clase.isAssignableFrom(Short.class) ||
		   clase.isAssignableFrom(short.class))
		{
			return TYPE_SHORT;
		}
		else if(clase.isAssignableFrom(Integer.class) ||
		   clase.isAssignableFrom(int.class))
		{
			return TYPE_INTEGER;
		}
		else if(clase.isAssignableFrom(Long.class) ||
		   clase.isAssignableFrom(long.class))
		{
			return TYPE_LONG;
		}
		else if(clase.isAssignableFrom(Float.class) ||
		   clase.isAssignableFrom(float.class))
		{
			return TYPE_FLOAT;
		}
		else if(clase.isAssignableFrom(Double.class) ||
		   clase.isAssignableFrom(double.class))
		{
			return TYPE_DOUBLE;
		}
		else if(clase.isAssignableFrom(BigDecimal.class))
		{
			return TYPE_BIG_DECIMAL;
		}
		else if(clase.isAssignableFrom(Boolean.class) ||
		   clase.isAssignableFrom(boolean.class))
		{
// El cache puede hacer uso de este... asi que no doy mas 'type not supported'
//			System.err.println("Type not supported!");
			return TYPE_BOOLEAN;
		}
		return TYPE_NOT_PRIMITIVE;
	}
}
