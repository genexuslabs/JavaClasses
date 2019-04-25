package com.genexus.specific.java;

import com.genexus.GxEjbContext;
import com.genexus.SessionInstances;
import com.genexus.common.interfaces.IExtensionModelContext;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;

public class JavaModelContextExtension implements IExtensionModelContext {

    public boolean isTimezoneSet(ModelContext ctx) {
        return !(ctx.isNullHttpContext());
    }

    public void initPackageClass(ModelContext ctx, Class packageClass) {
        if (ModelContext.gxcfgPackageClass != null)
        {
            ctx.packageClass = ModelContext.gxcfgPackageClass;
        }
        else
        {
            if (ModelContext.threadModelContext.get() == null)
            {
                ctx.packageClass = packageClass;
            }
            else
            {
                ctx.packageClass = ModelContext.getModelContext().packageClass;
            }
        }
    }

    private static String[] copyKeys = { "GAMConCli", "GAMSession", "GAMError", "GAMErrorURL", "GAMRemote" };
    private static void initializeSubmitSession(ModelContext oldContext, ModelContext newContext) {
        HttpContext httpCtx = oldContext.getHttpContext();
        HttpContext newHttpCtx = newContext.getHttpContext();
        if (httpCtx != null && newHttpCtx != null) {
            com.genexus.webpanels.WebSession ws = newHttpCtx.getWebSession();
            for (int i = 0; i < copyKeys.length && ws != null; i++){
                Object value = httpCtx.getSessionValue(copyKeys[i]);
                if (value != null) {
                    ws.setValue(copyKeys[i], value.toString());
                }
            }
        }
    }
}
