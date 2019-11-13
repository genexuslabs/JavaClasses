package com.genexus.sd.store.validation.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class StorePurchase {
	private String purchaseId = "";
	private String productId = "";
	private Date purchaseDate;
	private int productType;
	private int purchaseStatus = PurchaseStatus.INVALID;
	private int acknowledgementState;

	private Date subscriptionExpiration;
	private Date subscriptionFirstPurchased;
	
	private boolean customConsumed;
	private int customQuantity;
	private boolean customWillRenew;
	private int customCancelReason;
	private boolean customIsTrialPeriod;
	
	private PurchaseResult purchaseResult;
	
	public StorePurchase(){
		customQuantity = 1;
	}
	
	public String ToJson() {
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("purchaseId", purchaseId);
			jObj.put("productId", productId);
			if (purchaseDate != null)
				jObj.put("purchaseDate", serializeDate(purchaseDate));
			jObj.put("productType", productType);
			jObj.put("status", purchaseStatus);

			if (productType == ProductType.Subscription){
				JSONObject subscription = new JSONObject();
				if (subscriptionExpiration != null)
					subscription.put("expiration", serializeDate(subscriptionExpiration));
				if (subscriptionFirstPurchased != null)
					subscription.put("purchaseDate", serializeDate(subscriptionFirstPurchased));
				jObj.put("isTrial", customIsTrialPeriod);
				jObj.put("subscription", subscription);				
			}
			JSONObject custom = new JSONObject();					
			custom.put("consumed", customConsumed);
			custom.put("qty", customQuantity);
			custom.put("willRenew", customWillRenew);
			custom.put("cancelReason", customCancelReason);
			custom.put("acknowledgementState", acknowledgementState);
			if (purchaseResult != null){
				JSONObject prObj = purchaseResult.ToJsonObject();
				custom.put("originalPurchase", prObj);
			}
			jObj.put("advanced", custom);
		} catch (JSONException e) {			
			e.printStackTrace();
		}		
		return jObj.toString();
	}

	private String serializeDate(Date date){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return df.format(date);
	}
	
	public void setPurchaseId(String purchaseId) {
		this.purchaseId = purchaseId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public void setProductType(int productType) {
		this.productType = productType;
	}

	public void setPurchaseStatus(int purchaseStatus) {
		this.purchaseStatus = purchaseStatus;
	}

	public void setSubscriptionExpiration(Date subscriptionExpiration) {
		this.subscriptionExpiration = subscriptionExpiration;
	}

	public void setSubscriptionFirstPurchased(Date subscriptionFirstPurchased) {
		this.subscriptionFirstPurchased = subscriptionFirstPurchased;
	}

	public void setAcknowledgementState(int ackState) {
		this.acknowledgementState = ackState;
	}

	public void setCustomConsumed(boolean customConsumed) {
		this.customConsumed = customConsumed;
	}

	public void setCustomQuantity(int customQuantity) {
		this.customQuantity = customQuantity;
	}

	public void setCustomWillRenew(boolean customWillRenew) {
		this.customWillRenew = customWillRenew;
	}

	public void setCustomCancelReason(int customCancelReason) {
		this.customCancelReason = customCancelReason;
	}

	public void setCustomIsTrialPeriod(boolean customIsTrialPeriod) {
		this.customIsTrialPeriod = customIsTrialPeriod;
	}

	public String getPurchaseId() {
		return purchaseId;
	}

	public String getProductId() {
		return productId;
	}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public int getProductType() {
		return productType;
	}

	public int getPurchaseStatus() {
		return purchaseStatus;
	}

	public Date getSubscriptionExpiration() {
		return subscriptionExpiration;
	}

	public Date getSubscriptionFirstPurchased() {
		return subscriptionFirstPurchased;
	}

	public boolean isCustomConsumed() {
		return customConsumed;
	}

	public int getCustomQuantity() {
		return customQuantity;
	}

	public boolean isCustomWillRenew() {
		return customWillRenew;
	}

	public int getCustomCancelReason() {
		return customCancelReason;
	}

	public boolean isCustomIsTrialPeriod() {
		return customIsTrialPeriod;
	}

	public PurchaseResult getPurchaseResult() {
		return purchaseResult;
	}

	public void setPurchaseResult(PurchaseResult purchaseResult) {
		this.purchaseResult = purchaseResult;
	}

}
