package com.genexus.sd.store.validation.platforms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.genexus.sd.store.validation.model.ProductType;
import com.genexus.sd.store.validation.model.PurchaseResult;
import com.genexus.sd.store.validation.model.PurchaseStatus;
import com.genexus.sd.store.validation.model.PurchasesInformation;
import com.genexus.sd.store.validation.model.StorePurchase;
import com.genexus.sd.store.validation.model.exceptions.StoreConfigurationException;
import com.genexus.sd.store.validation.model.exceptions.StoreException;
import com.genexus.sd.store.validation.model.exceptions.StoreInvalidPurchaseException;
import com.genexus.sd.store.validation.model.exceptions.StorePurchaseNotFoundException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.InAppProduct;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;

import json.org.json.JSONException;
import json.org.json.JSONObject;

public class GooglePlayStoreManager implements IStoreManager {

	private String serviceAccountEmail;	
	private String certificatePath;
	private String certificatePassword;
	
	
	
	public StorePurchase GetPurchase(String productId, PurchaseResult purchaseResult)
			throws StoreConfigurationException, StoreInvalidPurchaseException, StoreException, StorePurchaseNotFoundException {		
		AndroidPublisher google = null;
		JSONObject trnData;
		String packageName = "";
		String token = "";
				
		try {
			trnData = new JSONObject(purchaseResult.getTransactionData());
			packageName = trnData.getString("packageName");
			token = trnData.getString("purchaseToken");
			
		} catch (JSONException e1) {
		}
			
		if (packageName.equals(""))
		{
			throw new StoreInvalidPurchaseException("PackageName not found in Purchase Transaction Data");				
		}

		if (token.equals(""))
		{
			throw new StoreInvalidPurchaseException("PurchaseToken not found in Purchase Transaction Data");				
		}
		
		if (serviceAccountEmail.equals("")){
			throw new StoreConfigurationException("Google Play Service Account Email cannot be empty");
		}
		if (certificatePath.equals("")){
			throw new StoreConfigurationException("Google Play Service Certificate cannot be empty");
		}		
		
		try {
			google = AndroidPublisherHelper.init("GXApp", serviceAccountEmail, certificatePath, certificatePassword);		
		} catch (Exception e) {			
			throw new StoreConfigurationException(e.getMessage());
		}		
		
		StorePurchase p = new StorePurchase();		
		p.setPurchaseResult(purchaseResult);
		try {			
			int prodType = getPurchaseProductType(productId, google, packageName );	
				
			switch(prodType)
			{
				case ProductType.Subscription:					
					SubscriptionPurchase s = google.purchases().subscriptions().get(packageName, productId, token).execute();
					p.setProductId(productId);
					p.setPurchaseId(token);
					p.setProductType(prodType);
					p.setPurchaseDate(new Date(s.getStartTimeMillis()));
					p.setSubscriptionExpiration(new Date(s.getExpiryTimeMillis()));
					p.setSubscriptionFirstPurchased(new Date(s.getStartTimeMillis()));
					p.setCustomConsumed(false);
					p.setCustomWillRenew(s.getAutoRenewing());
					p.setAcknowledgementState(s.getAcknowledgementState());
					Date now = new Date();
					if (p.getSubscriptionExpiration().before(now))
					{
						p.setPurchaseStatus(PurchaseStatus.EXPIRED);
					}
					else {
						if (s.getCancelReason() != null)
						{
							int cancelReason = s.getCancelReason();
							if (cancelReason == 0) {
								p.setPurchaseStatus(PurchaseStatus.VALID);
							}
							if (cancelReason == 1) {
								p.setPurchaseStatus(PurchaseStatus.CANCELLED);
							}							
						}
						else {
							p.setPurchaseStatus(PurchaseStatus.VALID);
						}
					}
					break;
				case ProductType.Product:					
					ProductPurchase prod = google.purchases().products().get(packageName, productId, token).execute();									
					p.setProductId(productId);
					p.setPurchaseId(token);
					p.setProductType(prodType);					
					p.setPurchaseDate(new Date(prod.getPurchaseTimeMillis()));					
					p.setCustomConsumed(prod.getConsumptionState() == 1);
					p.setAcknowledgementState(prod.getAcknowledgementState());

					if (prod.getPurchaseState() == 1)
					{
						p.setPurchaseStatus(PurchaseStatus.CANCELLED);
					}
					else
					{
						if (prod.getConsumptionState() == 1) //Consumed..
						{
							p.setPurchaseStatus(PurchaseStatus.EXPIRED);
						}
						else {
							p.setPurchaseStatus(PurchaseStatus.VALID);
						}
					}
					break;
					default:
						throw new StoreException("Google product type could not be queried");
			}
			
			
		}		
		catch (GoogleJsonResponseException e)		
		{
			switch (e.getStatusCode())
			{
				case 404:
					throw new StorePurchaseNotFoundException("Google Play Purchase Token was not found");
				case 400:
					throw new StorePurchaseNotFoundException("Google Play Purchase is invalid: " + e.getMessage() );
				default:
					throw new StoreException(e.getMessage(), e);					
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new StoreException(e.getMessage(), e);
		}
				
		return p;
	}
	
	private static Map<String, Integer> productTypesCache = new HashMap<String, Integer>();
	  
	public int getPurchaseProductType(String productId, AndroidPublisher google, String packageName) throws StoreInvalidPurchaseException {
		int productType = 0;
		String key = packageName + "_" + productId;
		if (!productTypesCache.containsKey(key))
		{
			InAppProduct inApp = null;
			try {				
				inApp = google.inappproducts().get(packageName, productId).execute();
				if (inApp.getPurchaseType().equals("managedUser"))
				{
					productType = ProductType.Product;
				}else{
					if (inApp.getPurchaseType().equals("subscription"))
					{
						productType = ProductType.Subscription;
					}				
				}
				productTypesCache.put(key, productType);
			} catch (IOException e) {
				throw new StoreInvalidPurchaseException("Google Play did not find productId:" + productId + " - " + packageName + " - " + e.getMessage());
			}			
		}
		else
		{
			productType = productTypesCache.get(key);
		}

		return productType;
	}



	public List<StorePurchase> GetPurchases(PurchasesInformation pInfo) throws Exception {
		int failedTrns = 0;
		List<StorePurchase> purchases = new ArrayList<StorePurchase>();
		Exception lastExcp = null;
		for (int i = 0 ; i< pInfo.getPurchases().size(); i++)
		{
			PurchaseResult p = pInfo.getPurchases().get(i);
			try
			{
				StorePurchase sP = GetPurchase(p.getProductIdentifier(), p);
				purchases.add(sP);
			}
			catch(Exception e)
			{
				failedTrns++;
				lastExcp = e;
			}
		}		
		if (lastExcp != null && failedTrns == pInfo.getPurchases().size())
		{
			throw lastExcp;
		}
		return purchases;
	}

	public void setServiceAccountEmail(String serviceAccountEmail) {
		this.serviceAccountEmail = serviceAccountEmail;
	}

	public void setCertificatePath(String certificatePath) {
		this.certificatePath = certificatePath;
	}

	public void setCertificatePassword(String certificatePassword) {
		this.certificatePassword = certificatePassword;
	}
	
}
