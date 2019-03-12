package com.genexus;

import com.genexus.common.classes.AbstractModelContext;

public class ClientContext
{
	static private LocalUtil localUtil;

	public static void setLocalUtil(LocalUtil localUtil)
	{
		ClientContext.localUtil = localUtil;
	}

	public static LocalUtil getLocalUtil()
	{
		return localUtil;
	}
	
	public static Messages getMessages()
	{
		return localUtil.getMessages();
	}

	private static AbstractModelContext modelContext;
	public static AbstractModelContext getModelContext()
	{
		return modelContext;
	}

	public static void setModelContext(AbstractModelContext modelContext)
	{
		ClientContext.modelContext = modelContext;
	}

	private static int remoteHandle = -1;
	public static int getHandle()
	{
		return remoteHandle;
	}

	public static void setHandle(int remoteHandle)
	{
		ClientContext.remoteHandle = remoteHandle;
	}

}