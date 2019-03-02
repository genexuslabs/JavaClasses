package com.genexus.webpanels;

import com.genexus.internet.HttpContext;

public class GXResourceProvider extends GXWebObjectStub
{
    public static final String PROVIDER_NAME = GXResourceProvider.class.getName();
    
    protected void doExecute(HttpContext context) throws Exception
    {
        new WebApplicationStartup().init(com.genexus.Application.gxCfg, context);
        
        String resourceType = context.GetNextPar();
        if (resourceType.trim().equalsIgnoreCase("image"))
        {
                String imageGUID = context.GetNextPar();
                String kbId = context.GetNextPar();
                String theme = context.GetNextPar();
                context.setAjaxCallMode();
                context.setTheme(theme);
                String imagePath = context.getImagePath(imageGUID, kbId, theme);
                if (imagePath != null && !imagePath.equals(""))
                {
                    context.getResponse().setContentType("text/plain");
                    context.getResponse().getWriter().write(imagePath + "\n");
                    context.getResponse().flushBuffer();
                    return;
                }
        }
        context.sendResponseStatus(404, "Resource not found");
    }   protected boolean IntegratedSecurityEnabled( )
   {
      return false;
   }	   protected int IntegratedSecurityLevel( )
   {
      return 0;
   }      protected String IntegratedSecurityPermissionPrefix( )
   {
      return "";
   }
   
   protected void init(HttpContext context )
   {
   }      
}
