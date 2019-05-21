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
        return output.trim();
     }
	
	 public static String sign(String pgmName, String issuer, String value, SecurityMode mode)
     {            
		 WebSecureToken token = new WebSecureToken(pgmName, issuer, StripInvalidChars(value));
         return SecureTokenHelper.sign(token, mode, getSecretKey());
     }

     private static String getSecretKey()
     {    	 
		String hashSalt = com.genexus.Application.getClientContext().getClientPreferences().getREORG_TIME_STAMP(); //Some random SALT that is different in every GX App installation. Better if changes over time
		return WebUtils.getEncryptionKey(ModelContext.getModelContext(), "") + hashSalt;
     }
/*
     public static boolean verify(String pgmName, String issuer, String value, String jwtToken)
     {
         WebSecureToken token = new WebSecureToken();
         return WebSecurityHelper.verify(pgmName, issuer, value, jwtToken, token);
     }
  */
     public static boolean verify(String pgmName, String issuer, String value, String jwtToken)
     {
    	 WebSecureToken token = new WebSecureToken();
    	 if(!SecureTokenHelper.verify(jwtToken, token, getSecretKey()))
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
    		 
     /*public static boolean verify(String pgmName, String issuer, String value, String jwtToken, WebSecureToken token)
     {     	 
         boolean ok = SecureTokenHelper.Verify(jwtToken, token, GetSecretKey());            
         return ok && !string.IsNullOrEmpty(pgmName) && token.ProgramName == pgmName && issuer == token.Issuer &&
         */
}
