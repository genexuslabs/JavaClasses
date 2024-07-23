package com.genexus.specific.java;

import java.util.Date;

import com.genexus.ApplicationContext;
import com.genexus.common.interfaces.SpecificImplementation;

import org.json.JSONException;

public final class Connect {

	public static void init()
	{
		SpecificImplementation.Application = new Application();
		SpecificImplementation.ModelContext = new JavaModelContextExtension();
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
		if (!ApplicationContext.getInstance().getReorganization())
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
		SpecificImplementation.UseUnicodeCharacterClass = true;
		}
}
