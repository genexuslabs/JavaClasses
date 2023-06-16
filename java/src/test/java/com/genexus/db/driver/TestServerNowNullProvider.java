package com.genexus.db.driver;

import com.genexus.Application;
import com.genexus.ClientContext;
import com.genexus.GXObjectHelper;
import com.genexus.GXutil;
import com.genexus.db.IDataStoreProvider;
import com.genexus.db.UserInformation;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class TestServerNowNullProvider {

	@Test
	public void testGetProviderNormalizedUrl() {
		Connect.init();
		Application.init(GXcfg.class);
		int  remoteHandle = ((UserInformation) GXObjectHelper.getUserInformation(ClientContext.getModelContext(), -1)).getHandle();

		java.util.Date serverDate = GXutil.serverNow(ClientContext.getModelContext(), remoteHandle, (IDataStoreProvider)null) ;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(serverDate);
		int century = getCentury(calendar);
		Assert.assertEquals(century, 21);

		serverDate = GXutil.serverNowMs(ClientContext.getModelContext(), remoteHandle, null) ;
		calendar.setTime(serverDate);
		century = getCentury(calendar);
		Assert.assertEquals(century, 21);

	}

	private int getCentury(Calendar calendar) {
		int year = calendar.get(Calendar.YEAR) - 1;
		return  (year / 100) + 1;
	}
}
