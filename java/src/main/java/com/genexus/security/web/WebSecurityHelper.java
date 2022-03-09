package com.genexus.security.web;

import java.util.Date;

import com.genexus.ModelContext;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.genexus.security.web.SecureTokenHelper.SecurityMode;
import com.genexus.webpanels.WebUtils;

public class WebSecurityHelper {

	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(WebSecurityHelper.class);
	
     public static String StripInvalidChars(String input)
     {
		if (input == null)
			return input;			 
        String output = input.replaceAll("[\u0000-\u001f]", "");
        return StringUtils.strip(output);
     }
	
	 public static String sign(String pgmName, String issuer, String value, SecurityMode mode, String key)
     {            
		 WebSecureToken token = new WebSecureToken(pgmName, issuer, StripInvalidChars(value));
         return SecureTokenHelper.sign(token, mode, key);
     }

     public static boolean verify(String pgmName, String issuer, String value, String jwtToken, String key)
     {
    	 WebSecureToken token = new WebSecureToken();
    	 if(!SecureTokenHelper.verify(jwtToken, token, key))
    		 return false;            
    	 boolean ret = !StringUtils.isBlank(pgmName) && token.get_pgmName().equals(pgmName) && issuer.equals(token.get_issuer()) &&
    			 StripInvalidChars(value).equals(StripInvalidChars(token.get_value())) && new Date().compareTo(token.get_expiration()) < 0;
		 if (!ret && log.isDebugEnabled()) {
			 String lsep = System.getProperty("line.separator");
			 
			 StringBuilder stringBuilder = new StringBuilder();
			 stringBuilder.append("Verify: Invalid token" + lsep);
			
			if (!(token.get_pgmName().equals(pgmName))) {
				stringBuilder.append(String.format("Verify: pgmName mismatch '%s' <> '%s' %s", token.get_pgmName(), pgmName, lsep));				
			}
			if (!(token.get_issuer().equals(issuer))) {
				stringBuilder.append(String.format("Verify: issuer mismatch '%s' <> '%s' %s", token.get_issuer(), issuer, lsep));				
			}
			if (!StripInvalidChars(value).equals(StripInvalidChars(token.get_value()))) {
				stringBuilder.append(String.format("Verify: value mismatch '%s' <> '%s' %s", token.get_value(), value, lsep));				
			}
			if (!(new Date().compareTo(token.get_expiration()) < 0)) {
				stringBuilder.append("Verify: token expired ");
			}
			log.error(stringBuilder.toString());
		 }
		 return ret;
     }

}
