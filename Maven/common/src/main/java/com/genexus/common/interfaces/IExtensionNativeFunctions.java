package com.genexus.common.interfaces;

import com.genexus.platform.INativeFunctions;
import com.genexus.util.IThreadLocal;
import com.genexus.util.IThreadLocalInitializer;

public interface IExtensionNativeFunctions {

	IThreadLocal newThreadLocal(IThreadLocalInitializer initializer);

	INativeFunctions getInstance();

}
