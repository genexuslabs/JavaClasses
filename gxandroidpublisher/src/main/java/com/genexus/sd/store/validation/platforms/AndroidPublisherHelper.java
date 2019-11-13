/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.genexus.sd.store.validation.platforms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Collections;

import com.genexus.sd.store.validation.model.exceptions.StoreConfigurationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.androidpublisher.AndroidPublisher;

/**
 * Helper class to initialize the publisher APIs client library.
 * <p>
 * Before making any calls to the API through the client library you need to
 * call the {@link AndroidPublisherHelper#init(String)} method. This will run
 * all precondition checks for for client id and secret setup properly in
 * resources/client_secrets.json and authorize this client against the API.
 * </p>
 */
public class AndroidPublisherHelper {

  
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

 
    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName the name of the application: com.example.app
     * @param serviceAccountEmail the Service Account Email (empty if using
     *            installed application)
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws StoreConfigurationException 
     */
    public static AndroidPublisher init(String applicationName,
             String serviceAccountEmail, String certPath, String certPassword) throws IOException, GeneralSecurityException, StoreConfigurationException {
    	
		File f = new File(certPath);
		
		if (!f.isAbsolute()){
			f = new File(getPrivateDirectory(), certPath);
		}
    	
		if (!f.exists()){
			throw new StoreConfigurationException("Google Play Certificate was not found at: " + certPath);
		}
		
    	FileInputStream is = new FileInputStream(f);
    	
    	PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
    			SecurityUtils.getPkcs12KeyStore(), 
    			is, 
    			certPassword, "privatekey", certPassword);
    			
    	
    	if (privateKey == null){    		
    		throw new StoreConfigurationException("Private key could not be loaded: " + certPath);
    	}
    	 
        newTrustedTransport();
        
        GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(serviceAccountEmail)
        .setServiceAccountScopes(
                Collections.singleton("https://www.googleapis.com/auth/androidpublisher"))
        .setServiceAccountPrivateKey(privateKey)
        .build();
       
        
        // Set up and return API client.
        return new AndroidPublisher.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(applicationName)
                .build();
    }

    private static void newTrustedTransport() throws GeneralSecurityException,
            IOException {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
    }
    
    private static String getPrivateDirectory() {
		if(com.genexus.ApplicationContext.getInstance().isServletEngine())
		{
			return com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath() + 
			File.separator + "WEB-INF" + File.separatorChar + "private" + File.separatorChar;
		}
		else
		{
			return "private";
		}
	}

}