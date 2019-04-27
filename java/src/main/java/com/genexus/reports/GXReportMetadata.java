package com.genexus.reports;

import java.util.Hashtable;

import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.xml.XMLReader;

public class GXReportMetadata 
{
  private Hashtable hash = new Hashtable();
  private Hashtable attriHash = new Hashtable();
  private String fileName;
  private IReportHandler reportHandler;

  public GXReportMetadata(String fileName, IReportHandler reportHandler) 
  {	  	
	  this.fileName = com.genexus.Application.getClientContext().getClientPreferences().getPRINT_LAYOUT_METADATA_DIR() + fileName + ".rpt";
	  this.reportHandler = reportHandler;
	  
	  if(ModelContext.getModelContext() != null)
	  {
		  HttpContext webContext = (HttpContext) ModelContext.getModelContext().getHttpContext();
		  if( (webContext != null) && (webContext instanceof HttpContextWeb))
		  {
			  this.fileName = ( (HttpContextWeb) webContext).getRealPath(this.fileName);
		  }
	  }	  
  }
  
  public void load()
  {
		XMLReader reader = new XMLReader();
		reader.open(fileName);
		if(reader.getErrCode() != 0)
		{
			System.out.println("ERROR1");
			System.err.println("Error opening metadata file: " + fileName);
			return;
		}		
		
		while(reader.readType(1, "PrintBlock") >0)
		{
			processPrintBlock(reader);
		}
		reader.close();
  }
  
  private void processPrintBlock(XMLReader reader)
  {
	  short result;
	  result = reader.read(); //Read <Controls>
	  result = reader.read();
	  while(!(reader.getName().equals("PrintBlock") && (reader.getNodeType() == 2)) && result!=0)
	  {
		  if(reader.getName().equals("ReportLabel") && (reader.getNodeType() == 1) && (reader.getIsSimple() == 0))
		  {
			  processReportLabel(reader, 0);
		  }
		  if(reader.getName().equals("ReportAttribute") && (reader.getNodeType() == 1) && (reader.getIsSimple() == 0))
		  {
			  processReportLabel(reader, 1);
		  }
		  if(reader.getName().equals("ReportLine") && (reader.getNodeType() == 1) && (reader.getIsSimple() == 0))
		  {
			  processReportLine(reader);
		  }
		  if(reader.getName().equals("ReportRectangle") && (reader.getNodeType() == 1) && (reader.getIsSimple() == 0))
		  {
			  processReportRectangle(reader);
		  }
		  if(reader.getName().equals("ReportImage") && (reader.getNodeType() == 1) && (reader.getIsSimple() == 0))
		  {
			  processReportImage(reader);
		  }
			if (reader.getName().equals("Properties") && (reader.getNodeType() == 1) && (reader.getIsSimple() == 0)) 
			{
				processProperties(reader);
			}		  		  
		  result = reader.read();
	  }
  }
  
  private void processReportLabel(XMLReader reader, int type)
  {
	  short result;
	  Integer key;
	  String sTxt;
	  int left;
	  int top;
	  int right;
	  int width;
	  int defaultWidth;
	  int bottom;
	  int align;
	  int valign;
	  int htmlformat = 0;
	  int border = 0;
	  String aligment;
	  RGB foreColor;
	  int backMode = 1;
	  RGB backColor;
	  String fontInfo;
	  boolean visible;
	  boolean wordwrap;
	  
	  result = reader.readType(1, "RPT_ID");
	  key = new Integer(reader.getValue());
	  
	  result = reader.readType(1, "RPT_VISIBLE");
	  visible = reader.getValue().equals("True");	  
	  
	  result = reader.readType(1, "RPT_TEXT");
	  sTxt = reader.getValue();
	  
	  result = reader.readType(1, "RPT_X");
	  left = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_Y");
	  top = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_WIDTH");
	  width = new Integer(reader.getValue()).intValue();
	  right = left + width;
	  
	  if (type == 0)
	  {
	  	defaultWidth = width;
	  }
	  else
	  {
	  	result = reader.readType(1, "RPT_WIDTH_Default");
	  	defaultWidth = new Integer(reader.getValue()).intValue();	  
	  }
	  	  
	  result = reader.readType(1, "RPT_HEIGHT");
	  bottom = top + new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_FORECOLOR");
	  foreColor = parseRGB(reader.getValue());	  
	  
	  result = reader.readType(1, "RPT_BACKCOLOR");
	  backColor = parseRGB(reader.getValue());	  
	  
	  if (reader.getValue().equals("Transparent, ARGB(0,255,255,255)"))
	  {
		  backMode = 0;
	  }	  
	  
	  result = reader.readType(1, "RPT_BORDERS");
	  border = reader.getValue().equals("None")? 0: 1;
	  
	  result = reader.readType(1, "RPT_ALIGNMENT");
	  aligment = reader.getValue();
	  valign = parseVerticalAlignment(aligment);
	  
	  result = reader.readType(1, "RPT_FONT");
	  fontInfo = reader.getValue();
	  
 	  if (type == 1) //Attribute
	  {
		  result = reader.readType(1, "GxFormat");
		  if (result > 0 && reader.getValue().indexOf("HTML") >=0)
			htmlformat = 1;
	  }
	  
	  result = reader.readType(1, "RPT_WORDWRAP");
	  wordwrap = reader.getValue().equals("True");
	  align =  parseAlignment(aligment, wordwrap, width, defaultWidth);	  

	  parseAttris(key, fontInfo, foreColor, backMode, backColor);
	  
	  if (visible)
	  {
		DrawText dt;
		if (type == 0)
		{
			  result = reader.readType(2, "ReportLabel");
			  dt = new DrawText(sTxt, left, top, right, bottom, align, valign, htmlformat, border);
		}
		else
		{
			  result = reader.readType(2, "ReportAttribute");
			  dt = new DrawText(null, left, top, right, bottom, align, valign, htmlformat, border);
		}
		hash.put(key, dt);
	  }
  }
  
  private void processReportLine(XMLReader reader)  
  {
	  short result;
	  Integer key;	  
	  int left;
	  int top;
	  int right;
	  int bottom;
	  int widht;
	  RGB rgb;
	  int style;
	  boolean visible;
	  
	  result = reader.readType(1, "RPT_ID");
	  key = new Integer(reader.getValue());
	  
	  result = reader.readType(1, "RPT_VISIBLE");
	  visible = reader.getValue().equals("True");	  
	  
	  result = reader.readType(1, "RPT_X");
	  left = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_Y");
	  top = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_WIDTH");
	  right = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_HEIGHT");
	  bottom = new Integer(reader.getValue()).intValue();	  
	  
	  result = reader.readType(1, "RPT_FORECOLOR");
	  rgb = parseRGB(reader.getValue());
	  
	  result = reader.readType(1, "RPT_LINEDIRECTION");
	  if (reader.getValue().equals("Horizontal"))
	  {
		  right = left + right;
		  bottom = top;
	  }
	  else
	  {
		  right = left;
		  bottom = top + bottom;
	  }
	  
	  result = reader.readType(1, "RPT_LINEWIDTH");
	  widht = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(2, "ReportLine");
	  
	  if (visible)
	  {
		DrawLine dl = new DrawLine(left, top, right, bottom, widht, rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 0);	  
		hash.put(key, dl);
	  }
  }  
  
  private void processReportRectangle(XMLReader reader)  
  {
	  short result;
	  Integer key;
	  int left;
	  int top;
	  int right;
	  int bottom;
	  int pen;
	  int backMode = 1;
	  RGB ForeRgb;
	  RGB BackRgb;
	  boolean visible;
	  
	  result = reader.readType(1, "RPT_ID");
	  key = new Integer(reader.getValue());
	  
	  result = reader.readType(1, "RPT_VISIBLE");
	  visible = reader.getValue().equals("True");	  
	  
	  result = reader.readType(1, "RPT_X");
	  left = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_Y");
	  top = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_WIDTH");
	  right = left + new Integer(reader.getValue()).intValue();
	  	  
	  result = reader.readType(1, "RPT_HEIGHT");
	  bottom = top + new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_BACKCOLOR");
	  if (reader.getValue().equals("Transparent, ARGB(0,255,255,255)"))
	  {
		  backMode = 0;
	  }
	  BackRgb = parseRGB(reader.getValue());	  
	  
	  result = reader.readType(1, "RPT_BORDERWIDTH");
	  pen = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_BORDERCOLOR");
	  ForeRgb = parseRGB(reader.getValue());	  
	  
	  result = reader.readType(2, "ReportRectangle");
	  
	  if (visible)
	  {	  
		DrawRect dr = new DrawRect(left, top, right, bottom, pen, ForeRgb.getRed(), ForeRgb.getGreen(), ForeRgb.getBlue(), backMode, BackRgb.getRed(), BackRgb.getGreen(), BackRgb.getBlue(), 0, 0, 0, 0, 0, 0, 0, 0);	  
		hash.put(key, dr);	  
	  }
  }
  
  private void processReportImage(XMLReader reader)  
  {
	  short result;
	  Integer key;
	  int left;
	  int top;
	  int right;
	  int bottom;
	  boolean visible;
	  
	  result = reader.readType(1, "RPT_ID");
	  key = new Integer(reader.getValue());
	  
	  result = reader.readType(1, "RPT_VISIBLE");
	  visible = reader.getValue().equals("True");	  
	  
	  result = reader.readType(1, "RPT_X");
	  left = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_Y");
	  top = new Integer(reader.getValue()).intValue();
	  
	  result = reader.readType(1, "RPT_WIDTH");
	  right = left + new Integer(reader.getValue()).intValue();
	  	  
	  result = reader.readType(1, "RPT_HEIGHT");
	  bottom = top + new Integer(reader.getValue()).intValue();	  
	  
	  result = reader.readType(2, "ReportImage");
	  
	  if (visible)
	  {	  
		DrawText dt = new DrawText(null, left, top, right, bottom, 0, 0);
		hash.put(key, dt);	  
	  }
  }
  
  private void processProperties(XMLReader reader)  
  {
	  short result;
	  Integer key;
	  int height;
	  boolean visible;
	  
	  result = reader.readType(1, "RPT_ID");
	  key = new Integer(reader.getValue());
	  
	  result = reader.readType(1, "RPT_VISIBLE");
	  visible = reader.getValue().equals("True");	  
	  	  
	  result = reader.readType(1, "RPT_HEIGHT");
	  height = new Integer(reader.getValue()).intValue();	  
	  
	  if (visible)
	  {	  
	  	PrintblockProperties dt = new PrintblockProperties(height);
			hash.put(key, dt);	  
	  }
  }  
  
  private int parseAlignment(String txt, boolean wordwrap, int width, int defaultWidth)
  {
	  if (txt.endsWith("Right"))
	  {
		  if (wordwrap)
		  {
			  return 2+16;
		  }
		  if (width != defaultWidth)
		  {
		  	return 2;
		  }
		  return 2+256;
	  }	  
	  if (txt.endsWith("Center"))
	  {
		  if (wordwrap)
		  {
			  return 1+16;
		  }
		  if (width != defaultWidth)
		  {
		  	return 1;
		  }
		  return 1+256;
	  }
	  if (txt.endsWith("Justify"))
	  {
		  if (wordwrap)
		  {
			  return 3+16;
		  }
		  if (width != defaultWidth)
		  {
		  	return 3;
		  }
		  return 3+256;
	  }	  
	  if (wordwrap)
	  {
		  return 0+16;
	  }
	  if (width != defaultWidth)
	  {
	  	return 0;
	  }	   
	  return 0+256;
  }
  
  private int parseVerticalAlignment(String txt)
  {
	  if (txt.startsWith("Top"))
	  {
		  return 0;
	  }
	  if (txt.startsWith("Middle"))
	  {
		  return 1;
	  }
	  return 2;
  }

  private RGB parseRGB(String txt)
  {
	  String[] txts = txt.substring(0, txt.length() -1).split(",");
	  RGB rgb = new RGB(new Integer(txts[2]).intValue(), new Integer(txts[3]).intValue(), new Integer(txts[4]).intValue());
	  return rgb;
  }
  
  private void parseAttris(Integer key, String fontInfo, RGB foreColor, int backMode, RGB backColor)
  {
	  String fontName;
	  int fontSize;
	  boolean fontBold = false;
	  boolean fontItalic = false;
	  boolean fontUnderline = false;
	  boolean fontStrikethru = false;
	  
	  String[] fontInfos = fontInfo.split(",", 3);
	  fontName = fontInfos[0];
	  
	  float aux = Float.parseFloat(fontInfos[1].trim().replace("pt", ""));
	  fontSize = Math.round(aux);
	  
	  if (fontInfos.length > 2)
	  {
		if (fontInfos[2].indexOf("Bold") != -1)
		{
			fontBold = true;
		}
		if (fontInfos[2].indexOf("Italic") != -1)
		{
			fontItalic = true;
		}
		if (fontInfos[2].indexOf("Underline") != -1)
		{
			fontUnderline = true;
		}
		if (fontInfos[2].indexOf("Strikeout") != -1)
		{
			fontStrikethru = true;
		}
	  }
	  
	  Attris atts = new Attris(fontName, fontSize, fontBold, fontItalic, fontUnderline, fontStrikethru, 0, foreColor, backMode, backColor);
	  attriHash.put(key, atts);
  }
  
  public void GxDrawText(int printBlock, int controlId, int Gx_line)
  {
	  Integer key = new Integer(controlId);
	  Attris att = (Attris)attriHash.get(key);
	  if (att != null)
	  {
		reportHandler.GxAttris(att.fontName, att.fontSize, att.fontBold, att.fontItalic, att.fontUnderline, att.fontStrikethru, 0, att.foreColor.getRed(), att.foreColor.getGreen(), att.foreColor.getBlue(), att.backMode, att.backColor.getRed(), att.backColor.getGreen(), att.backColor.getBlue()) ;	  		  
	  }
	  DrawText dt = (DrawText)hash.get(key);
	  if (dt != null)
	  {
		reportHandler.GxDrawText(dt.sTxt, dt.left, Gx_line + dt.top, dt.right, Gx_line + dt.bottom, dt.align, dt.htmlformat, dt.border, dt.valign);
	  }
  }
  
  public void GxDrawText(int printBlock, int controlId, int Gx_line, String value)
  {
	  Integer key = new Integer(controlId);
	  Attris att = (Attris)attriHash.get(key);
	  if (att != null)
	  {
		reportHandler.GxAttris(att.fontName, att.fontSize, att.fontBold, att.fontItalic, att.fontUnderline, att.fontStrikethru, 0, att.foreColor.getRed(), att.foreColor.getGreen(), att.foreColor.getBlue(), att.backMode, att.backColor.getRed(), att.backColor.getGreen(), att.backColor.getBlue()) ;	  		  
	  }	  
	  DrawText dt = (DrawText)hash.get(key);
	  if (dt != null)
	  {	  
		reportHandler.GxDrawText(value, dt.left, Gx_line + dt.top, dt.right, Gx_line + dt.bottom, dt.align, dt.htmlformat, dt.border, dt.valign);
	  }
  }
  
  public void GxDrawLine(int printBlock, int controlId, int Gx_line)
  {
	  Integer key = new Integer(controlId);
	  DrawLine dl = (DrawLine)hash.get(key);
	  if (dl != null)
	  {	  
		reportHandler.GxDrawLine(dl.left, Gx_line + dl.top, dl.right, Gx_line + dl.bottom, dl.widht, dl.foreRed, dl.foreGreen, dl.foreBlue, dl.style);
	  }
  }
  
   public void GxDrawRect(int printBlock, int controlId, int Gx_line)
  {
	  Integer key = new Integer(controlId);
	  DrawRect dr = (DrawRect)hash.get(key);
	  if (dr != null)
	  {	  
		reportHandler.GxDrawRect(dr.left, Gx_line + dr.top, dr.right, Gx_line + dr.bottom, dr.pen, dr.foreRed, dr.foreGreen, dr.foreBlue, dr.backMode, dr.backRed, dr.backGreen, dr.backBlue, dr.styleTop, dr.styleBottom, dr.styleRight, dr.styleLeft, dr.cornerRadioTL, dr.cornerRadioTR, dr.cornerRadioBL, dr.cornerRadioBR);
	  }
  }
   
  public void GxDrawBitMap(int printBlock, int controlId, int Gx_line, String value, int aspectRatio)
  {
	  Integer key = new Integer(controlId);
	  DrawText db = (DrawText)hash.get(key);
	  if (db != null)
	  {	  
		reportHandler.GxDrawBitMap(value, db.left, Gx_line + db.top, db.right, Gx_line + db.bottom, aspectRatio);
	  }
  }
  
  public int GxDrawGetPrintBlockHeight(int printBlock)
  {
  	Integer key = new Integer(printBlock);
  	PrintblockProperties db = (PrintblockProperties)hash.get(key);
  	return db.height; 
  }
  
	class DrawText
	{
		String sTxt;
		int left;
		int top;
		int right;
		int bottom;
		int align;
		int valign;		
		int htmlformat;
		int border;
		public DrawText(String sTxt, int left, int top, int right, int bottom, int align, int valign, int htmlformat, int border)
		{
			this.border = border;
			this.htmlformat = htmlformat;
			this.sTxt = sTxt;
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.align = align;
			this.valign = valign;
		}
		public DrawText(String sTxt, int left, int top, int right, int bottom, int align, int valign)
		{
			this.sTxt = sTxt;
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.align = align;
			this.valign = valign;
		}
	}
	
	class DrawLine
	{
		int left;
		int top;
		int right;
		int bottom;
		int widht;
		int foreRed;
		int foreGreen;		
		int foreBlue;		
		int style;		

		public DrawLine(int left, int top, int right, int bottom, int widht, int foreRed, int foreGreen, int foreBlue, int style)
		{
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.widht = widht;
			this.foreRed = foreRed;
			this.foreGreen = foreGreen;
			this.foreBlue = foreBlue;
			this.style = style;
		}
	}
	
	class DrawRect
	{
		int left;
		int top;
		int right;
		int bottom;
		int pen;
		int foreRed;
		int foreGreen;		
		int foreBlue;
		int backMode;
		int backRed;
		int backGreen;		
		int backBlue;		
		int styleTop; 
		int styleBottom; 
		int styleRight; 
		int styleLeft; 
		int cornerRadioTL; 
		int cornerRadioTR; 
		int cornerRadioBL; 
		int cornerRadioBR;

		public DrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR)
		{
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.pen = pen;
			this.foreRed = foreRed;
			this.foreGreen = foreGreen;
			this.foreBlue = foreBlue;			
			this.backMode = backMode;
			this.backRed = backRed;
			this.backGreen = backGreen;
			this.backBlue = backBlue;			
			this.styleTop = styleTop; 
			this.styleBottom = styleBottom; 
			this.styleRight = styleRight; 
			this.styleLeft = styleLeft; 
			this.cornerRadioTL = cornerRadioTL; 
			this.cornerRadioTR = cornerRadioTR; 
			this.cornerRadioBL = cornerRadioBL; 
			this.cornerRadioBR = cornerRadioBR;
		}
	}

	class DrawBitMap
	{
		int left;
		int top;
		int right;
		int bottom;		

		public DrawBitMap(int left, int top, int right, int bottom)
		{
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}
	}
	
	class RGB
	{
		int foreRed;
		int foreGreen;		
		int foreBlue;
		
		public RGB(int foreRed, int foreGreen, int foreBlue)
		{
			this.foreRed = foreRed;
			this.foreGreen = foreGreen;		
			this.foreBlue = foreBlue;			
		}
		
		public int getRed()
		{
			return this.foreRed;
		}
		
		public int getGreen()
		{
			return this.foreGreen;
		}
		
		public int getBlue()
		{
			return this.foreBlue;
		}		
	}
	
	class Attris
	{
		String fontName;
		int fontSize;
		boolean fontBold;
		boolean fontItalic;
		boolean fontUnderline;
		boolean fontStrikethru; 
		int Pen;
		RGB foreColor;
		int backMode;
		RGB backColor;
		
		public Attris(String fontName, int fontSize, boolean fontBold, boolean fontItalic, boolean fontUnderline, boolean fontStrikethru, int Pen, RGB foreColor, int backMode, RGB backColor)
		{
			this.fontName = fontName;
			this.fontSize = fontSize;
			this.fontBold = fontBold;
			this.fontItalic = fontItalic;
			this.fontUnderline = fontUnderline;
			this.fontStrikethru = fontStrikethru; 
			this.Pen = Pen;
			this.foreColor = foreColor;
			this.backMode = backMode;
			this.backColor = backColor;			
		}
	}
	
	class PrintblockProperties
	{
		int height;		

		public PrintblockProperties(int height)
		{
			this.height = height;
		}
	}	
}
