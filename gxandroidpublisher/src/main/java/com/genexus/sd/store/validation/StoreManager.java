package com.genexus.sd.store.validation;

//import org.apache.commons.lang.NotImplementedException;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.genexus.GXBaseCollection;
import com.genexus.sd.store.validation.model.PurchaseResult;
import com.genexus.sd.store.validation.model.PurchasesInformation;
import com.genexus.sd.store.validation.model.StorePurchase;
import com.genexus.sd.store.validation.model.exceptions.*;
import com.genexus.sd.store.validation.platforms.AppleStoreStoreManager;
import com.genexus.sd.store.validation.platforms.GooglePlayStoreManager;
import com.genexus.sd.store.validation.platforms.IStoreManager;
import com.genexus.xml.GXXMLSerializable;

import json.org.json.JSONException;
import json.org.json.JSONObject;

public class StoreManager {
	
	private int errCode;
	private String errDescription = "";
		
	public int getPurchase(GXXMLSerializable gxStoreConfig, String productId, GXXMLSerializable gxPurchaseResult, GXXMLSerializable gxStorePurchase){		
		IStoreManager storeMgr;
		errDescription = "";
					
		PurchaseResult purchase = new PurchaseResult(gxPurchaseResult);
		storeMgr = getManager(gxStoreConfig, purchase.getPlatform());
		if (storeMgr != null)
		{			
			StorePurchase storePurchase = new StorePurchase();
			
			try {									
				storePurchase = storeMgr.GetPurchase(productId.trim(), purchase);
				gxStorePurchase.fromJSonString(storePurchase.ToJson());
				errCode = 0;
			}
			catch (StoreConfigurationException e)
			{
				errCode = 3;
				errDescription = e.getMessage();
			}
			catch (StoreInvalidPurchaseException e)
			{
				errCode = 2;
				errDescription = e.getMessage();
			}
			catch (StorePurchaseNotFoundException e) {							
				errCode = 11;
				errDescription = e.getMessage();
			}
			catch (StoreException e)
			{
				errCode = 10;
				errDescription = e.getMessage();				
			} catch (StoreResponsePurchaseException e) {
				errCode = 11;
				errDescription = e.getMessage();				
			} 			
		}		
		return errCode;
	}
	
	//public GXBaseCollection<GXXMLSerializable> getPurchases(GXXMLSerializable gxStoreConfig,  GXXMLSerializable gxPurchasesInfo){			
	public String getPurchases(GXXMLSerializable gxStoreConfig,  GXXMLSerializable gxPurchasesInfo){			
		GXBaseCollection<GXXMLSerializable> list = new GXBaseCollection<GXXMLSerializable>();		
		PurchasesInformation pInformation = new PurchasesInformation(gxPurchasesInfo); ;		
		IStoreManager mgr = getManager(gxStoreConfig, pInformation.getPurchasePlatform());		
		if (mgr != null){			
			try {				
				List<StorePurchase> products = mgr.GetPurchases(pInformation);	
				Class c = Class.forName("com.genexuscore.genexus.sd.store.SdtStorePurchase");
				
				for (int i = 0; i< products.size(); i++){
					StorePurchase p1 = products.get(i);
					GXXMLSerializable sP = (GXXMLSerializable) c.getDeclaredConstructor().newInstance();
					sP.fromJSonString(p1.ToJson());					
					list.add(sP);
				}
			} catch (StoreConfigurationException e) {
				errCode = 3;
				errDescription = e.getMessage();
			} catch (StoreInvalidPurchaseException e) {
				errCode = 2;
				errDescription = e.getMessage();
			} catch (StoreException e) {
				errCode = 10;
				errDescription = e.getMessage();
			} catch (StorePurchaseNotFoundException e) {
				errCode = 11;
				errDescription = e.getMessage();
			}
			catch (Exception e)
			{
				errCode = 12;
				errDescription = e.getMessage();
			}
		}				
		return list.toJSonString(false);
	}
	

	private IStoreManager getManager(GXXMLSerializable gxStoreConfig, int platform) {
		JSONObject storeConfig = null;
		try {
			storeConfig = new JSONObject(gxStoreConfig.toJSonString(false));
		} catch (JSONException e) {
			errCode = 3;
			errDescription = "Malformed Store Configuration";
			LogError(e);
			return null;
		}
		
		IStoreManager mgr = null;
		switch (platform)
		{
			case 2:					
				mgr = new AppleStoreStoreManager(GetConfigValue("appleKey", storeConfig));	
				break;
			case 1:
					mgr = new GooglePlayStoreManager();
					((GooglePlayStoreManager)mgr).setCertificatePassword(GetConfigValue("googleCertificatePassword", storeConfig));
					((GooglePlayStoreManager)mgr).setServiceAccountEmail(GetConfigValue("googleServiceAccount", storeConfig));
					((GooglePlayStoreManager)mgr).setCertificatePath(GetConfigValue("googleCertificate", storeConfig));														
				break;
			default:
				throw new NotImplementedException("StoreManager Platform not implemented");
		}
		return mgr;
	}
	
	public static String GetConfigValue(String key, JSONObject storeConfig)
	{
		String value = "";
		if (storeConfig.has(key))
		{
			try {
				value = storeConfig.getString(key);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (value.equals(""))
		{
			LogError( key + " must be specified");
		}

		return value;
	}
	

	private static void LogError(Exception e) {
		LogError(e.getMessage());
	}
	private static void LogError(String error) {
		System.out.println("Store Validation Error - " + error);
	}

	public String getErrDescription() {
		return errDescription;
	}


	public int getErrCode() {
		return errCode;
	}

}
