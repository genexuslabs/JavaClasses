package com.genexus.sd.store.validation.platforms;

import java.util.List;

import com.genexus.sd.store.validation.model.PurchaseResult;
import com.genexus.sd.store.validation.model.PurchasesInformation;
import com.genexus.sd.store.validation.model.StorePurchase;
import com.genexus.sd.store.validation.model.exceptions.*;

public interface IStoreManager
{
	StorePurchase GetPurchase(String productId, PurchaseResult purchaseResult) throws StoreConfigurationException, StoreInvalidPurchaseException, StoreException, StorePurchaseNotFoundException, StoreResponsePurchaseException;

	List<StorePurchase> GetPurchases(PurchasesInformation pInfo) throws StoreConfigurationException, StoreInvalidPurchaseException, StoreException, StorePurchaseNotFoundException, Exception;
}
