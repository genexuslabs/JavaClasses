package com.genexus.specific.android;

import java.util.Date;

import com.genexus.common.interfaces.SpecificImplementation;

import org.json.JSONException;
import org.json.JSONObject;

public final class Connect {

	public static void init()
	{
	
		SpecificImplementation.FileUtils = new FileUtils();
		SpecificImplementation.Application = new Application();
		SpecificImplementation.ModelContext = new AndroidModelContextExtension();
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
		// connect GXSilentTrn
		SpecificImplementation.GXSilentTrnSdt = new GXSilentTrnSdt();
		SpecificImplementation.SdtMessages_Message = new SdtMessages_Message();
		SpecificImplementation.JsonSerialization = new AndroidJSONSerialization();

		SpecificImplementation.KeepDecimals = true;
		SpecificImplementation.MillisecondMask = "SSS";
		SpecificImplementation.SupportPending = true;
		SpecificImplementation.cdowMask = "EEEE";
		SpecificImplementation.Base64Encode = "8859_1";
		SpecificImplementation.UseUnicodeCharacterClass = false;
	}
}
