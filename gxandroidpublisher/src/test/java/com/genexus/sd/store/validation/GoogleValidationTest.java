package com.genexus.sd.store.validation;

import static org.junit.Assert.*;
import com.genexus.sd.store.validation.model.PurchaseResult;
import com.genexus.sd.store.validation.model.StorePurchase;
import com.genexus.sd.store.validation.model.exceptions.StoreConfigurationException;
import com.genexus.sd.store.validation.model.exceptions.StoreException;
import com.genexus.sd.store.validation.model.exceptions.StoreInvalidPurchaseException;
import com.genexus.sd.store.validation.model.exceptions.StorePurchaseNotFoundException;

import org.json.JSONObject;
import org.junit.*;

import com.genexus.sd.store.validation.platforms.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class GoogleValidationTest {

	@Test
	@Ignore
	public void validateSuscriptionPurchase() {
		GooglePlayStoreManager mgr = init();

		String productId = "ligamx.pro.suscription.yearly.2018.v3";
		String trnData = "{\"orderId\":\"GPA.3328-7048-1570-99168\",\"packageName\":\"com.futbolx.ligamx\",\"productId\":\"ligamx.pro.suscription.yearly.2018.v3\",\"purchaseTime\":1573619557148,\"purchaseState\":0,\"purchaseToken\":\"piflghabihdlfhomgjamcjmj.AO-J1OyG6kTsrwQJcXfqnoQouABzIhZRSDqP9gpsgc7lrucRJghTD5ivAvqPr9M1k5hTMoQHMl1hR2Tkp0mKjr5EAqYPzznHC-3hSLm_UmoaS1JrenOlWIwSeadN7insbz6uhDLd1B-c-v2qqrPlCFqPudKcunVIaA\",\"autoRenewing\":true}";

		PurchaseResult p = new PurchaseResult("", productId, 2, trnData);
		StorePurchase storePurchase = null;

		try {
			storePurchase = mgr.GetPurchase(productId, p);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertSame(1, storePurchase.getPurchaseStatus());
		assertEquals(productId, storePurchase.getProductId());
		assertEquals(1, storePurchase.getCustomQuantity());
		assertEquals(2, storePurchase.getProductType());
	}

	@Test
	@Ignore
	public void validateProductPurchase() {
		GooglePlayStoreManager mgr = init();

		String productId = "ligamx.pro.buy.year";
		String trnData = "{\"orderId\":\"GPA.1341-4038-5007-49835\",\"packageName\":\"com.futbolx.ligamx\",\"productId\":\"ligamx.pro.buy.year\",\"purchaseTime\":1470543065125,\"purchaseState\":0,\"purchaseToken\":\"ohlmeolplkmakkcahopkohoi.AO-J1OyVxMU2Q2zJ48NBS6aqIoThm7hId6XmIipE0lslR6w1HOFIhrVDW3pHL0Ym-CQVZR5RgUx4_X7niorw_jbqFyEzFQ759fV_p1hAVwlSpw75aNBfXPYdrXl8B1YFQvAJSbtDDc9U\"}";


		PurchaseResult p = new PurchaseResult("", productId, 2, trnData);
		StorePurchase storePurchase = null;

		try {
			storePurchase = mgr.GetPurchase(productId, p);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue(new GregorianCalendar(2016, Calendar.AUGUST, 8).getTime().getTime() > storePurchase.getPurchaseDate().getTime());
		assertTrue(new GregorianCalendar(2016, Calendar.AUGUST, 6).getTime().getTime() < storePurchase.getPurchaseDate().getTime());
		assertSame(1, storePurchase.getPurchaseStatus());
		assertEquals(productId, storePurchase.getProductId());
		assertEquals(1, storePurchase.getCustomQuantity());
		assertEquals(1, storePurchase.getProductType());
	}

	private GooglePlayStoreManager init(){
		GooglePlayStoreManager mgr = new  GooglePlayStoreManager();
		mgr.setCertificatePassword("notasecret");
		mgr.setCertificatePath("Google Play Android Developer-2d8501f0dbf2.p12");
		mgr.setServiceAccountEmail("181771219097-5jrctcqbcktq1r3fe7gd3kus5bdpge6k@developer.gserviceaccount.com");
		return mgr;
	}



}
