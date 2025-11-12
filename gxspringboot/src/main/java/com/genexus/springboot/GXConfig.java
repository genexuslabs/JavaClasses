package com.genexus.springboot;

import com.genexus.Application;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.servlet.CorsFilter;
import com.genexus.xml.GXXMLSerializable;

import jakarta.annotation.PreDestroy;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebMvc
public class GXConfig implements WebMvcConfigurer {
	public static final ILogger logger = LogManager.getLogger(GXConfig.class);
	private static final String REWRITE_FILE = "rewrite.config";

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

			registry.addResourceHandler("/_ng/**")
				.addResourceLocations("classpath:/ng/");

			registry.addResourceHandler("/" + blobPath + "/**")
				.addResourceLocations("file:./" + blobPath + "/");
		}
		catch (ClassNotFoundException e) {
			logger.error("Error setting context folders ", e);
		}
	}

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new CorsFilter());
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}


	@Bean
	public FilterRegistrationBean<UrlRewriteFilter> urlRewriteFilter() {
		FilterRegistrationBean<UrlRewriteFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new UrlRewriteFilter());
		registrationBean.addUrlPatterns("/*");
		if (new ClassPathResource(REWRITE_FILE).exists()) {
			registrationBean.addInitParameter("modRewriteConf", "true");
			registrationBean.addInitParameter("confPath", REWRITE_FILE);
			registrationBean.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
		}
		else {
			registrationBean.setEnabled(false);
		}
		return registrationBean;
	}

	@Bean
	public ServletContextInitializer jerseyFilter() {
		Set<Class<?>> rrcs = JaxrsResourcesHolder.getAll();

		if (rrcs.isEmpty()) {
			return sc -> {};
		}

		ResourceConfig rc = new ResourceConfig();
		rc.registerClasses(rrcs.toArray(new Class<?>[0]));
		rc.property(ServletProperties.FILTER_FORWARD_ON_404, true);

		ServletContainer container = new ServletContainer(rc);

		FilterRegistrationBean<ServletContainer> reg = new FilterRegistrationBean<>(container);
		reg.addUrlPatterns("/rest/*");
		reg.setName("jersey-filter");
		reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);

		Map<String, String> initParams = new HashMap<>();
		initParams.put(ServletProperties.FILTER_CONTEXT_PATH, "/rest");
		reg.setInitParameters(initParams);

		return reg;
	}

	@PreDestroy
	public void onDestroy() {
		GXXMLSerializable.classesCacheMethods.clear();
	}
}
