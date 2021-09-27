package com.genexus.filters;

import json.org.json.*;
import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import com.genexus.servlet.*;
import com.genexus.servlet.http.IHttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class APIObjectFilter extends Filter {
    
    private ArrayList<String> appPath = new ArrayList<String>();
    
    public static final Logger logger = LogManager.getLogger(APIObjectFilter.class);

    public void doFilter(IServletRequest request, IServletResponse response, IFilterChain chain) throws Exception {
        if (request.isHttpServletRequest() && response.isHttpServletResponse()) {
            IHttpServletRequest httpRequest = request.getHttpServletRequest();
            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length()).substring(1);
            String urlString = path.toLowerCase();
            boolean isPath = false;
            for(String appBasePath : appPath)
            {                
                if (urlString.startsWith(appBasePath))   
                    isPath = true;                
            }     
            if(isPath)
            {
                String fwdURI = "/rest/" + path;
				httpRequest.getRequestDispatcher(fwdURI).forward(request,response);
            }
            else
            {
                chain.doFilter(request, response);
            }
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void init(Map<String, String> headers, String path, String sessionCookieName) throws ServletException {        
        try {
            String paramValue = headers.get("BasePath");
            if (paramValue != null && !paramValue.isEmpty())
            {                
                if (paramValue.equals("*"))
                {                    
                    if (path != null &&  !path.isEmpty())
                    {                        
                        paramValue  = path + "private";                        
                        Path privateFolder = Paths.get(paramValue);
                        if (!Files.exists(privateFolder)){
                            paramValue  = path + "WEB-INF" + File.separator + "private";
                        }
                    }
                }
                logger.info("API metadata path: " +  paramValue) ; 
                Stream<Path> walk = Files.walk(Paths.get(paramValue + File.separator)); 
                List<String> result = walk.map(x -> x.toString()).filter(f -> f.endsWith(".grp.json")).collect(Collectors.toList());
                for (String temp : result)
                {
                    try{
                        String read = String.join( "", Files.readAllLines(Paths.get(temp)));
                        JSONObject jo = new JSONObject(read);
                        String apipath = jo.getString("BasePath");
                        appPath.add(apipath.toLowerCase());
                    }
                    catch(IOException e)
                    {
                        logger.error("Exception in API Filter: ", e);  
                    }
                }        
            }
            else
            {
                logger.info("API base path invalid."); 
            }
        } 
        catch (Exception e) {
            logger.error("Exception in API Filter: ", e);            	        
        }
    }

    public void destroy() {
    }
}