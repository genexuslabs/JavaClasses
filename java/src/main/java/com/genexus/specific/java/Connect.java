package com.genexus.specific.java;

import java.util.Date;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.UserInformation;

import json.org.json.IExtensionJSONObject;
import json.org.json.JSONException;
import json.org.json.JSONObject;



public final class Connect {

	public static void init()
	{
		SpecificImplementation.Application = new Application();
		SpecificImplementation.FileUtils = new FileUtils();
		SpecificImplementation.Cursor = new Cursor();
		SpecificImplementation.BatchUpdateCursor = new BatchUpdateCursor();
		SpecificImplementation.HttpClient = new HttpClient();
		SpecificImplementation.DynamicExecute = new DynamicExecute();
		SpecificImplementation.GXDirectory = new GXDirectory();
		SpecificImplementation.GXExternalFileInfo = new GXExternalFileInfo();
		SpecificImplementation.GXSilentTrnSdt = new GXSilentTrnSdt();
		SpecificImplementation.GXSmartCacheProvider = new GXSmartCacheProvider();
		SpecificImplementation.GXutil = new GXutil();
		SpecificImplementation.GXXMLSerializable = new GXXMLSerializable();
		SpecificImplementation.GXXMLSerializer = new GXXMLserializer();
		SpecificImplementation.HttpClient = new HttpClient();
		SpecificImplementation.HTTPConnection = new HTTPConnection();
		SpecificImplementation.HttpCookie = new HttpCookie();
		SpecificImplementation.ImagesPath = new ImagesPath();
		SpecificImplementation.LocalUtil = new LocalUtil();
		SpecificImplementation.LogManager = new LogManager();
		SpecificImplementation.Messages = new Messages();
		SpecificImplementation.NativeFunctions = new NativeFunctions();
		SpecificImplementation.PictureFormatter = new PictureFormatter();
		SpecificImplementation.Algorithms = new CryptoAlghorithms();
		SpecificImplementation.UserLog = new UserLog();
		SpecificImplementation.KeepDecimals = false;
		SpecificImplementation.MillisecondMask = "S";
		SpecificImplementation.cdowMask = "EEEEE";
		SpecificImplementation.SupportPending = false;
		SpecificImplementation.AddToArrayCurrent = false;
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
