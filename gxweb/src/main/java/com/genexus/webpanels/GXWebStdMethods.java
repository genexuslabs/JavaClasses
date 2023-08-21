package com.genexus.webpanels;

import java.lang.reflect.Method;

import com.genexus.ModelContext;
import com.genexus.internet.HttpAjaxContext;

public class GXWebStdMethods
{
    private static Class webStdClass = null;
    
    private static Class getWebStdClass(ModelContext context)
    {
        if (webStdClass == null)
        {
            try
            {
                String packageName = "";
                if (!context.getPackageName().equals(""))
                {
                    packageName = context.getPackageName() + ".";
                }
                webStdClass = Class.forName(packageName + "GxWebStd");
            }
            catch(Exception e) {}
        }
        return webStdClass;
    }
    
    private static Method getMethod(ModelContext context, String name)
    {
        Class webStdFuncs = getWebStdClass(context);
        Method[] methods = webStdFuncs.getMethods();
        for (int i=0; i<methods.length; i++)
        {
            if (methods[i].getName().equalsIgnoreCase(name))
            {
                return methods[i];
            }
        }
        return null;
    }
    
    private static Object[] convertParms(Class[] types, Object[] parms) throws Exception
    {
        Object[] convertedParms = new Object[parms.length];
        for (int i=0; i<types.length; i++)
        {
            if (types[i].equals(byte.class) || types[i].equals(int.class) || types[i].equals(short.class))
            {
                convertedParms[i] = com.genexus.GXutil.convertObjectTo(parms[i], types[i], false);
            }
            else
            {
                convertedParms[i] = parms[i];
            }
        }
        return convertedParms;
    }
    
    public static void callMethod(ModelContext context, String controlType, Object[] parms, String GridName)
    {
        if (tagHasStdMethod(controlType))
        {
            try
            {
                Class webStdFuncs = getWebStdClass(context);
                if (webStdFuncs != null)
                {
					HttpAjaxContext httpContext = (HttpAjaxContext) context.getHttpContext();
                    if (httpContext != null)
                    {
                        boolean addCallerPgm = true;
                        Method method = null;
                        if (controlType.equalsIgnoreCase("edit"))
                        {
                            method = getMethod(context, "gx_single_line_edit");
                        }
                        else if (controlType.equalsIgnoreCase("html_textarea"))
                        {
                            method = getMethod(context, "gx_html_textarea");
                        }
                        else if (controlType.equalsIgnoreCase("button"))
                        {
                            String sCtrlName = (String)parms[1];//sCtrlName
                            if (sCtrlName.lastIndexOf('_') > 0)
                            {
                                String row = sCtrlName.substring(sCtrlName.lastIndexOf('_') + 1);
                                short eventType = ((Number) parms[10]).shortValue();
                                if ( eventType == 7) //execCliEvt
                                {
                                    parms[12] = ((String)parms[12]) + ",'" + GridName + "','" + row + '\''; //sEventName
                                }
                                else if (eventType == 5) //serverEvt
                                {
                                    parms[12] = parms[12] + row; //sEventName 
                                }
                            }
                            method = getMethod(context, "gx_button_ctrl");
                        }
                        else if (controlType.equalsIgnoreCase("blob"))
                        {
                            method = getMethod(context, "gx_blob_field");
                        }
                        else if (controlType.equalsIgnoreCase("label"))
                        {
                            method = getMethod(context, "gx_label_ctrl");
                        }
                        else if (controlType.equalsIgnoreCase("radio"))
                        {
                            method = getMethod(context, "gx_radio_ctrl");
                        }
                        else if (controlType.equalsIgnoreCase("combobox"))
                        {
                            method = getMethod(context, "gx_combobox_ctrl1");
                        }
                        else if (controlType.equalsIgnoreCase("listbox"))
                        {
                            method = getMethod(context, "gx_listbox_ctrl1");
                        }
                        else if (controlType.equalsIgnoreCase("checkbox"))
                        {
                       	    addCallerPgm = false;
                            method = getMethod(context, "gx_checkbox_ctrl");
                        }
                        else if (controlType.equalsIgnoreCase("bitmap"))
                        {
                            method = getMethod(context, "gx_bitmap");
                        }
                        else if (controlType.equalsIgnoreCase("table"))
                        {
                            try
                            {
                                method = getMethod(context, "gx_table_start");
                                String sStyleString = "";
                                if (Integer.parseInt(parms[1].toString()) == 0 /*Visible*/)
                                {
                                        sStyleString = "display:none;";
                                }
                                int nBorder = (parms[8].toString().equals("")) ? 0 : Integer.parseInt(parms[8].toString());
                                method.invoke(null, new Object[] { httpContext,
                                                                        parms[0],       //sCtrlName
                                                                        parms[0],	//sHTMLid
                                                                        "",             //sHTMLTags
                                                                        parms[2],	//sClassString
                                                                        nBorder,	//nBorder
                                                                        parms[6],	//sAlign
                                                                        parms[7],	//sTooltiptext
                                                                        parms[9],	//nCellpadding
                                                                        parms[10],      //nCellspacing
                                                                        sStyleString,	//sStyleString
                                                                        parms[13],	//sRules
                                                                        0,		//nParentIsFreeStyle
                                                                        });
                            }
                            catch(Exception e)
                            {
                                httpContext.writeTextNL("<table>");
                            }
                            method = null;
                        }
                        if (method != null)
                        {
                           int parmsLen =  parms.length + 1;
                           if (addCallerPgm)
                           {
                           	parmsLen++;
                           }
                            Object[] allParms = new Object[parmsLen];
                            allParms[0] = httpContext;
                            System.arraycopy(parms, 0, allParms, 1, parms.length);
                            if (addCallerPgm)
                            {
                            	allParms[parms.length + 1] = ""; //sCallerPgm
                            }
                            if (method.getParameterTypes().length == allParms.length)
                            {
                                method.invoke(null, convertParms(method.getParameterTypes(), allParms));
                            }
                        }
                    }
                }
            }
            catch(Exception e) {}
        }
        else
        {
            openTag(context, controlType, parms);
        }
    }
    
    private static boolean tagHasStdMethod(String tag)
    {
        if (tag.equalsIgnoreCase("row"))
            return false;
        if (tag.equalsIgnoreCase("cell"))
            return false;
        if (tag.equalsIgnoreCase("usercontrol"))
            return false;
        return true;
    }

    public static void openTag(ModelContext context, String tag, Object[] parms)
    {
		HttpAjaxContext httpContext = (HttpAjaxContext) context.getHttpContext();
        if (tag.equalsIgnoreCase("row"))
        {
            httpContext.writeTextNL("<tr>");
        }
        else if (tag.equalsIgnoreCase("cell"))
        {
            httpContext.writeText("<td ");
            String parm = parms[0].toString();
            if (parm != null && !parm.equals(""))
            {
                httpContext.writeText(" background=\"" + parm + "\" ");
            }
            parm = parms[1].toString();
            if (parm != null && !parm.equals(""))
	    {
            httpContext.writeText(" " + parm + " ");
	    }
            httpContext.writeTextNL(">");
        }
        else if (tag.equalsIgnoreCase("usercontrol"))
        {
            httpContext.writeTextNL("<div class=\"gx_usercontrol\" id=\"" + parms[0].toString() + "\"></div>");
        }
    }

    public static void closeTag(ModelContext context, String tag)
    {
		HttpAjaxContext httpContext = (HttpAjaxContext) context.getHttpContext();
        if (tag.equalsIgnoreCase("table"))
        {
            httpContext.writeTextNL("</table>");
        }
        else if (tag.equalsIgnoreCase("row"))
        {
            httpContext.writeTextNL("</tr>");
        }
        else if (tag.equalsIgnoreCase("cell"))
        {
            httpContext.writeTextNL("</td>");
        }
    }
}
