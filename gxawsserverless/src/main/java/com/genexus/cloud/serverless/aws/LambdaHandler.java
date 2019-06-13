package com.genexus.cloud.serverless.aws;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.genexus.ApplicationContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.util.IniFile;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    private static ILogger logger = null;
    private static JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler = null;
    private static ResourceConfig jerseyApplication = null;
    private static final String BASE_REST_PATH = "/rest/";

    public LambdaHandler() throws Exception {
        if (LambdaHandler.jerseyApplication == null) {
            logger = com.genexus.specific.java.LogManager.initialize(".", LambdaHandler.class);
            LambdaHandler.jerseyApplication = ResourceConfig.forApplication(initialize());
            if (jerseyApplication.getClasses().size() == 0) {
                String errMsg = "No endpoints found for this application";
                logger.error(errMsg);
                throw new Exception(errMsg);
            }
            LambdaHandler.handler = JerseyLambdaContainerHandler.getAwsProxyHandler(LambdaHandler.jerseyApplication);
        }
    }

    public AwsProxyResponse handleRequest(AwsProxyRequest awsProxyRequest, Context context) {
        String path = awsProxyRequest.getPath();
        path = path.replace(BASE_REST_PATH, "/");
        awsProxyRequest.setPath(path);
        return this.handler.proxy(awsProxyRequest, context);
    }

    public static Application initialize() throws Exception {
        IniFile config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", null);
        String className = config.getProperty("Client", "PACKAGE", null);
        Class<?> cls;
        try {
            cls = Class.forName(className + ".GXApplication");
            Application app = (Application) cls.newInstance();
            ApplicationContext appContext = ApplicationContext.getInstance();
            appContext.setServletEngine(true);
            appContext.setServletEngineDefaultPath("");
            com.genexus.Application.init(cls);
            return app;
        } catch (Exception e) {
            logger.error("Failed to initialize App", e);
            throw e;
        }
    }
}