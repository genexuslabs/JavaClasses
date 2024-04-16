package com.genexus.mock;

import com.genexus.ModelContext;

public interface IGXMock {
	boolean handle(int remoteHandle , ModelContext context, Object gxObject, String[] parametersName);
}
