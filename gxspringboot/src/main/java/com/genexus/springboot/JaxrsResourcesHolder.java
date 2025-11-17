package com.genexus.springboot;

import java.util.LinkedHashSet;
import java.util.Set;

public final class JaxrsResourcesHolder {
	private static final Set<Class<?>> RESOURCES = new LinkedHashSet<>();
	private JaxrsResourcesHolder() {}

	public static void setAll(Set<Class<?>> rrcs) {
		RESOURCES.clear();
		if (rrcs != null) RESOURCES.addAll(rrcs);
	}

	public static Set<Class<?>> getAll() {
		return new LinkedHashSet<>(RESOURCES);
	}
}

