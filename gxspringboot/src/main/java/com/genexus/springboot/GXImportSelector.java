package com.genexus.springboot;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;
import com.genexus.webpanels.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;

public class GXImportSelector implements ImportSelector {

	public static final ILogger logger = new LogManager().getLogger(GXImportSelector.class);
	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		ArrayList<String> restImports = new ArrayList<>();
		try {
			Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:*.services");
			for (Resource resource : resources) {
				selectImport(restImports, resource.getFilename());
			}
		}
		catch (IOException e){
			logger.error("Error loading External Services classes ", e);
		}

		addWebSocketsImport(restImports);

		return restImports.toArray(new String[0]);
	}

	private void selectImport(ArrayList<String> restImports, String servicesClassesFileName) {
		try {
			InputStream is = new ClassPathResource(servicesClassesFileName).getInputStream();
			if (is != null) {
				WebUtils.AddExternalServicesFile(null, restImports, is);

				is.close();
			}
		}
		catch (Exception e){
			logger.error("Error loading External Services classes ", e);
		}
	}

	private void addWebSocketsImport(ArrayList<String> restImports) {
		try {
			restImports.add(Class.forName("com.genexus.internet.websocket.GXWebSocket").getName());
		}
		catch (ClassNotFoundException e) {
			logger.info("WebSocket class not found");
		}
	}
}