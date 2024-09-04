package com.genexus.cloud.serverless;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.GXProcedure;
import com.genexus.GxUserType;
import com.genexus.ModelContext;
import com.genexus.cloud.serverless.model.EventMessageResponse;
import com.genexus.cloud.serverless.model.EventMessages;
import com.genexus.db.DynamicExecute;
import org.apache.commons.lang.NotImplementedException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class GXProcedureExecutor {
	protected Class<GXProcedure> entryPointClass;

	private Class<?>[][] supportedMethodSignatures = new Class<?>[5][];
	private int methodSignatureIdx = -1;

	protected static final String MESSAGE_COLLECTION_INPUT_CLASS_NAME = "com.genexus.genexusserverlessapi.SdtEventMessages";
	protected static final String MESSAGE_OUTPUT_COLLECTION_CLASS_NAME = "com.genexus.genexusserverlessapi.SdtEventMessageResponse";

	public GXProcedureExecutor(Class entryPointClassParms) throws ClassNotFoundException, NotImplementedException {
		entryPointClass = entryPointClassParms;
		supportedMethodSignatures[0] = new Class<?>[]{Class.forName(MESSAGE_COLLECTION_INPUT_CLASS_NAME), Class.forName(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME)};
		supportedMethodSignatures[1] = new Class<?>[]{String.class, Class.forName(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME)};
		supportedMethodSignatures[2] = new Class<?>[]{String.class};
		supportedMethodSignatures[3] = new Class<?>[]{Class.forName(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME)};
		supportedMethodSignatures[4] = new Class<?>[]{}; //No inputs, no outputs

		Optional<Method> executeMethodOpt = Arrays.stream(this.entryPointClass.getDeclaredMethods()).filter(m -> m.getName() == DynamicExecute.METHOD_EXECUTE).findFirst();

		if (!executeMethodOpt.isPresent()) {
			throw new NotImplementedException(String.format("EXECUTE Method not implemented on Class '%s'", entryPointClass.getName()));
		}

		Method executeMethod = executeMethodOpt.get();
		Class<?>[] parametersTypes = executeMethod.getParameterTypes();

		for (int i = 0; i < supportedMethodSignatures.length && methodSignatureIdx < 0; i++) {
			if (supportedMethodSignatures[i].length != parametersTypes.length) {
				continue;
			}
			Class<?>[] listParameters = (Class<?>[]) supportedMethodSignatures[i];
			boolean isMatch = true;
			for (int j = 0; j < listParameters.length && isMatch; j++) {
				isMatch = listParameters[j] == parametersTypes[j] || listParameters[j] == parametersTypes[j].getComponentType();
			}
			if (isMatch) {
				methodSignatureIdx = i;
			}
		}
		if (methodSignatureIdx < 0) {
			throw new NotImplementedException("Expected signature method did not match");
		}

	}

	public EventMessageResponse execute(ModelContext modelContext, EventMessages msgs, String rawJsonEvent) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {
		EventMessageResponse response = new EventMessageResponse();

		Object[] parameters;
		boolean returnsValue = true;
		switch (methodSignatureIdx) {
			case 0:
				Class<?> inputClass = Class.forName(MESSAGE_COLLECTION_INPUT_CLASS_NAME);
				Object msgsInput = inputClass.getConstructor().newInstance();
				if (GxUserType.class.isAssignableFrom(inputClass)) {
					((GxUserType) msgsInput).fromJSonString(Helper.toJSONString(msgs));
				}
				parameters = new Object[]{msgsInput, new Object[]{}};
				break;
			case 1:
				parameters = new Object[]{rawJsonEvent, new Object[]{}};
				break;
			case 2:
				parameters = new Object[]{rawJsonEvent};
				returnsValue = false;
				break;
			case 3:
				parameters = new Object[]{new Object[]{}};
				break;
			default:
				parameters = new Object[]{};
				returnsValue = false;
				break;
		}

		Object[] paramOutArray = null;
		if (returnsValue) {
			parameters[parameters.length - 1] = (Object[]) Array.newInstance(Class.forName(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME), 1);
			paramOutArray = (Object[]) parameters[parameters.length - 1];
			paramOutArray[0] = Class.forName(MESSAGE_OUTPUT_COLLECTION_CLASS_NAME).getConstructor(int.class, ModelContext.class).newInstance(-1, modelContext);
		}

		com.genexus.db.DynamicExecute.dynamicExecute(modelContext, -1, entryPointClass, "", entryPointClass.getName(), parameters);

		if (paramOutArray != null) {
			GxUserType handlerOutput = (GxUserType) paramOutArray[0];
			String jsonResponse = handlerOutput.toJSonString(false);
			response = new ObjectMapper().readValue(jsonResponse, EventMessageResponse.class);
		}
		return response;
	}
}
