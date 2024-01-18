package com.mockdata;

import com.genexus.ModelContext;
import com.genexus.mock.IGXMock;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestMockDataProvider implements IGXMock {

	public boolean handle(int remoteHandle , ModelContext context, Object gxObject, String[] parametersName) {
		if (gxObject instanceof TestOriginalClass) {
			try {
				Field[] privateFields = new Field[parametersName.length];
				for(int i=0; i < parametersName.length; i++) {
					privateFields[i] = gxObject.getClass().getDeclaredField(parametersName[i]);
					privateFields[i].setAccessible(true);
				}

				Class<?> myClass = Class.forName("com.mockdata.TestMockDataClass");
				Object instance = myClass.getDeclaredConstructor(int.class, ModelContext.class).newInstance(remoteHandle, context);
				for(Method method : myClass.getDeclaredMethods()) {
					if (method.getName().equals("execute")) {
						Object[] parameters = new Object[parametersName.length];
						Class[] parametersTypes = method.getParameterTypes();
						int i = 0;
						for (Class parameterClass : parametersTypes) {
							if (parameterClass.isArray()) {
								parameters[i] = Array.newInstance(privateFields[i].getType(), 1);
								Array.set(parameters[i], 0, privateFields[i].get(gxObject));
							}
							else
								parameters[i] = privateFields[i].get(gxObject);
							i++;
						}
						method.invoke(instance, parameters);
						i = 0;
						for (Class parameterClass : parametersTypes) {
							if (parameterClass.isArray())
								privateFields[i].set(gxObject, Array.get(parameters[i], 0));
							i++;
						}
						break;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
}
