package com.genexus.specific.android;

import java.util.Date;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.UserInformation;

import json.org.json.IExtensionJSONObject;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public final class Connect {

	public static void init()
	{
	
		SpecificImplementation.FileUtils = new FileUtils();
		SpecificImplementation.Application = new Application();
		SpecificImplementation.LogManager = new LogManager();
		SpecificImplementation.UserLog = new UserLog();
		SpecificImplementation.HttpClient = new HttpClient();
		SpecificImplementation.GXDirectory = new GXDirectory();
		SpecificImplementation.GXExternalFileInfo = new GXExternalFileInfo();
		SpecificImplementation.GXSmartCacheProvider = new GXSmartCacheProvider();
		SpecificImplementation.GXutil = new GXutil();
		SpecificImplementation.HttpClient = new HttpClient();
		SpecificImplementation.ImagesPath = new ImagesPath();
		SpecificImplementation.LocalUtil = new LocalUtil();
		SpecificImplementation.Messages = new Messages();
		SpecificImplementation.NativeFunctions = new NativeFunctions();
		SpecificImplementation.PictureFormatter = new PictureFormatter();
		SpecificImplementation.Algorithms = new CryptoAlghorithms();
		SpecificImplementation.KeepDecimals = true;
		SpecificImplementation.MillisecondMask = "SSS";
		SpecificImplementation.SupportPending = true;
		SpecificImplementation.cdowMask = "EEEE";
		SpecificImplementation.AddToArrayCurrent = true;
		SpecificImplementation.Base64Encode = "8859_1";
		JSONObject.extension = new JSONObjectExtension();
		
	}
	
	 static class JSONObjectExtension implements IExtensionJSONObject {

			@Override
			public String dateToString(Date d) throws JSONException {
				if (d == null) {
			        throw new JSONException("Null pointer");
			    }
			    UserInformation ui = (UserInformation) com.genexus.GXObjectHelper.getUserInformation(new com.genexus.ModelContext(com.genexus.ModelContext.getModelContextPackageClass()), -1);
			    com.genexus.LocalUtil localUtil = ui.getLocalUtil();
			    String dateString = localUtil.format(d, localUtil.getDateFormat() + " " + localUtil.getTimeFormat());
			    try
			    {
			    	com.genexus.db.DBConnectionManager.getInstance().disconnect(ui.getHandle());
			    }
			    catch (Throwable e)
			    {}
			    return dateString;
			}
			
		}
}
