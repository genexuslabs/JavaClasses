/*$Log: GXWebForm.java,v $
/*Revision 1.1.2.2  2006/10/26 15:38:16  alevin
/*- Renombro el setClass a setThemeClass y el getCSSClass a getThemeClass.
/*
/*Revision 1.1.2.1  2005/03/28 15:50:22  dmendez
/*Se implementa objeto form para poder manejarlo en master pages como unico
/*para el master page y placeholder.
/**/

package com.genexus.webpanels;

public class GXWebForm
{
  com.genexus.webpanels.HTMLChoice Meta = new com.genexus.webpanels.HTMLChoice();
  com.genexus.webpanels.HTMLChoice Metaequiv = new com.genexus.webpanels.HTMLChoice();
  com.genexus.internet.StringCollection Jscriptsrc = new com.genexus.internet.StringCollection();
  String Caption = new String("");
  int Backcolor;
  int Textcolor;
  String Background  = new String("");
  int Visible;
  int Windowstate;
  int Enabled;
  int Top;
  int Left;
  int Width;
  int Height;
  String Internalname = new String("");
  String Bitmap = new String("");
  String Tag = new String("");
  String Class = new String("");
  String Headerrawhtml = new String("");

  public com.genexus.webpanels.HTMLChoice getMeta()
  {
    return this.Meta;
  }

 public void setMeta(  com.genexus.webpanels.HTMLChoice Value )
 {
    this.Meta = Value ;
 }

 public com.genexus.webpanels.HTMLChoice getMetaequiv()
 {
    return this.Metaequiv ;
 }

 public void setMetaequiv(  com.genexus.webpanels.HTMLChoice Value )
 {
    this.Metaequiv = Value ;
 }

 public com.genexus.internet.StringCollection getJscriptsrc()
 {
    return this.Jscriptsrc ;
 }

 public void setJscriptsrc(  com.genexus.internet.StringCollection Value )
 {
    this.Jscriptsrc = Value ;
 }

 public String getCaption()
 {
    return this.Caption;
 }

 public void setCaption(  String Value )
 {
    this.Caption = Value ;
 }

 public int getIBackground()
 {
    return this.Backcolor ;
 }

 public void setIBackground(  int Value )
 {
    this.Backcolor = Value ;
 }

 public int getTextcolor()
 {
    return this.Textcolor ;
 }

 public void setTextcolor(  int Value )
 {
    this.Textcolor = Value ;
 }

 public String getBackground()
 {
    return this.Background ;
 }

 public void setBackground(  String Value )
 {
    this.Background = Value ;
 }

 public int getVisible()
 {
    return this.Visible ;
 }

 public void setVisible(  int Value )
 {
    this.Visible = Value ;
 }

 public int getWindowstate()
 {
    return this.Windowstate ;
 }

 public void setWindowstate(  int Value )
 {
    this.Windowstate = Value ;
 }

 public int getEnabled()
 {
    return this.Enabled ;
 }

 public void setEnabled(  int Value )
 {
    this.Enabled = Value ;
 }

public int getTop()
 {
    return this.Top ;
 }

 public void setTop(  int Value )
 {
    this.Top = Value ;
 }

 public int getLeft()
 {
    return this.Left ;
 }

 public void setLeft(  int Value )
 {
    this.Left = Value ;
 }

 public int getWidth()
 {
    return this.Width ;
 }

 public void setWidth(  int Value )
 {
    this.Width = Value ;
 }

 public int getHeight()
 {
    return this.Height;
 }

 public void setHeight(  int Value )
 {
    this.Height = Value ;
 }

 public String getInternalname()
 {
    return this.Internalname ;
 }

 public void setInternalname(  String Value )
 {
    this.Internalname = Value ;
 }

 public String getBitmap()
 {
    return this.Bitmap;
 }

 public void setBitmap(  String Value )
 {
    this.Bitmap = Value ;
 }


 public String getTag()
 {
    return this.Tag ;
 }

 public void setTag(  String Value )
 {
    this.Tag = Value ;
 }

 public String getThemeClass()
 {
    return this.Class ;
 }

 public void setThemeClass(  String Value )
 {
    this.Class = Value ;
 }
 public String getHeaderrawhtml()
 {
    return this.Headerrawhtml ;
 }

 public void setHeaderrawhtml(  String Value )
 {
    this.Headerrawhtml = Value ;
 }

 public static void addResponsiveMetaHeaders(com.genexus.webpanels.HTMLChoice meta)
 {
    tryAddMetaHeader(meta, "viewport", "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no");
    tryAddMetaHeader(meta, "apple-mobile-web-app-capable", "yes");
 }

 private static void tryAddMetaHeader(com.genexus.webpanels.HTMLChoice meta, String key, String value)
 {
    if (!meta.existItem(key))
    {
      meta.addItem(key, value);
    }
 }
}
