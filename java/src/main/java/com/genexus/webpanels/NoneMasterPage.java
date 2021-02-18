/*
               File: nonemasterpage_impl
        Description: None Master Page
             Author: GeneXus Java Generator version 15_0_12-126415
       Generated on: April 26, 2019 17:13:47.18
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.genexus.webpanels;
import com.genexus.*;
import com.genexus.internet.HttpContext;
import com.genexus.webpanels.*;

public final  class NoneMasterPage extends GXMasterPage
{
    public NoneMasterPage( com.genexus.internet.HttpContext context )
    {
        super(context);
    }

    public NoneMasterPage( int remoteHandle )
    {
        super( remoteHandle , new ModelContext( NoneMasterPage.class ));
    }

    public NoneMasterPage( int remoteHandle ,
                                ModelContext context )
    {
        super( remoteHandle , context);
    }

    static String ArtifactUniqueId ;
    static
    {
        ArtifactUniqueId = com.genexus.util.GxUtilsLoader.getClassUniqueId( NoneMasterPage.class) ;
    }

    protected void createObjects( )
    {
    }

    public void initweb( )
    {
        initialize_properties( ) ;
    }

    public void webExecute( )
    {
        initweb( ) ;
        if ( ! isAjaxCallMode( ) )
        {
            pa0A2( ) ;
            if ( ! isAjaxCallMode( ) )
            {
            }
            if ( ( GxWebError == 0 ) && ! isAjaxCallMode( ) )
            {
                ws0A2( ) ;
                if ( ! isAjaxCallMode( ) )
                {
                    we0A2( ) ;
                }
            }
        }
        cleanup();
    }

    public void renderHtmlHeaders( )
    {
        if ( ! isFullAjaxMode( ) )
        {
            GXWebForm.addResponsiveMetaHeaders((getDataAreaObject() == null ? Form : getDataAreaObject().getForm()).getMeta());
            getDataAreaObject().renderHtmlHeaders();
        }
    }

    public void renderHtmlOpenForm( )
    {
        if ( ! isFullAjaxMode( ) )
        {
            getDataAreaObject().renderHtmlOpenForm();
        }
    }

    public void send_integrity_footer_hashes( )
    {
        GXKey = httpContext.decrypt64( httpContext.getCookie( "GX_SESSION_ID"), context.getServerKey( )) ;
    }

    public void sendCloseFormHiddens( )
    {
        /* Send hidden variables. */
        /* Send saved values. */
        send_integrity_footer_hashes( ) ;
    }

    public void renderHtmlCloseForm0A2( )
    {
        sendCloseFormHiddens( ) ;
        sendSecurityToken(sPrefix);
        if ( ! isFullAjaxMode( ) )
        {
            getDataAreaObject().renderHtmlCloseForm();
        }
        if ( httpContext.isSpaRequest( ) )
        {
            httpContext.disableOutput();
        }
        httpContext.AddJavascriptSource("nonemasterpage.js", "?"+ArtifactUniqueId, false, true);
        httpContext.writeTextNL( "</body>") ;
        httpContext.writeTextNL( "</html>") ;
        if ( httpContext.isSpaRequest( ) )
        {
            httpContext.enableOutput();
        }
    }

    public String getPgmname( )
    {
        return "NoneMasterPage" ;
    }

    public String getPgmdesc( )
    {
        return "None Master Page" ;
    }

    public void wb0A0( )
    {
        if ( httpContext.isAjaxRequest( ) )
        {
            httpContext.disableOutput();
        }
        if ( ! wbLoad )
        {
            renderHtmlHeaders( ) ;
            renderHtmlOpenForm( ) ;
            if ( ! ShowMPWhenPopUp( ) && httpContext.isPopUpObject( ) )
            {
                if ( httpContext.isSpaRequest( ) )
                {
                    httpContext.enableOutput();
                }
                if ( httpContext.isSpaRequest( ) )
                {
                    httpContext.disableJsOutput();
                }
                /* Content placeholder */
                httpContext.writeText( "<div") ;
                classAttribute( httpContext, "gx-content-placeholder");
                httpContext.writeText( ">") ;
                if ( ! isFullAjaxMode( ) )
                {
                    getDataAreaObject().renderHtmlContent();
                }
                httpContext.writeText( "</div>") ;
                if ( httpContext.isSpaRequest( ) )
                {
                    httpContext.disableOutput();
                }
                if ( httpContext.isSpaRequest( ) )
                {
                    httpContext.enableJsOutput();
                }
                wbLoad = true ;
                return  ;
            }
            /* Div Control */
            gx_div_start( httpContext, "", 1, 0, "px", 0, "px", "Section", "left", "top", " "+"data-gx-base-lib=\"bootstrapv3\""+" "+"data-abstract-form"+" ", "", "div");
            /* Div Control */
            gx_div_start( httpContext, divMaintable_Internalname, 1, 0, "px", 0, "px", "Table", "left", "top", "", "", "div");
            /* Div Control */
            gx_div_start( httpContext, "", 1, 0, "px", 0, "px", "row", "left", "top", "", "", "div");
            /* Div Control */
            gx_div_start( httpContext, "", 1, 0, "px", 0, "px", "col-xs-12", "left", "top", "", "", "div");
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.enableOutput();
            }
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.disableJsOutput();
            }
            /* Content placeholder */
            httpContext.writeText( "<div") ;
            classAttribute( httpContext, "gx-content-placeholder");
            httpContext.writeText( ">") ;
            if ( ! isFullAjaxMode( ) )
            {
                getDataAreaObject().renderHtmlContent();
            }
            httpContext.writeText( "</div>") ;
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.disableOutput();
            }
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.enableJsOutput();
            }
            gx_div_end( httpContext, "left", "top", "div");
            gx_div_end( httpContext, "left", "top", "div");
            gx_div_end( httpContext, "left", "top", "div");
            gx_div_end( httpContext, "left", "top", "div");
        }
        wbLoad = true ;
    }

    public static void classAttribute( HttpContext httpContext ,
                                       String sClass )
    {
        if ( ! (GXutil.strcmp("", sClass)==0) )
        {
            httpContext.writeText( " class=\"") ;
            httpContext.writeValue( GXutil.ltrim( sClass)) ;
            httpContext.writeText( "\" ") ;
        }
    }


    public static void gx_div_start( HttpContext httpContext ,
                                     String sInternalName ,
                                     int nVisible ,
                                     int nWidth ,
                                     String sWidthUnit ,
                                     int nHeight ,
                                     String sHeightUnit ,
                                     String sClassString ,
                                     String sAlign ,
                                     String sVAlign ,
                                     String sTags ,
                                     String sExtraStyle ,
                                     String sHtmlTag )
    {
        String sOStyle ;
        boolean bHAlignedVar ;
        boolean bVAlignedVar ;
        bHAlignedVar = (boolean)(!(GXutil.strcmp("", sAlign)==0)&&(GXutil.strcmp(GXutil.lower( sAlign), "left")!=0)) ;
        bVAlignedVar = (boolean)(!(GXutil.strcmp("", sVAlign)==0)&&(GXutil.strcmp(GXutil.lower( sVAlign), "top")!=0)) ;
        httpContext.writeText( "<"+sHtmlTag+" ") ;
        if ( ! (GXutil.strcmp("", sInternalName)==0) )
        {
            httpContext.writeText( "id=\""+sInternalName+"\" ") ;
        }
        classAttribute( httpContext, sClassString);
        sOStyle = "" ;
        if ( nVisible == 0 )
        {
            sOStyle = "display:none;" ;
        }
        if ( ! (0==nWidth) )
        {
            sOStyle = sOStyle + " width:" + GXutil.ltrim( GXutil.str( nWidth, 10, 0)) + sWidthUnit + ";" ;
        }
        if ( ! (0==nHeight) )
        {
            sOStyle = sOStyle + " height:" + GXutil.ltrim( GXutil.str( nHeight, 10, 0)) + sHeightUnit + ";" ;
        }
        if ( ! (GXutil.strcmp("", sExtraStyle)==0) )
        {
            sOStyle = sOStyle + GXutil.CssPrettify( sExtraStyle+";") ;
        }
        styleAttribute( httpContext, sOStyle);
        if ( ! (GXutil.strcmp("", sTags)==0) )
        {
            httpContext.writeText( sTags) ;
        }
        if ( bHAlignedVar )
        {
            httpContext.writeText( " data-align=\"") ;
            httpContext.writeText( GXutil.lower( sAlign)) ;
            httpContext.writeText( "\"") ;
        }
        if ( bVAlignedVar )
        {
            httpContext.writeText( " data-valign=\"") ;
            httpContext.writeText( GXutil.lower( sVAlign)) ;
            httpContext.writeText( "\"") ;
        }
        httpContext.writeText( ">") ;
        if ( bHAlignedVar || bVAlignedVar )
        {
            httpContext.writeText( "<div data-align-outer=\"\"><div data-align-inner=\"\">") ;
        }
    }

    public static void styleAttribute( HttpContext httpContext ,
                                       String sStyle )
    {
        if ( ! (GXutil.strcmp("", sStyle)==0) )
        {
            httpContext.writeText( " style=\"") ;
            httpContext.writeValue( GXutil.ltrim( GXutil.CssPrettify( sStyle))) ;
            httpContext.writeText( "\" ") ;
        }
    }

    public static void gx_div_end( HttpContext httpContext ,
                                   String sAlign ,
                                   String sVAlign ,
                                   String sHtmlTag )
    {
        boolean bHAlignedVar ;
        boolean bVAlignedVar ;
        bHAlignedVar = (boolean)(!(GXutil.strcmp("", sAlign)==0)&&(GXutil.strcmp(GXutil.lower( sAlign), "left")!=0)) ;
        bVAlignedVar = (boolean)(!(GXutil.strcmp("", sVAlign)==0)&&(GXutil.strcmp(GXutil.lower( sVAlign), "top")!=0)) ;
        if ( bHAlignedVar || bVAlignedVar )
        {
            httpContext.writeText( "</div></div>") ;
        }
        httpContext.writeText( "</"+sHtmlTag+">") ;
    }

    public static boolean gx_redirect( HttpContext httpContext )
    {
        if ( httpContext.willRedirect( ) )
        {
            httpContext.redirect( httpContext.wjLoc );
            httpContext.dispatchAjaxCommands();
            return true ;
        }
        else if ( httpContext.nUserReturn == 1 )
        {
            if ( httpContext.isAjaxRequest( ) )
            {
                httpContext.ajax_rsp_command_close();
                httpContext.dispatchAjaxCommands();
            }
            else
            {
                if ( (GXutil.strcmp("", httpContext.getReferer( ))==0) || httpContext.isLocalStorageSupported( ) )
                {
                    httpContext.setStream();
                    if ( httpContext.isSpaRequest( true) )
                    {
                        httpContext.setHeader("X-SPA-RETURN", httpContext.getWebReturnParmsJS( ));
                        httpContext.setHeader("X-SPA-RETURN-MD", httpContext.getWebReturnParmsMetadataJS( ));
                    }
                    else
                    {
                        httpContext.writeText( httpContext.htmlDocType( )) ;
                        httpContext.writeText( "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><title>Close window</title>") ;
                        httpContext.AddJavascriptSource("jquery.js", "?"+httpContext.getBuildNumber( 126415), false, true);
                        httpContext.AddJavascriptSource("gxgral.js", "?"+httpContext.getBuildNumber( 126415), false, true);
                        httpContext.writeText( "</head><body><script type=\"text/javascript\">") ;
                        httpContext.writeText( "gx.fn.closeWindowServerScript(") ;
                        httpContext.writeText( httpContext.getWebReturnParmsJS( )) ;
                        httpContext.writeText( ", ") ;
                        httpContext.writeText( httpContext.getWebReturnParmsMetadataJS( )) ;
                        if ( httpContext.isLocalStorageSupported( ) )
                        {
                            httpContext.writeText( ", true") ;
                        }
                        else
                        {
                            httpContext.writeText( ", false") ;
                        }
                        httpContext.writeText( ");</script></body></html>") ;
                    }
                }
                else
                {
                    httpContext.redirect( httpContext.getReferer( ) );
                    httpContext.windowClosed();
                }
            }
            return true ;
        }
        else
        {
            return false ;
        }
    }



    public void start0A2( )
    {
        wbLoad = false ;
        wbEnd = 0 ;
        wbStart = 0 ;
        httpContext.wjLoc = "" ;
        httpContext.nUserReturn = (byte)(0) ;
        httpContext.wbHandled = (byte)(0) ;
        if ( GXutil.strcmp(httpContext.getRequestMethod( ), "POST") == 0 )
        {
        }
        wbErr = false ;
        strup0A0( ) ;
        if ( ! httpContext.willRedirect( ) && ( httpContext.nUserReturn != 1 ) )
        {
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.disableJsOutput();
            }
            if ( getDataAreaObject().executeStartEvent() != 0 )
            {
                httpContext.setAjaxCallMode();
            }
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.enableJsOutput();
            }
        }
    }

    public void ws0A2( )
    {
        start0A2( ) ;
        evt0A2( ) ;
    }

    public void evt0A2( )
    {
        if ( GXutil.strcmp(httpContext.getRequestMethod( ), "POST") == 0 )
        {
            if ( ! httpContext.willRedirect( ) && ( httpContext.nUserReturn != 1 ) && ! wbErr )
            {
                /* Read Web Panel buttons. */
                sEvt = httpContext.cgiGet( "_EventName") ;
                EvtGridId = httpContext.cgiGet( "_EventGridId") ;
                EvtRowId = httpContext.cgiGet( "_EventRowId") ;
                if ( GXutil.len( sEvt) > 0 )
                {
                    sEvtType = GXutil.left( sEvt, 1) ;
                    sEvt = GXutil.right( sEvt, GXutil.len( sEvt)-1) ;
                    if ( GXutil.strcmp(sEvtType, "E") == 0 )
                    {
                        sEvtType = GXutil.right( sEvt, 1) ;
                        if ( GXutil.strcmp(sEvtType, ".") == 0 )
                        {
                            sEvt = GXutil.left( sEvt, GXutil.len( sEvt)-1) ;
                            if ( GXutil.strcmp(sEvt, "RFR_MPAGE") == 0 )
                            {
                                httpContext.wbHandled = (byte)(1) ;
                                dynload_actions( ) ;
                            }
                            else if ( GXutil.strcmp(sEvt, "LOAD_MPAGE") == 0 )
                            {
                                httpContext.wbHandled = (byte)(1) ;
                                dynload_actions( ) ;
                                /* Execute user event: Load */
                                e110A2 ();
                            }
                            else if ( GXutil.strcmp(sEvt, "ENTER_MPAGE") == 0 )
                            {
                                httpContext.wbHandled = (byte)(1) ;
                                if ( ! wbErr )
                                {
                                    Rfr0gs = false ;
                                    if ( ! Rfr0gs )
                                    {
                                    }
                                    dynload_actions( ) ;
                                }
                                /* No code required for Cancel button. It is implemented as the Reset button. */
                            }
                            else if ( GXutil.strcmp(sEvt, "LSCR") == 0 )
                            {
                                httpContext.wbHandled = (byte)(1) ;
                                dynload_actions( ) ;
                                dynload_actions( ) ;
                            }
                        }
                        else
                        {
                        }
                    }
                    if ( httpContext.wbHandled == 0 )
                    {
                        getDataAreaObject().dispatchEvents();
                    }
                    httpContext.wbHandled = (byte)(1) ;
                }
            }
        }
    }

    public void we0A2( )
    {
        if ( ! gx_redirect( httpContext) )
        {
            Rfr0gs = true ;
            refresh( ) ;
            renderHtmlCloseForm0A2( ) ;
        }
    }

    public void pa0A2( )
    {
        if ( nDonePA == 0 )
        {
            if ( (GXutil.strcmp("", httpContext.getCookie( "GX_SESSION_ID"))==0) )
            {
                gxcookieaux = httpContext.setCookie( "GX_SESSION_ID", httpContext.encrypt64( com.genexus.util.Encryption.getNewKey( ), context.getServerKey( )), "", GXutil.nullDate(), "", httpContext.getHttpSecure()) ;
            }
            GXKey = httpContext.decrypt64( httpContext.getCookie( "GX_SESSION_ID"), context.getServerKey( )) ;
            toggleJsOutput = httpContext.isJsOutputEnabled( ) ;
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.disableJsOutput();
            }
            init_web_controls( ) ;
            if ( toggleJsOutput )
            {
                if ( httpContext.isSpaRequest( ) )
                {
                    httpContext.enableJsOutput();
                }
            }
            if ( ! httpContext.isAjaxRequest( ) )
            {
            }
            nDonePA = (byte)(1) ;
        }
    }

    public void dynload_actions( )
    {
        /* End function dynload_actions */
    }

    public void send_integrity_hashes( )
    {
    }

    public void clear_multi_value_controls( )
    {
        if ( httpContext.isAjaxRequest( ) )
        {
            dynload_actions( ) ;
            before_start_formulas( ) ;
        }
    }

    public void fix_multi_value_controls( )
    {
    }

    public void refresh( )
    {
        send_integrity_hashes( ) ;
        rf0A2( ) ;
        if ( isFullAjaxMode( ) )
        {
            send_integrity_footer_hashes( ) ;
        }
        /* End function Refresh */
    }

    public void initialize_formulas( )
    {
        /* GeneXus formulas. */
        Gx_err = (short)(0) ;
    }

    public void rf0A2( )
    {
        initialize_formulas( ) ;
        clear_multi_value_controls( ) ;
        if ( ShowMPWhenPopUp( ) || ! httpContext.isPopUpObject( ) )
        {
            gxdyncontrolsrefreshing = true ;
            fix_multi_value_controls( ) ;
            gxdyncontrolsrefreshing = false ;
        }
        if ( ! httpContext.willRedirect( ) && ( httpContext.nUserReturn != 1 ) )
        {
            /* Execute user event: Load */
            e110A2 ();
            wb0A0( ) ;
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.enableOutput();
            }
        }
    }

    public void send_integrity_lvl_hashes0A2( )
    {
    }

    public void before_start_formulas( )
    {
        Gx_err = (short)(0) ;
    }

    public void strup0A0( )
    {
        /* Before Start, stand alone formulas. */
        before_start_formulas( ) ;
        httpContext.wbGlbDoneStart = (byte)(1) ;
        /* After Start, stand alone formulas. */
        if ( GXutil.strcmp(httpContext.getRequestMethod( ), "POST") == 0 )
        {
            /* Read saved SDTs. */
            /* Read variables values. */
            /* Read saved values. */
            /* Read subfile selected row values. */
            /* Read hidden variables. */
            GXKey = httpContext.decrypt64( httpContext.getCookie( "GX_SESSION_ID"), context.getServerKey( )) ;
        }
        else
        {
            dynload_actions( ) ;
        }
    }

    protected void nextLoad( )
    {
    }

    protected void e110A2( )
    {
        /* Load Routine */
    }

    public void setparameters( Object[] obj )
    {
    }

    public String getresponse( String sGXDynURL )
    {
        initialize_properties( ) ;
        BackMsgLst = httpContext.GX_msglist ;
        httpContext.GX_msglist = LclMsgLst ;
        sDynURL = sGXDynURL ;
        nGotPars = 1 ;
        nGXWrapped = 1 ;
        httpContext.setWrapped(true);
        pa0A2( ) ;
        ws0A2( ) ;
        we0A2( ) ;
        httpContext.setWrapped(false);
        httpContext.GX_msglist = BackMsgLst ;
        String response = "";
        try
        {
            response = ((java.io.ByteArrayOutputStream) httpContext.getOutputStream()).toString("UTF8");
        }
        catch (java.io.UnsupportedEncodingException e)
        {
            Application.printWarning(e.getMessage(), e);
        }
        finally
        {
            httpContext.closeOutputStream();
        }
        return response;
    }

    public void responsestatic( String sGXDynURL )
    {
    }

    public void master_styles( )
    {
        define_styles( ) ;
    }

    public void define_styles( )
    {
        httpContext.AddThemeStyleSheetFile("", context.getHttpContext().getTheme( )+".css", "?"+httpContext.getCacheInvalidationToken( ));
        boolean outputEnabled = httpContext.isOutputEnabled( ) ;
        if ( httpContext.isSpaRequest( ) )
        {
            httpContext.enableOutput();
        }
        idxLst = 1 ;
        while ( idxLst <= (getDataAreaObject() == null ? Form : getDataAreaObject().getForm()).getJscriptsrc().getCount() )
        {
            httpContext.AddJavascriptSource(GXutil.rtrim( (getDataAreaObject() == null ? Form : getDataAreaObject().getForm()).getJscriptsrc().item(idxLst)), "?"+ArtifactUniqueId, true, true);
            idxLst = (int)(idxLst+1) ;
        }
        if ( ! outputEnabled )
        {
            if ( httpContext.isSpaRequest( ) )
            {
                httpContext.disableOutput();
            }
        }
        /* End function define_styles */
    }

    public void include_jscripts( )
    {
        if ( nGXWrapped != 1 )
        {
            httpContext.AddJavascriptSource("nonemasterpage.js", "?"+ArtifactUniqueId, false, true);
        }
        /* End function include_jscripts */
    }

    public void init_default_properties( )
    {
        divMaintable_Internalname = "MAINTABLE_MPAGE" ;
        (getDataAreaObject() == null ? Form : getDataAreaObject().getForm()).setInternalname( "FORM_MPAGE" );
    }

    public void initialize_properties( )
    {
        if ( httpContext.isSpaRequest( ) )
        {
            httpContext.disableJsOutput();
        }
        init_default_properties( ) ;
        Contholder1.setDataArea(getDataAreaObject());
        if ( httpContext.isSpaRequest( ) )
        {
            httpContext.enableJsOutput();
        }
    }

    public void init_web_controls( )
    {
        /* End function init_web_controls */
    }

    public boolean supportAjaxEvent( )
    {
        return true ;
    }

    public void initializeDynEvents( )
    {
        setEventMetadata("REFRESH_MPAGE","{handler:'refresh',iparms:[]");
        setEventMetadata("REFRESH_MPAGE",",oparms:[]}");
    }

    protected boolean IntegratedSecurityEnabled( )
    {
        return false;
    }

    protected int IntegratedSecurityLevel( )
    {
        return 0;
    }

    protected String IntegratedSecurityPermissionPrefix( )
    {
        return "";
    }

    protected void cleanup( )
    {
        super.cleanup();
        CloseOpenCursors();
    }

    protected void CloseOpenCursors( )
    {
    }

    /* Aggregate/select formulas */
    public void initialize( )
    {
        Contholder1 = new com.genexus.webpanels.GXDataAreaControl();
        Form = new com.genexus.webpanels.GXWebForm();
        GXKey = "" ;
        sPrefix = "" ;
        sEvt = "" ;
        EvtGridId = "" ;
        EvtRowId = "" ;
        sEvtType = "" ;
        BackMsgLst = new com.genexus.internet.MsgList();
        LclMsgLst = new com.genexus.internet.MsgList();
        sDynURL = "" ;
        /* GeneXus formulas. */
        Gx_err = (short)(0) ;
    }

    private byte GxWebError ;
    private byte nDonePA ;
    private byte nGotPars ;
    private byte nGXWrapped ;
    private short wbEnd ;
    private short wbStart ;
    private short gxcookieaux ;
    private short Gx_err ;
    private int idxLst ;
    private String GXKey ;
    private String sPrefix ;
    private String divMaintable_Internalname ;
    private String sEvt ;
    private String EvtGridId ;
    private String EvtRowId ;
    private String sEvtType ;
    private String sDynURL ;
    private boolean wbLoad ;
    private boolean Rfr0gs ;
    private boolean wbErr ;
    private boolean toggleJsOutput ;
    private boolean gxdyncontrolsrefreshing ;
    private com.genexus.internet.MsgList BackMsgLst ;
    private com.genexus.internet.MsgList LclMsgLst ;
    private com.genexus.webpanels.GXDataAreaControl Contholder1 ;
    private com.genexus.webpanels.GXWebForm Form ;
}

