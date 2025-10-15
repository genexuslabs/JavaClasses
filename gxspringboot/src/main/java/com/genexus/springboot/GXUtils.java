package com.genexus.springboot;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Set;

public class GXUtils {
	public static final ILogger logger = com.genexus.diagnostics.core.LogManager.getLogger(GXUtils.class);

	public static ToolCallbackProvider operationsTools(String packageName) {
		Connect.init();
		LogManager.initialize(".");

		Reflections reflections = new Reflections(
			new org.reflections.util.ConfigurationBuilder()
				.forPackages(packageName)
				.addScanners(org.reflections.scanners.Scanners.MethodsAnnotated)
		);

		Set<Method> toolMethods = reflections.getMethodsAnnotatedWith(Tool.class);

		MethodToolCallbackProvider.Builder builder = MethodToolCallbackProvider.builder();

		// Keep track of classes that have already been added
		Set<Class<?>> processedClasses = new java.util.HashSet<>();

		for (Method method : toolMethods) {
			Class<?> clazz = method.getDeclaringClass();

			// Skip if we've already processed this class
			if (processedClasses.contains(clazz)) {
				continue;
			}

			try {
				Object instance = clazz.getConstructor(int.class).newInstance(-1);
				builder.toolObjects(instance);
				processedClasses.add(clazz);

				Tool toolAnnotation = method.getAnnotation(Tool.class);
				logger.debug(String.format("Registered tool: %s - %s",
					toolAnnotation.name().isEmpty() ? method.getName() : toolAnnotation.name(),
					toolAnnotation.description()));
			} catch (Exception e) {
				logger.error("Error instantiating tool class: " + clazz.getName(), e);
			}
		}

		if (!toolMethods.isEmpty())
			return builder.build();

		return null;
	}
}
