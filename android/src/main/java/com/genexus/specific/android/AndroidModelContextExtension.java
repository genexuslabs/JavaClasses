package com.genexus.specific.android;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.IExtensionModelContext;

public class AndroidModelContextExtension implements IExtensionModelContext {

    public boolean isTimezoneSet(ModelContext ctx) {
        return true;
    }

    public void initPackageClass(ModelContext ctx, Class packageClass) {
        if (com.genexus.Application.getContextClassName() == null)
            ctx.packageClass = packageClass;
        else
            ctx.packageClass = com.genexus.Application.getContextClassName();
    }
}
