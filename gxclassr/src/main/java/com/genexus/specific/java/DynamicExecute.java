package com.genexus.specific.java;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.IExtensionDynamicExecute;

public class DynamicExecute implements IExtensionDynamicExecute {

	@Override
	public boolean getIsWebContext(Object ctxt) {
		ModelContext context = (ModelContext) ctxt;
		if (context.getHttpContext() != null && context.getHttpContext() instanceof com.genexus.webpanels.HttpContextWeb)
		{
			return true;
		}
		return false;
	}

}
