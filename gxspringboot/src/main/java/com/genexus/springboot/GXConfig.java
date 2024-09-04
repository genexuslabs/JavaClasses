package com.genexus.springboot;

import com.genexus.Application;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
@EnableWebMvc
public class GXConfig implements WebMvcConfigurer {
	public static final ILogger logger = LogManager.getLogger(GXConfig.class);

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		AntPathMatcher matcher = new AntPathMatcher();
		matcher.setCaseSensitive(false);
		configurer.setPathMatcher(matcher);
	}

	@Value("${server.servlet.context-parameters.gxcfg}")
	private String gxConfig;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		try {
			Application.init(Class.forName(gxConfig));

			String webImageDir = Application.getClientContext().getClientPreferences().getWEB_IMAGE_DIR();
			String blobPath = SpecificImplementation.Application.getDefaultPreferences().getBLOB_PATH().replace("\\", "");

			registry.addResourceHandler(webImageDir + "**")
				.addResourceLocations("classpath:" + webImageDir);

			registry.addResourceHandler("/" + blobPath + "/**")
				.addResourceLocations("file:./" + blobPath + "/");
		}
		catch (ClassNotFoundException e) {
			logger.error("Error setting context folders ", e);
		}
	}
}
