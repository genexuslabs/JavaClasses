package com.genexus.security.web;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.security.web.jose.jwt.JWTSigner;
import com.genexus.security.web.jose.jwt.JWTVerifier;

import json.org.json.JSONObject;

public class SecureTokenHelper {
	
	public static final ILogger logger = LogManager.getLogger(SecureTokenHelper.class);
	
    public enum SecurityMode
    {
        Sign,
        SignEncrypt,           
        None 
    }

	@SuppressWarnings("unchecked")
    public static String sign(SecureToken token, SecurityMode mode, String secretKey)
    {        
        String payload = token.ToJavascriptSource();
        String encoded = "";
        JWTSigner signer = new JWTSigner(secretKey);
        Map<String,Object> claims;
		try {
			claims = new ObjectMapper().readValue(payload, Map.class);
			switch (mode)
	        {
	            case Sign:
				try {
					encoded = signer.sign(claims);				
				} catch (Exception e) {
					logger.error("Sign Error", e);
				}
	                break;
	            case SignEncrypt:
	            case None:
	                throw new NotImplementedException();	                	            	            
	        }		
		} 
		catch (Exception e1) {
			logger.error("Sign Error", e1);
		}												      
        return encoded;
    }

	public static boolean verify(String jwtToken, SecureToken outToken, String secretKey)
	{		
		JWTVerifier verifier = new JWTVerifier(secretKey);
		boolean ok = false;            
        if (!StringUtils.isBlank(jwtToken))
		{							
        	try {
				Map<String,Object> mapObj = verifier.verify(jwtToken);
				JSONObject jObj = new JSONObject(mapObj);
				outToken.FromJSONObject(jObj);
				ok = true;			
			}
        	catch (Exception e)
        	{
        		logger.debug(String.format("Web Token Encryption Exception for Token \nTOKEN: '%s'", jwtToken), e);
        	}
		}
		return ok;
	} 
}
