package com.genexus.common.interfaces;

import com.genexus.ModelContext;

public interface IExtensionModelContext {

    boolean isTimezoneSet(ModelContext ctx);

    void initPackageClass(ModelContext ctx, Class packageClass);
}
