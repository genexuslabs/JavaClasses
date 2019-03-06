package com.genexus.sd.store.validation.model;

import java.util.Comparator;


public class PurchaseResultComparator implements Comparator<StorePurchase> {
	public int compare(StorePurchase o1, StorePurchase o2) {		 
		return o2.getSubscriptionExpiration().compareTo(o1.getSubscriptionExpiration());
	}
}