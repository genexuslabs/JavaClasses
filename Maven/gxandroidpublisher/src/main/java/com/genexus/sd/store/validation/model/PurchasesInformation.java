package com.genexus.sd.store.validation.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.genexus.xml.GXXMLSerializable;

public class PurchasesInformation {

	private String appleReceipt;	
	private int purchasePlatform;
	private List<PurchaseResult> purchases = new ArrayList<PurchaseResult>();
	
	
	public String getAppleReceipt() {
		return appleReceipt;
	}
	public void setAppleReceipt(String appleReceipt) {
		this.appleReceipt = appleReceipt;
	}
	public int getPurchasePlatform() {
		return purchasePlatform;
	}
	public void setPurchasePlatform(int purchasePlatform) {
		this.purchasePlatform = purchasePlatform;
	}
	public List<PurchaseResult> getPurchases() {
		return purchases;
	}
	public void setPurchases(List<PurchaseResult> purchases) {
		this.purchases = purchases;
	}
	
	
	public PurchasesInformation(GXXMLSerializable data)
	{
		String json = data.toJSonString(false);
		JSONObject jObj = null;
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (jObj != null){
			try {
				if (jObj.has("Receipt")){				
					this.appleReceipt = jObj.getString("Receipt");				
				}
				if (jObj.has("PurchasePlatform")){				
					this.purchasePlatform = jObj.getInt("PurchasePlatform");				
				}
				if (jObj.has("Purchases")){									
					org.json.JSONArray purchases = jObj.getJSONArray("Purchases");
					for (int i = 0; i < purchases.length(); i++) {
						PurchaseResult p = new PurchaseResult(purchases.getJSONObject(i));
						this.purchases.add(p);					
					}					
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
