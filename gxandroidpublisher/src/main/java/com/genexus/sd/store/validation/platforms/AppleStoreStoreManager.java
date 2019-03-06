package com.genexus.sd.store.validation.platforms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.NotImplementedException;

import com.genexus.cryptography.GXHashing;
import com.genexus.sd.store.validation.model.*;
import com.genexus.sd.store.validation.model.exceptions.*;

import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public class AppleStoreStoreManager implements IStoreManager{

	private final String APPLE_STORE_VALIDATION_URL_PROD = "https://buy.itunes.apple.com/verifyReceipt";
	private final String APPLE_STORE_VALIDATION_URL_SANDBOX = "https://sandbox.itunes.apple.com/verifyReceipt";
	private String _iTunesStorePassword;
	
	public AppleStoreStoreManager(String iTunesStorePassword)
	{
		_iTunesStorePassword = iTunesStorePassword;
	}
	
	
	public StorePurchase GetPurchase(String productId, PurchaseResult purchaseResult)
			throws StoreConfigurationException, StoreInvalidPurchaseException, StoreException,
			StorePurchaseNotFoundException, StoreResponsePurchaseException {
		
		StorePurchase p = new StorePurchase();		
		String purchaseToken = purchaseResult.getPurchaseId();
		
		if (purchaseToken.isEmpty())
		{
			throw new StoreInvalidPurchaseException("PurchaseToken not found in Purchase Transaction Data");
		}

		if (_iTunesStorePassword.isEmpty())
		{
			throw new StoreConfigurationException("iTunes Store Password cannot be empty");
		}

		try
		{
			p = validatePurchase(purchaseToken, purchaseResult, PurchaseEnvironment.Production);
		}
		catch (StoreResponseEnvironmentException e)
		{
			try {
				p = validatePurchase(purchaseToken, purchaseResult, PurchaseEnvironment.Sandbox);
			} catch (StoreResponseEnvironmentException e2) {
			} catch (JSONException e1) {
				throw new StoreInvalidPurchaseException(e1.getMessage());
			}
		}  catch (JSONException e) {
			throw new StoreInvalidPurchaseException(e.getMessage());
		}
		return p;
	}

	private HashMap<String, String> dataCache = new HashMap<String, String>();
	private GXHashing hash = new GXHashing();
	
	private StorePurchase validatePurchase(String purchaseToken, PurchaseResult purchaseResult, PurchaseEnvironment env) throws StoreResponsePurchaseException, StoreResponseEnvironmentException, JSONException, StorePurchaseNotFoundException
	{
		StorePurchase p = null;
		
		String responseString = "";
		String url = (env == PurchaseEnvironment.Production) ? APPLE_STORE_VALIDATION_URL_PROD : APPLE_STORE_VALIDATION_URL_SANDBOX;

		JSONObject inputObj = new JSONObject();
		inputObj.put("receipt-data", purchaseResult.getTransactionData().trim());
		inputObj.put("password", _iTunesStorePassword);
		
		String key = hash.compute(inputObj.toString(), "");
		
		if (!dataCache.containsKey(key)) {
			responseString = doPost(inputObj.toString(), url);
			dataCache.put(key, responseString);
		}
		else
		{
			responseString = dataCache.get(key);
		}
		JSONObject jResponse = new JSONObject(responseString);

		if (jResponse.has("status"))
		{
			int statusCode = (int)jResponse.getInt("status");
			switch (statusCode)
			{
				case 21000:
					throw new StoreResponsePurchaseException("The App Store could not read the JSON object you provided.");
				case 21002:
					throw new StoreResponsePurchaseException("The data in the receipt-data property was malformed or missing.");
				case 21003:
					throw new StoreResponsePurchaseException("The receipt could not be authenticated.");
				case 21004:
					throw new StoreResponsePurchaseException("The shared secret you provided does not match the shared secret on file for your account.");
				case 21005:
					throw new StoreResponsePurchaseException("The receipt server is not currently available.");
				case 21006:
					throw new NotImplementedException();
				case 21007:
					dataCache.remove(key);
					throw new StoreResponseEnvironmentException();
				case 21008:
					break;
				case 0:
					break;
				default:
					throw new NotImplementedException();
			}
			boolean found = false;
			if (jResponse.has("receipt"))
			{
				JSONObject receipt = jResponse.getJSONObject("receipt");
				if (receipt.has("in_app"))
				{
					JSONArray purchases = receipt.getJSONArray("in_app");
					for (int i = 0; i < purchases.length(); i++) {
						JSONObject purchase = purchases.getJSONObject(i);
						if (purchase.has("transaction_id") && purchase.getString("transaction_id").equals(purchaseToken))
						{
							found = true;
							p = parsePurchase(purchase);
							
							String ATT_ORIG_TRN_ID = "original_transaction_id";
							if (p.getPurchaseStatus() == PurchaseStatus.EXPIRED && p.getProductType() == (int)ProductType.Subscription && purchase.has(ATT_ORIG_TRN_ID) && jResponse.has("latest_receipt_info"))
							{
								String originalTransactionId = purchase.getString(ATT_ORIG_TRN_ID);
								JSONArray latestInfo = jResponse.getJSONArray("latest_receipt_info");
								List<StorePurchase> list = new ArrayList<StorePurchase>();
								for (int i1 = 0; i1 < latestInfo.length(); i1++) 
								{
									JSONObject latestPurchase = latestInfo.getJSONObject(i1);								
									if (latestPurchase.has(ATT_ORIG_TRN_ID) && latestPurchase.getString(ATT_ORIG_TRN_ID).equals(originalTransactionId))
									{
										p = parsePurchase(latestPurchase);										
										list.add(p);
										if (p.getPurchaseStatus() == PurchaseStatus.VALID)
										{
											break;
										}
									}
								}
								if (p.getPurchaseStatus() != PurchaseStatus.VALID && list.size() > 0)
								{
									Collections.sort(list, new PurchaseResultComparator());
									p = list.get(0);
								}								
							}							
							else
							{
								break;
							}
							
						}
					}
				}

			}
			if (!found)
			{
				throw new StorePurchaseNotFoundException("Purchase Id not found inside Apple Receipt");
			}
		}
		else
		{
			throw new StoreResponsePurchaseException("Aplle Store validation servers seems to be unavailable.");
		}	
		if (p != null){
			p.setPurchaseResult(purchaseResult);			
		}
		return p;
	}
	
	
	private StorePurchase parsePurchase(JSONObject purchase)
	{
		StorePurchase p;
		int pType = ProductType.Product;
		if (purchase.has("web_order_line_item_id"))
		{
			pType = ProductType.Subscription;
		}

		p = new StorePurchase();
		p.setProductId(getValueDefault("product_id", purchase, ""));
		p.setPurchaseId(getValueDefault("transaction_id", purchase, ""));
		p.setPurchaseDate(getDateValueFromMS("purchase_date_ms", purchase));
		p.setProductType((int)pType);
		p.setCustomIsTrialPeriod(getValueDefault("is_trial_period", purchase, "false") == "true");
		p.setPurchaseStatus(PurchaseStatus.VALID);	
		if (pType == ProductType.Subscription)
		{
			p.setSubscriptionExpiration(getDateValueFromMS("expires_date_ms", purchase));
			p.setSubscriptionFirstPurchased(getDateValueFromMS("original_purchase_date_ms", purchase));
			
			if (p.getSubscriptionExpiration().before(new Date()))
			{
				p.setPurchaseStatus(PurchaseStatus.EXPIRED);				
			}
		}
		return p;
	}
	
	private Date getDateValueFromMS(String key, JSONObject obj)
	{
		String value = getValueDefault(key, obj, "0");
		return new Date(Long.parseLong(value));
	}
	
	private String getValueDefault(String key, JSONObject obj, String defaultValue)
	{
		String value = "";
		if (obj.has(key))
		{
			try {
				value = obj.getString(key);
			} catch (JSONException e) {				
			}			
		}
		return value;
	}
	
	
	public List<StorePurchase> GetPurchases(PurchasesInformation pInfo) throws StoreConfigurationException,
			StoreInvalidPurchaseException, StoreException, StorePurchaseNotFoundException {
		List<StorePurchase> list = new ArrayList<StorePurchase>();
		for (int i = 0; i< pInfo.getPurchases().size(); i++)
		{				
			PurchaseResult p = pInfo.getPurchases().get(i);
			p.setTransactionData(pInfo.getAppleReceipt());
			try {
				StorePurchase sp = this.GetPurchase(p.getProductIdentifier(), p);
				if (!list.contains(sp))
				{
					list.add(sp);					
				}
			}
			catch(Exception e)
			{
				
			}
		}
		for (Iterator<StorePurchase> it=list.iterator(); it.hasNext();) {
			StorePurchase itProd = it.next();
		    if (itProd.getProductType() == ProductType.Subscription && itProd.getPurchaseStatus() != PurchaseStatus.VALID )
		        it.remove(); 
		}		
		return list;
	}
	
	private static String doPost(String postData, String url)
	{		
		URL obj;
		StringBuffer response = new StringBuffer();
		try {
			obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			//add reuqest header
			con.setRequestMethod("POST");
				
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postData);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
		
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			
		}
			
		return response.toString();
	}
	
	
	/*public List<StorePurchase> GetPurchases(PurchasesInformation pInfo)
	{
		throw new NotImplementedException();
	}*/

}
