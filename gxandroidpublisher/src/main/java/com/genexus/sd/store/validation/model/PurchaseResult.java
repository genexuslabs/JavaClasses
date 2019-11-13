package com.genexus.sd.store.validation.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.genexus.xml.GXXMLSerializable;

public class PurchaseResult {

	private String purchaseId;
	private String productIdentifier;
	private int platform;
	private String transactionData;

	public PurchaseResult(String purchaseId, String productId, int platform, String trnData)
	{
		this.purchaseId = purchaseId;
		this.productIdentifier = productId;
		this.platform = platform;
		this.transactionData = trnData;
	}


	public JSONObject ToJsonObject() {
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("purchaseId", purchaseId);
			jObj.put("ProductIdentifier", productIdentifier);			
			jObj.put("PurchasePlatform", platform);
			jObj.put("TransactionData", transactionData);			
		} catch (JSONException e) {			
			e.printStackTrace();
		}			
		return jObj;
	}
	
	public PurchaseResult(JSONObject jObj)
	{
		fromJSON(jObj);
	}
	
	public PurchaseResult(GXXMLSerializable data)
	{
		String json = data.toJSonString(false);
		JSONObject jObj = null;
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fromJSON(jObj);
	}
	
	private void fromJSON(JSONObject jObj){
		
		if (jObj != null){
			try {
				if (jObj.has("PurchaseId")){				
					this.purchaseId = jObj.getString("PurchaseId");				
				}
				if (jObj.has("PurchasePlatform")){				
					this.platform = jObj.getInt("PurchasePlatform");				
				}
				if (jObj.has("TransactionData")){				
					this.transactionData = jObj.getString("TransactionData");				
				}
				if (jObj.has("ProductIdentifier")){				
					this.productIdentifier = jObj.getString("ProductIdentifier");				
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getPurchaseId() {
		return purchaseId.trim();
	}
	public String getProductIdentifier() {
		return productIdentifier.trim();
	}
	public void setProductIdentifier(String productIdentifier) {
		this.productIdentifier = productIdentifier;
	}
	public int getPlatform() {
		return platform;
	}
	public void setPlatform(int platform) {
		this.platform = platform;
	}
	public String getTransactionData() {
		return transactionData;
	}

	public void setTransactionData(String trnData) {
		transactionData = trnData;
	}


}
