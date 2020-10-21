package com.genexus.filters;

import com.genexus.ApplicationContext;
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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class APIObjectFilter implements Filter {
    
    private ArrayList<String> appPath = new ArrayList<String>();

    FilterConfig config; 

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length()).substring(1);
            String urlString = (path.lastIndexOf("/") > -1)? path.substring(0,path.lastIndexOf("/")).toLowerCase():path.toLowerCase();
            boolean isPath = appPath.contains(urlString);
            if(isPath)
            {
                //String originalURI = httpRequest.getRequestURI();
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

    public void init(FilterConfig filterConfig) throws ServletException {        
        try {
            config = filterConfig;            
            String paramValue = config.getInitParameter("BasePath");
            if (paramValue != null && !paramValue.isEmpty())
            {
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
                        System.err.println(e.toString());
                        e.printStackTrace();
                    }
                }        
            }        
        } 
        catch (Exception e) {
            System.err.println(e.toString());
	        e.printStackTrace();
        }
    }

    public void destroy() {
    }
}