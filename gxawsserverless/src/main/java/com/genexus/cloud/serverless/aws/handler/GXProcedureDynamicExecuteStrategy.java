package com.genexus.cloud.serverless.aws.handler;

import com.genexus.GXProcedure;
import com.genexus.GxUserType;
import com.genexus.ModelContext;
import com.genexus.db.DynamicExecute;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GXProcedureDynamicExecuteStrategy {
	private int id;
	protected Class<GXProcedure> entryPointClass;
	protected List<Class<GxUserType>> classParameters = new ArrayList<>();
	protected Class<GxUserType> outputClassParameter;
	private Boolean isValid = null;

	public GXProcedureDynamicExecuteStrategy(int id, String procedureClassName) throws ClassNotFoundException {
		this.setId(id);
		entryPointClass = (Class<GXProcedure>) Class.forName(procedureClassName);
	}

	public void addInputParameter(String className) throws ClassNotFoundException {
		classParameters.add((Class<GxUserType>)Class.forName(className));
	}

	public void addOutputParameter(String className) throws ClassNotFoundException {
		this.outputClassParameter = (Class<GxUserType>)Class.forName(className);
	}

	public boolean isValid() {
		if (isValid != null) {
			return isValid;
		}

		List<Class<?>> classMethodParameters = new ArrayList<>();
		classMethodParameters.addAll(classParameters);
		classMethodParameters.add(Array.newInstance(outputClassParameter, 0).getClass());

		Class<?>[] classMethodParametersArray = new Class[classMethodParameters.size()];
		for (int i = 0; i < classMethodParameters.size(); i++) {
			classMethodParametersArray[i] = classMethodParameters.get(i);
		}

		try {
			this.entryPointClass.getMethod(DynamicExecute.METHOD_EXECUTE, classMethodParametersArray);
			isValid = true;
		}
		catch (Exception e) {
			isValid = false;
		}
		return isValid;

	}

	public Object[] execute(ModelContext m, String[] parmsStringSerializedValue) throws Exception {
		int methodParameterSize = classParameters.size() + 1;

		if (parmsStringSerializedValue.length != classParameters.size()) {
			throw new Exception("Signature method does not match");
		}

		Object[] parameters = new Object[methodParameterSize];
		int idx = 0;
		for (String objString : parmsStringSerializedValue) {
			Class<?> classParameter = classParameters.get(idx);
			parameters[idx] = classParameter.getConstructor().newInstance();

			if (GxUserType.class.isAssignableFrom(classParameter)) {
				GxUserType sdt = (GxUserType) parameters[idx];
				sdt.fromJSonString(objString);
			} else if (classParameter == String.class) {
				parameters[idx] = objString;
			} else {
				throw new Exception("Parameter cannot be handled");
			}
			idx++;
		}

		Object [] outputArray = (Object[]) Array.newInstance(outputClassParameter, 1);
		outputArray[0] = outputClassParameter.getConstructor(int.class, ModelContext.class).newInstance(-1 , m);;
		parameters[idx] = outputArray;

		com.genexus.db.DynamicExecute.dynamicExecute(m, -1, entryPointClass, "", entryPointClass.getName(), parameters);
		return outputArray;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
