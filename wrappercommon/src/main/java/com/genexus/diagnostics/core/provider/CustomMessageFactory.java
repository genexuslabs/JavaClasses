package com.genexus.diagnostics.core.provider;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverContext;
import org.apache.logging.log4j.layout.template.json.resolver.EventResolverFactory;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverConfig;
import org.apache.logging.log4j.layout.template.json.resolver.TemplateResolverFactory;


@Plugin(name = "CustomMessage", category = TemplateResolverFactory.CATEGORY)
public final class CustomMessageFactory implements EventResolverFactory {
	private static final CustomMessageFactory INSTANCE = new CustomMessageFactory();
	private CustomMessageFactory() { /* no instances */ }

	@PluginFactory
	public static CustomMessageFactory getInstance() {
		return INSTANCE;
	}

	@Override
	public String getName() {
		return CustomMessageResolver.getName();
	}

	@Override
	public CustomMessageResolver create(EventResolverContext context, TemplateResolverConfig config) {
		return new CustomMessageResolver(config);
	}
}
