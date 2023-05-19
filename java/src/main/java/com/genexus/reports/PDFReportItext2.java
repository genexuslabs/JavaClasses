package com.genexus.reports;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.platform.NativeFunctions;
import com.genexus.webpanels.HttpContextWeb;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.Barcode;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import com.genexus.reports.fonts.PDFFont;
import com.genexus.reports.fonts.PDFFontDescriptor;
import com.genexus.reports.fonts.Type1FontMetrics;

public class PDFReportItext2 extends GXReportPDFCommons
{
    private com.lowagie.text.Rectangle pageSize;   // Contiene las dimensiones de la página
	private BaseFont baseFont;
	private Barcode barcode = null;
    private Document document;
	private PdfWriter writer;
	private PdfTemplate template;
	private BaseFont templateFont;
	public boolean lineCapProjectingSquare = true;
	public boolean barcode128AsImage = true;
	ConcurrentHashMap<String, Image> documentImages;
	public int runDirection = PdfWriter.RUN_DIRECTION_LTR;

	public PDFReportItext2(ModelContext context) {
		super(context);
        document = null;
		pageSize = null;
		documentImages = new ConcurrentHashMap<>();
    }

	protected void init() {
      Document.compress = true;
	  try {
      writer = PdfWriter.getInstance(document, outputStream);
	  }
	  catch(DocumentException de) {
            log.error("Failed to initialize new iText2 document: ", de);
      }
      document.open();
    }
	
	/**
	 * @param hideCorners indica si se deben ocultar los triangulos de las esquinas cuando el lado que los une esta oculto.
	 */
	private void drawRectangle(PdfContentByte cb, float x, float y, float w, float h, 
		int styleTop, int styleBottom, int styleRight, int styleLeft, 
		float radioTL, float radioTR, float radioBL, float radioBR, float penAux, boolean hideCorners) {
	
		float[] dashPatternTop = getDashedPattern(styleTop);
		float[] dashPatternBottom = getDashedPattern(styleBottom);
		float[] dashPatternLeft = getDashedPattern(styleLeft);
		float[] dashPatternRight = getDashedPattern(styleRight);

		//-------------------bottom line---------------------
		if (styleBottom!=STYLE_NONE_CONST) {
			cb.setLineDash(dashPatternBottom, 0);
		}

		float b = 0.4477f;
		if (radioBL>0) {
			cb.moveTo(x + radioBL, y);
		}
		else {
			if (hideCorners && styleLeft==STYLE_NONE_CONST && radioBL==0) {
				cb.moveTo(x + penAux, y);
			}
			else {
				cb.moveTo(x, y);
			}
		}

		//-------------------bottom right corner---------------------

		if (styleBottom!=STYLE_NONE_CONST){ //si es null es Style None y no traza la linea

			if (hideCorners && styleRight==STYLE_NONE_CONST && radioBR==0) {
				cb.lineTo(x + w - penAux, y);
			}
			else {
				cb.lineTo(x + w - radioBR, y);
			}
			if (radioBR>0 && styleRight!=STYLE_NONE_CONST) {
				cb.curveTo(x + w - radioBR * b, y, x + w, y + radioBR * b, x + w, y + radioBR);
			}
		}

		//-------------------right line---------------------

		if (styleRight!=STYLE_NONE_CONST && dashPatternRight!=dashPatternBottom) {
			cb.stroke();
			cb.setLineDash(dashPatternRight, 0);
			if (hideCorners && styleBottom==STYLE_NONE_CONST && radioBR==0) {
				cb.moveTo(x + w, y + penAux);
			}
			else {
				cb.moveTo(x + w, y + radioBR);
			}
		}

		//-------------------top right corner---------------------
		if (styleRight!=STYLE_NONE_CONST) {
			if (hideCorners && styleTop==STYLE_NONE_CONST && radioTR==0) {
				cb.lineTo (x + w, y + h - penAux);
			}
			else {
				cb.lineTo (x + w, y + h - radioTR);
			}
			if (radioTR>0 && styleTop!=STYLE_NONE_CONST) {
				cb.curveTo(x + w, y + h - radioTR * b, x + w - radioTR * b, y + h, x + w - radioTR, y + h);
			}
		}

		//-------------------top line---------------------

		if (styleTop!=STYLE_NONE_CONST && dashPatternTop!=dashPatternRight) {
			cb.stroke();
			cb.setLineDash(dashPatternTop, 0);
			if (hideCorners && styleRight==STYLE_NONE_CONST && radioTR==0) {
				cb.moveTo(x + w - penAux, y + h);
			}
			else {
				cb.moveTo(x + w - radioTR, y + h);
			}
		}

		//-------------------top left corner---------------------
		if (styleTop!=STYLE_NONE_CONST) {
			if (hideCorners && styleLeft==STYLE_NONE_CONST && radioTL==0) {
				cb.lineTo(x + penAux, y + h);
			}
			else {
				cb.lineTo(x + radioTL, y + h);
			}
			if (radioTL>0 && styleLeft!=STYLE_NONE_CONST) {
				cb.curveTo(x + radioTL * b, y + h, x, y + h - radioTL * b, x, y + h - radioTL);
			}
		}
            
		//-------------------left line---------------------

		if (styleLeft!=STYLE_NONE_CONST  && dashPatternLeft!=dashPatternTop) {
			cb.stroke();
			cb.setLineDash(dashPatternLeft, 0);
			if (hideCorners && styleTop==STYLE_NONE_CONST && radioTL==0) {
				cb.moveTo(x, y + h - penAux);
			}
			else {
				cb.moveTo(x, y + h - radioTL);
			}
		}
            
		//-------------------bottom left corner---------------------
		if (styleLeft!=STYLE_NONE_CONST) {
			if (hideCorners && styleBottom==STYLE_NONE_CONST && radioBL==0) {
				cb.lineTo(x, y + penAux);
			}
			else {
				cb.lineTo(x, y + radioBL);
			}
			if (radioBL>0 && styleBottom!=STYLE_NONE_CONST) {
				cb.curveTo(x, y + radioBL * b, x + radioBL * b, y, x + radioBL, y);
			}
		}
		cb.stroke();
            
	}
	private void roundRectangle(PdfContentByte cb, float x, float y, float w, float h, 
		float radioTL, float radioTR, float radioBL, float radioBR) {

		//-------------------bottom line---------------------

		float b = 0.4477f;
		if (radioBL>0) {
			cb.moveTo(x + radioBL, y);
		}
		else {
			cb.moveTo(x, y);
		}

		//-------------------bottom right corner---------------------

		cb.lineTo(x + w - radioBR, y);
		if (radioBR>0) {
			cb.curveTo(x + w - radioBR * b, y, x + w, y + radioBR * b, x + w, y + radioBR);
		}
		

		cb.lineTo (x + w, y + h - radioTR);
		if (radioTR>0) {
			cb.curveTo(x + w, y + h - radioTR * b, x + w - radioTR * b, y + h, x + w - radioTR, y + h);
		}

		cb.lineTo(x + radioTL, y + h);
		if (radioTL>0) {
			cb.curveTo(x + radioTL * b, y + h, x, y + h - radioTL * b, x, y + h - radioTL);
		}
		cb.lineTo(x, y + radioBL);
		if (radioBL>0) {
			cb.curveTo(x, y + radioBL * b, x + radioBL * b, y, x + radioBL, y);
		}
            
	}

	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, 
		int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR) {

		PdfContentByte cb = writer.getDirectContent();

		float penAux = (float)convertScale(pen);
		float rightAux = (float)convertScale(right);
		float bottomAux = (float)convertScale(bottom);
		float leftAux = (float)convertScale(left);
		float topAux = (float)convertScale(top);
		
		cb.saveState();
		
		float x1, y1, x2, y2;
		x1 = leftAux + leftMargin;
		y1 = pageSize.getTop() - bottomAux - topMargin -bottomMargin;
		x2 = rightAux + leftMargin;
		y2 = pageSize.getTop() - topAux - topMargin -bottomMargin;

		cb.setLineWidth(penAux);		
		cb.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE);

		if (cornerRadioBL==0 && cornerRadioBR==0 && cornerRadioTL==0 && cornerRadioTR==0 && styleBottom==0 && styleLeft==0 && styleRight==0 && styleTop==0) {
			//Tengo que hacer eso para que el borde quede del mismo color que el fill si se indica que no se quiere borde,
			//porque no funciona el setLineWidth
			if (pen > 0)
				cb.setRGBColorStroke(foreRed, foreGreen, foreBlue);
			else
				cb.setRGBColorStroke(backRed, backGreen, backBlue);

			cb.rectangle(x1, y1, x2 - x1, y2 - y1);

			if (backMode!=0) {
				cb.setColorFill(new Color(backRed, backGreen, backBlue));
				cb.fillStroke();
			}
			cb.closePathStroke();
		}
		else {
			float w = x2 - x1;
			float h = y2 - y1;
			if (w < 0) {
				x1 += w;
				w = -w;
			}
			if (h < 0) {
				y1 += h;
				h = -h;
			}

			float cRadioTL = (float)convertScale(cornerRadioTL);
			float cRadioTR = (float)convertScale(cornerRadioTR);
			float cRadioBL = (float)convertScale(cornerRadioBL);
			float cRadioBR = (float)convertScale(cornerRadioBR);

			// Scale the radius if it's too large or to small to fit.
			int max = (int)Math.min(w, h);
			cRadioTL = Math.max(0, Math.min(cRadioTL, max/2));
			cRadioTR = Math.max(0, Math.min(cRadioTR, max/2));
			cRadioBL = Math.max(0, Math.min(cRadioBL, max/2));
			cRadioBR = Math.max(0, Math.min(cRadioBR, max/2));

			if (backMode!=0) {
				//Interior del rectangulo
				cb.setRGBColorStroke(backRed, backGreen, backBlue);
				cb.setLineWidth(0);		
					roundRectangle(cb, x1, y1, w, h,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR);
				cb.setColorFill(new Color(backRed, backGreen, backBlue));
				cb.fillStroke();
				cb.setLineWidth(penAux);		
			}
			if (pen > 0) {
				//Bordes del rectangulo
				cb.setRGBColorStroke(foreRed, foreGreen, foreBlue);
					drawRectangle(cb, x1, y1, w, h, 
						styleTop, styleBottom, styleRight, styleLeft,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR, penAux, false);
			}
		}
		cb.restoreState();

		log.debug("GxDrawRect -> (" + left + "," + top + ") - (" + right + "," + bottom + ")  BackMode: " + backMode + " Pen:" + pen);
    }
	
    public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style) {
		PdfContentByte cb = writer.getDirectContent();

		float widthAux = (float)convertScale(width);
		float rightAux = (float)convertScale(right);
		float bottomAux = (float)convertScale(bottom);
		float leftAux = (float)convertScale(left);
		float topAux = (float)convertScale(top);

		log.debug("GxDrawLine -> (" + left + "," + top + ") - (" + right + "," + bottom + ") Width: " + width);

		float x1, y1, x2, y2;

		x1 = leftAux + leftMargin;
		y1 = pageSize.getTop() - bottomAux - topMargin -bottomMargin;
		x2 = rightAux + leftMargin;
		y2 = pageSize.getTop() - topAux - topMargin -bottomMargin;

		cb.saveState();
		cb.setRGBColorStroke(foreRed, foreGreen, foreBlue);
		cb.setLineWidth(widthAux);

		if (lineCapProjectingSquare) {
			cb.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE); //Hace que lineas de width 10 por ejemplo que forman una esquina no quedan igual que en disenio porque "rellena" la esquina.
		}
		if (style!=0) {
			float[] dashPattern = getDashedPattern(style);
			cb.setLineDash(dashPattern, 0);
		}
		cb.moveTo(x1, y1);
		cb.lineTo(x2, y2);			
		cb.stroke();
			
		cb.restoreState();
    }
	
    public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio) {
		try {
			com.lowagie.text.Image image;
			try {
				if (documentImages != null && documentImages.containsKey(bitmap)) {
					image = documentImages.get(bitmap);
				}
				else {

					if (!NativeFunctions.isWindows() && new File(bitmap).isAbsolute() && bitmap.startsWith(httpContext.getStaticContentBase())) {
						bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
					}				
				
					if (!new File(bitmap).isAbsolute() && !bitmap.toLowerCase().startsWith("http:") && !bitmap.toLowerCase().startsWith("https:")) {
						if (bitmap.startsWith(httpContext.getStaticContentBase())) {
							bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
						}					
						// Si la ruta a la imagen NO es absoluta, en aplicaciones Web le agregamos al comienzo la ruta al root de la aplicación
						// más la staticContentBaseURL si ésta es relativa.
						image = com.lowagie.text.Image.getInstance(defaultRelativePrepend + bitmap);
						if(image == null) { // Si all\uFFFDEno se encuentra la imagen, entonces la buscamos bajo el webAppDir (para mantener compatibilidad)
							bitmap = webAppDir + bitmap;
							image = com.lowagie.text.Image.getInstance(bitmap);
						}
						else {
							bitmap = defaultRelativePrepend + bitmap;
						}
					}
					else {
						image = com.lowagie.text.Image.getInstance(bitmap);
					}
				}
			}
			catch(java.lang.IllegalArgumentException ex) {//Puede ser una url absoluta
				java.net.URL url= new java.net.URL(bitmap);
				image = com.lowagie.text.Image.getInstance(url);
			}

			if (documentImages == null) {
				documentImages = new ConcurrentHashMap<>();
			}
			documentImages.putIfAbsent(bitmap, image);

			log.debug("GxDrawBitMap -> '" + bitmap + "' [" + left + "," + top + "] - Size: (" + (right - left) + "," + (bottom - top) + ")");

	        if(image != null) { // Si la imagen NO se encuentra, no hago nada
				float rightAux = (float)convertScale(right);
				float bottomAux = (float)convertScale(bottom);
				float leftAux = (float)convertScale(left);
				float topAux = (float)convertScale(top);

				image.setAbsolutePosition(leftAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin);
				if (aspectRatio == 0)
					image.scaleAbsolute(rightAux - leftAux , bottomAux - topAux);
				else
					image.scaleToFit(rightAux - leftAux , bottomAux - topAux);
				PdfContentByte cb = writer.getDirectContent();
				cb.addImage(image);
			}
		}
		catch(DocumentException de) {
			log.error("GxDrawBitMap failed:", de);
		}
		catch(IOException ioe) {
			log.error("GxDrawBitMap failed:", ioe);
		}
		catch(Exception e) {
			log.error("GxDrawBitMap failed:", e);
		}
    }

    public void GxAttris(String fontName, int fontSize, boolean fontBold, boolean fontItalic, boolean fontUnderline, boolean fontStrikethru, int Pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue) {
		boolean isCJK = false;
		boolean embeedFont = isEmbeddedFont(fontName);
		String originalFontName = fontName;
		if (!embeedFont) {
			fontName = getSubstitute(fontName); // Veo si hay substitutos solo si el font no va a ir embebido
		}

		String fontSubstitute = "";
		if (!originalFontName.equals(fontName)) {
			fontSubstitute = "Original Font: " + originalFontName + " Substitute";
		}
		log.debug("GxAttris: ");
		log.debug("\\-> " + fontSubstitute + "Font: " + fontName + " (" + fontSize + ")" + (fontBold ? " BOLD" : "") + (fontItalic ? " ITALIC" : "") + (fontStrikethru ? " Strike" : ""));
		log.debug("\\-> Fore (" + foreRed + ", " + foreGreen + ", " + foreBlue + ")");log.debug("\\-> Back (" + backRed + ", " + backGreen + ", " + backBlue + ")");

		if (barcode128AsImage && fontName.toLowerCase().indexOf("barcode 128") >= 0 || fontName.toLowerCase().indexOf("barcode128") >= 0) {
			barcode = new Barcode128();
			barcode.setCodeType(Barcode128.CODE128);
		}
		else {
			barcode = null;
		}
		this.fontUnderline = fontUnderline;        
		this.fontStrikethru = fontStrikethru;        
        this.fontSize = fontSize;
        this.fontBold = fontBold;
        this.fontItalic = fontItalic;
		foreColor = new Color(foreRed, foreGreen, foreBlue);
        backColor = new Color(backRed, backGreen, backBlue);
		
		backFill = (backMode != 0);
		try {
			if (PDFFont.isType1(fontName)) {
				//Me fijo si es un Asian font
				for(int i = 0; i < Type1FontMetrics.CJKNames.length; i++) {
					if(Type1FontMetrics.CJKNames[i][0].equalsIgnoreCase(fontName) ||
					   Type1FontMetrics.CJKNames[i][1].equalsIgnoreCase(fontName)) {
						String style = "";
						if (fontBold && fontItalic)
							style = "BoldItalic";
						else {
							if (fontItalic)
								style = "Italic";
							if (fontBold)
								style = "Bold";
						}
						setAsianFont(fontName, style);
						isCJK = true;
						break;
					}
				}
				if (!isCJK) {
					int style = 0;
					if (fontBold && fontItalic)
						style = style + 3;
					else {
						if (fontItalic)
							style = style + 2;
						if (fontBold)
							style = style + 1;
					}
					for(int i=0;i<PDFFont.base14.length;i++) {
						if(PDFFont.base14[i][0].equalsIgnoreCase(fontName)) {
							fontName =  PDFFont.base14[i][1+style].substring(1);
							break;
						}
					}
					baseFont = BaseFont.createFont(fontName, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
				}
			}
			else {//Si el Font es true type
				String style = "";
				if (fontBold && fontItalic)
					style = ",BoldItalic";
				else {
					if (fontItalic)
						style = ",Italic";
					if (fontBold)
						style = ",Bold";
				}

				fontName = fontName + style;
				String fontPath = getFontLocation(fontName);
				boolean foundFont = true;
				if (fontPath.equals("")) {
					fontPath = PDFFontDescriptor.getTrueTypeFontLocation(fontName, props);
					if (fontPath.equals("")) {
						baseFont = BaseFont.createFont("Helvetica", BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
						foundFont = false;
					}
				}
				if (foundFont) {
					if (isEmbeddedFont(fontName)) {
						baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
					}
					else {//No se embebe el font
						baseFont = BaseFont.createFont(fontPath + style, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
					}
				}
			}
		}
		catch(DocumentException de) {
			log.error("GxAttris failed: ", de);
        }
        catch(IOException ioe) {
			log.error("GxAttris failed: ", ioe);
        }
    }

	public void setAsianFont(String fontName, String style) {
		try {
			if (style.equals("")) {
				if (fontName.equals("Japanese"))
					baseFont = BaseFont.createFont("HeiseiMin-W3", "UniJIS-UCS2-HW-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Japanese2"))
					baseFont = BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("SimplifiedChinese"))
					baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("TraditionalChinese"))
					baseFont = BaseFont.createFont("MHei-Medium", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Korean"))
					baseFont = BaseFont.createFont("HYSMyeongJo-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
			}
			else {
				if (fontName.equals("Japanese"))
					baseFont = BaseFont.createFont("HeiseiMin-W3," + style, "UniJIS-UCS2-HW-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Japanese2"))
					baseFont = BaseFont.createFont("HeiseiKakuGo-W5," + style, "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("SimplifiedChinese"))
					baseFont = BaseFont.createFont("STSong-Light," + style, "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("TraditionalChinese"))
					baseFont = BaseFont.createFont("MHei-Medium," + style, "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Korean"))
					baseFont = BaseFont.createFont("HYSMyeongJo-Medium," + style, "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
			}
		}
		catch(DocumentException de) {
			log.error("setAsianFont failed: ", de);
        }
        catch(IOException ioe) {
			log.error("setAsianFont failed: ", ioe);
        }
	}
    public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border, int valign) {
    	boolean printRectangle = false;
    	if (props.getBooleanGeneralProperty(Const.BACK_FILL_IN_CONTROLS, true))
    		printRectangle = true;
    		
    	if (printRectangle && (border == 1 || backFill)) {
    		GxDrawRect(left, top, right, bottom, border, foreColor.getRed(), foreColor.getGreen(), foreColor.getBlue(), backFill ? 1 : 0, backColor.getRed(), backColor.getGreen(), backColor.getBlue(), 0, 0);
    	}    	
		
        PdfContentByte cb = writer.getDirectContent();
		sTxt = CommonUtil.rtrim(sTxt);
		
		Font font = new Font(baseFont, fontSize);
		cb.setFontAndSize(baseFont, fontSize);
		cb.setColorFill(foreColor);
		int arabicOptions = 0;
		float captionHeight = 	baseFont.getFontDescriptor(baseFont.CAPHEIGHT, fontSize);
		float rectangleWidth = baseFont.getWidthPoint(sTxt, fontSize); 
		float lineHeight = baseFont.getFontDescriptor(baseFont.BBOXURY, fontSize) - baseFont.getFontDescriptor(baseFont.BBOXLLY, fontSize);
		float textBlockHeight = (float)convertScale(bottom-top);
		int linesCount =   (int)(textBlockHeight/lineHeight);
		int bottomOri = bottom;
		int topOri = top;
		
		//Si se tiene un campo con mas de una linea y no tiene wrap, no es justify, y no es html, se simula que el campo tiene una sola linea
		//asignando al bottom el top mas el lineHeight
		if (linesCount >= 2 && !((align & 16) == 16) && htmlformat != 1) {
			if (valign == VerticalAlign.TOP.value())
				bottom = top + (int)reconvertScale(lineHeight);
			else if (valign == VerticalAlign.BOTTOM.value())
				top = bottom - (int)reconvertScale(lineHeight);
		}

		float bottomAux = (float)convertScale(bottom) - ((float)convertScale(bottom-top) - captionHeight)/2;   
		//Al bottom de los textos se le resta espacio entre el texto y el borde del textblock, 
		//porque en el reporte genexus la x,y de un
		//text es la x,y del cuadro que contiene el texto, y la api de itext espera la x,y del texto en si.
		//Generalmente el cuadro es mas grande que lo que ocupa el texto realmente (depende del tipo de font)
		//captionHeight esta convertido, bottom y top no.
		float topAux = (float)convertScale(top) + ((float)convertScale(bottom-top) - captionHeight)/2;   


		float startHeight = bottomAux - topAux - captionHeight;
		
		float leftAux = (float)convertScale(left);
		float rightAux = (float)convertScale(right);
		int alignment = align & 3;
		boolean autoResize = (align & 256) == 256;
		
        if (htmlformat == 1) {
            StyleSheet styles = new StyleSheet();
			Hashtable locations = getFontLocations();
			for (Enumeration e = locations.keys(); e.hasMoreElements() ;) {
				String fontName = (String)e.nextElement();
				String fontPath = (String)locations.get(fontName);
				if (fontPath.equals("")) {
					fontPath = PDFFontDescriptor.getTrueTypeFontLocation(fontName, props);
				}
				if (!fontPath.equals("")) {
					FontFactory.register(fontPath, fontName);
					styles.loadTagStyle("body", "face", fontName);

					if (isEmbeddedFont(fontName)){
						styles.loadTagStyle("body", "encoding", BaseFont.IDENTITY_H);
					}
					else{
						styles.loadTagStyle("body", "encoding", BaseFont.WINANSI);
					}
				}
			}

            //Bottom y top son los absolutos, sin considerar la altura real a la que se escriben las letras.
            bottomAux = (float)convertScale(bottom);
            topAux = (float)convertScale(top);

            ColumnText Col = new ColumnText(cb);
			int colAlignment = columnAlignment(alignment);
			if (colAlignment!=0)
				Col.setAlignment(colAlignment);
			ColumnText simulationCol = new ColumnText(null);
			float drawingPageHeight = (float)this.pageSize.getTop() - topMargin - bottomMargin;
			Col.setSimpleColumn(leftAux + leftMargin,drawingPageHeight - bottomAux,rightAux + leftMargin,drawingPageHeight - topAux);
			simulationCol.setSimpleColumn(leftAux + leftMargin,drawingPageHeight - bottomAux,rightAux + leftMargin,drawingPageHeight - topAux);

            try {
                ArrayList objects = HTMLWorker.parseToList(new StringReader(sTxt), styles);
                for (int k = 0; k < objects.size(); ++k) {
						if (pageHeightExceeded(bottomAux, drawingPageHeight)) {
							simulationCol.addElement((Element)objects.get(k));
							simulationCol.go(true);

							if (simulationCol.getYLine() < bottomMargin) {
								GxEndPage();
								GxStartPage();
								simulationCol = new ColumnText(null);
								simulationCol.setSimpleColumn(leftAux + leftMargin,drawingPageHeight - bottomAux,rightAux + leftMargin,drawingPageHeight - topAux);
								simulationCol.addElement((Element)objects.get(k));

								Col = new ColumnText(cb);
								if (colAlignment!=0)
									Col.setAlignment(colAlignment);
								Col.setSimpleColumn(leftAux + leftMargin,drawingPageHeight - bottomAux,rightAux + leftMargin,drawingPageHeight - topAux);

								bottomAux = bottomAux - drawingPageHeight;
							}
						}
						if (objects.get(k) instanceof Paragraph && colAlignment!=0)
							((Paragraph)objects.get(k)).setAlignment(colAlignment);

						Col.addElement((Element)objects.get(k));
						Col.go();
				}
			}catch(Exception de){
				log.error("ERROR printing HTML text ", de.getMessage());
			}
		}
		else 
		if (barcode!=null) {
			log.debug("Barcode: --> " + barcode.getClass().getName());
			try {
				barcode.setCode(sTxt);
				barcode.setTextAlignment(alignment);
				com.lowagie.text.Rectangle rectangle = new com.lowagie.text.Rectangle(0, 0);
				//El rectangulo tiene tamaño ok.
				switch (alignment) {
					case 1: // Center Alignment
						rectangle = new com.lowagie.text.Rectangle((leftAux + rightAux) / 2 + leftMargin - rectangleWidth / 2,
							(float)this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							(leftAux + rightAux) / 2 + leftMargin + rectangleWidth / 2,
							(float)this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin);
						break;
					case 2: // Right Alignment
						rectangle = new com.lowagie.text.Rectangle(rightAux + leftMargin - rectangleWidth,
							(float)this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							rightAux + leftMargin,
							(float)this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin);
						break;
					case 0: // Left Alignment
						rectangle = new com.lowagie.text.Rectangle(leftAux + leftMargin,
							(float)this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							leftAux + leftMargin + rectangleWidth,
							(float)this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin);
						break;
				}
				barcode.setAltText("");
				barcode.setBaseline(0);

				if (fontSize < Const.LARGE_FONT_SIZE)
					barcode.setX(Const.OPTIMAL_MINIMU_BAR_WIDTH_SMALL_FONT);
				else
					barcode.setX(Const.OPTIMAL_MINIMU_BAR_WIDTH_LARGE_FONT);

				Image imageCode = barcode.createImageWithBarcode(cb, backFill ? backColor : null, foreColor);
				imageCode.setAbsolutePosition(leftAux + leftMargin, rectangle.getBottom());
				barcode.setBarHeight(rectangle.getHeight());
				imageCode.scaleToFit(rectangle.getWidth(), rectangle.getHeight());
				document.add(imageCode);
			}
			catch (Exception ex) {
				log.error("GxDrawText: Error generating Barcode " + barcode.getClass().getName(), ex);
			}
		}
		else {

			if(backFill) {
				com.lowagie.text.Rectangle rectangle = new com.lowagie.text.Rectangle(0,0);
				//Si el texto tiene background lo dibujo de esta forma
				switch(alignment) {
				case 1: // Center Alignment
					rectangle = new com.lowagie.text.Rectangle((leftAux + rightAux)/2 + leftMargin - rectangleWidth/2, (float)this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin , (leftAux + rightAux)/2 + leftMargin + rectangleWidth/2, (float)this.pageSize.getTop() - topAux - topMargin -bottomMargin);				
					break;
				case 2: // Right Alignment
					rectangle = new com.lowagie.text.Rectangle(rightAux + leftMargin - rectangleWidth, (float)this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin , rightAux + leftMargin, (float)this.pageSize.getTop() - topAux - topMargin -bottomMargin);				
					break;
				case 0: // Left Alignment
					rectangle = new com.lowagie.text.Rectangle(leftAux + leftMargin, (float)this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin , leftAux + leftMargin + rectangleWidth, (float)this.pageSize.getTop() - topAux - topMargin -bottomMargin);				
					break;
				}
				rectangle.setBackgroundColor(backColor);
				try {
					document.add(rectangle);
				}
				catch(DocumentException de) {
					log.error("backfill in GxDrawText failed: ", de);
				}
			}
			
			float underlineSeparation = lineHeight / 5;//Separacion entre el texto y la linea del subrayado
			int underlineHeight = (int)underlineSeparation + (int)(underlineSeparation/4);
			com.lowagie.text.Rectangle underline;
			
			//Si el texto esta subrayado
			if (fontUnderline) {
				underline = new com.lowagie.text.Rectangle(0,0);
				
				switch(alignment) {
				case 1: // Center Alignment
						underline = new com.lowagie.text.Rectangle( 
							(leftAux + rightAux)/2 + leftMargin - rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation, 
							(leftAux + rightAux)/2 + leftMargin + rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight);				
					break;
				case 2: // Right Alignment
						underline = new com.lowagie.text.Rectangle( rightAux + leftMargin - rectangleWidth , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation, 
							rightAux + leftMargin, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);				
					break;
				case 0: // Left Alignment
						underline = new com.lowagie.text.Rectangle( leftAux + leftMargin , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation, 
							leftAux + leftMargin + rectangleWidth, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);				
					break;
				}
				underline.setBackgroundColor(foreColor);
				try {
					document.add(underline);
				}
				catch(DocumentException de) {
					log.error("fontUnderline in GxDrawText failed: ", de);
				}
			}
			
			//Si el texto esta tachado
			if (fontStrikethru) {
				underline = new com.lowagie.text.Rectangle(0,0);
				float strikethruSeparation = lineHeight / 2;
				
				switch(alignment)
				{
				case 1: // Center Alignment
						underline = new com.lowagie.text.Rectangle( 
							(leftAux + rightAux)/2 + leftMargin - rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation, 
							(leftAux + rightAux)/2 + leftMargin + rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);				
					break;
				 case 2: // Right Alignment
						underline = new com.lowagie.text.Rectangle( rightAux + leftMargin - rectangleWidth , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation, 
							rightAux + leftMargin, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);				
					break;
				case 0: // Left Alignment
						underline = new com.lowagie.text.Rectangle( leftAux + leftMargin , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation, 
							leftAux + leftMargin + rectangleWidth, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);
					break;
				}
				underline.setBackgroundColor(foreColor);
				try {
					document.add(underline);
				}
				catch(DocumentException de) {
					log.error("fontStrikethru in GxDrawText failed: ", de);
				}
			}
			
			if(sTxt.trim().equalsIgnoreCase("{{Pages}}")) {// Si el texto es la cantidad de páginas del documento
				if (!templateCreated) {
					template = cb.createTemplate(right - left, bottom - top);
					templateCreated = true;
				}
				cb.addTemplate(template, leftAux + leftMargin, this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin);
				templateFont = baseFont;
				templateFontSize = fontSize;
				templateColorFill = foreColor;
				return;
			}

			float textBlockWidth = rightAux - leftAux;
			float TxtWidth = baseFont.getWidthPoint(sTxt, fontSize);
			boolean justified = (alignment == 3) && textBlockWidth < TxtWidth;
			boolean wrap = ((align & 16) == 16);

			//Justified
            if (wrap || justified) {
                //Bottom y top son los absolutos, sin considerar la altura real a la que se escriben las letras.
                bottomAux = (float)convertScale(bottomOri);
                topAux = (float)convertScale(topOri);

                //La constante 2 para LEADING indica la separacion que se deja entre un renglon y otro. (es lo que mas se asemeja a la api vieja).
                float leading = (float)(Double.valueOf(props.getGeneralProperty(Const.LEADING)).doubleValue());
                Paragraph p = new Paragraph(sTxt, font);

				float llx = leftAux + leftMargin;
				float lly = (float)this.pageSize.getTop() - bottomAux - topMargin - bottomMargin;
				float urx = rightAux + leftMargin;
				float ury = (float)this.pageSize.getTop() - topAux - topMargin - bottomMargin;

                try{
					DrawColumnText(cb, llx, lly, urx, ury, p, leading, runDirection, valign, alignment);
                }
			    catch (DocumentException ex) {
					log.error("wrap in GxDrawText failed: ", ex);
			    }
            }
			else{ //no wrap
				startHeight=0;
				if (!autoResize) {
					//Va quitando el ultimo char del texto hasta que llega a un string cuyo ancho se pasa solo por un caracter
					//del ancho del textblock ("se pasa solo por un caracter": esto es porque en el caso general es ese el texto que
					//mas se parece a lo que se disenia en genexus).
					String newsTxt = sTxt;
					while(TxtWidth > textBlockWidth && (newsTxt.length()-1>=0)) {
						sTxt = newsTxt;
						newsTxt = newsTxt.substring(0, newsTxt.length()-1);
						TxtWidth = baseFont.getWidthPoint(newsTxt, fontSize);
					}
				}

				Phrase phrase = new Phrase(sTxt, font);
				switch(alignment) {
				case 1: // Center Alignment
					ColumnText.showTextAligned(cb, cb.ALIGN_CENTER, phrase, ((leftAux + rightAux) / 2) + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin + startHeight, 0, runDirection, arabicOptions);
					break;
				case 2: // Right Alignment
					ColumnText.showTextAligned(cb, cb.ALIGN_RIGHT, phrase, rightAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin + startHeight, 0, runDirection, arabicOptions);
					break;
				case 0: // Left Alignment
				case 3: // Justified, only one text line
					ColumnText.showTextAligned(cb, cb.ALIGN_LEFT, phrase, leftAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin + startHeight, 0, runDirection, arabicOptions);
					break;
				}
			}
		}
    }
	boolean pageHeightExceeded(float bottomAux, float drawingPageHeight){
		return super.pageHeightExceeded(bottomAux,drawingPageHeight);
	}
	ColumnText SimulateDrawColumnText(PdfContentByte cb, Rectangle rect, Paragraph p, float leading, int runDirection, int alignment) throws DocumentException {
		ColumnText Col = new ColumnText(cb);
		Col.setRunDirection(runDirection);
		Col.setAlignment(alignment);
		Col.setLeading(leading, 1);
		Col.setSimpleColumn(rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop());
		Col.addText(p);
		Col.go(true);
		return Col;
	}
	void DrawColumnText(PdfContentByte cb, float llx, float lly, float urx, float ury, Paragraph p, float leading, int runDirection, int valign, int alignment) throws DocumentException {
		Rectangle rect = new Rectangle(llx, lly, urx, ury);
		ColumnText ct = SimulateDrawColumnText(cb, rect, p, leading, runDirection, alignment);//add the column in simulation mode
		float y = ct.getYLine();
		int linesCount = ct.getLinesWritten();

		//calculate a new rectangle for valign = middle 
		if (valign == VerticalAlign.MIDDLE.value())
			ury = ury - ((y - lly) / 2) + leading;
		else if (valign == VerticalAlign.BOTTOM.value())
			ury = ury - (y - lly- leading);
		else if (valign == VerticalAlign.TOP.value())
			ury = ury + leading/2;

		rect = new Rectangle(llx, lly, urx, ury); //Rectangle for new ury

		ColumnText Col = new ColumnText(cb);
		Col.setRunDirection(runDirection);
		if (linesCount <= 1)
			Col.setLeading(0, 1);
		else
			Col.setLeading(leading, 1);
		Col.setSimpleColumn(rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop());

		Col.setAlignment(columnAlignment(alignment));
		Col.addText(p);
		Col.go();
	}
	private int columnAlignment(int alignment) {
		if (alignment == Element.ALIGN_JUSTIFIED)
			return justifiedType;
		else
			return alignment;
	}

    public boolean GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) {
		boolean preResult = super.GxPrintInit(output, gxXPage, gxYPage, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);

		runDirection = Integer.valueOf(props.getGeneralProperty(Const.RUN_DIRECTION)).intValue();

		if (props.getBooleanGeneralProperty(Const.JUSTIFIED_TYPE_ALL, false))
			justifiedType = Element.ALIGN_JUSTIFIED_ALL;
		else
			justifiedType = Element.ALIGN_JUSTIFIED;

		//Se ignora el parametro orientation para el calculo del pageSize, los valores de alto y ancho ya vienen invertidos si Orientation=2=landscape.
        this.pageSize = computePageSize(leftMargin, topMargin, pageWidth, pageLength, props.getBooleanGeneralProperty(Const.MARGINS_INSIDE_BORDER, false));
        gxXPage[0] = (int)this.pageSize.getRight();
		if (props.getBooleanGeneralProperty(Const.FIX_SAC24437, true))
			gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y); // Cuanto menor sea GX_PAGE_SCALE_Y, GeneXus imprime mayor parte de cada hoja
		else
			gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y_OLD); // Cuanto menor sea GX_PAGE_SCALE_Y, GeneXus imprime mayor parte de cada hoja

		document = new Document(this.pageSize,0,0,0,0);

        init();

		if (!preResult)
			return !preResult;
		else
			return true;
    }

	private com.lowagie.text.Rectangle computePageSize(float leftMargin, float topMargin, int width, int length, boolean marginsInsideBorder) {
		if ((leftMargin == 0 && topMargin == 0)||marginsInsideBorder) {
			if (length == 23818 && width == 16834)
				return PageSize.A3;
			else if (length == 16834 && width == 11909)
				return PageSize.A4;
			else if (length == 11909 && width == 8395)
				return PageSize.A5;
			else if (length == 20016 && width == 5731)
				return PageSize.B4;
			else if (length == 14170 && width == 9979)
				return PageSize.B5;
			else if (length == 15120 && width == 10440)
				return PageSize.EXECUTIVE;
			else if (length == 20160 && width == 12240)
				return PageSize.LEGAL;
			else if (length == 15840 && width == 12240)
				return PageSize.LETTER;
			else
				return new com.lowagie.text.Rectangle((int)(width / PAGE_SCALE_X) , (int)(length / PAGE_SCALE_Y) );
		}
		return new com.lowagie.text.Rectangle((int)(width / PAGE_SCALE_X) + leftMargin, (int)(length / PAGE_SCALE_Y) + topMargin);
	}

    public void GxEndDocument() {
      if(document.getPageNumber() == 0) { // Si no hay ninguna página en el documento, agrego una vacia}
        writer.setPageEmpty(false);
      }

        //Ahora proceso los comandos GeneXus {{Pages}}
	    if (template != null) {
	  		template.beginText();
			template.setFontAndSize(templateFont, templateFontSize);
			template.setTextMatrix(0,0);
			template.setColorFill(templateColorFill);
			template.showText(String.valueOf(pages));
			template.endText();
		}
		int copies = 1;
		try {
			copies = Integer.parseInt(printerSettings.getProperty(form, Const.COPIES));
			log.debug("Setting number of copies to " + copies);
			writer.addViewerPreference(PdfName.NUMCOPIES, new PdfNumber(copies));

			int duplex= Integer.parseInt(printerSettings.getProperty(form, Const.DUPLEX));
			PdfName duplexValue;
			switch (duplex){
				case 1: duplexValue = PdfName.SIMPLEX; break;
				case 2: duplexValue = PdfName.DUPLEX; break;
				case 3: duplexValue = PdfName.DUPLEXFLIPSHORTEDGE;break;
				case 4: duplexValue = PdfName.DUPLEXFLIPLONGEDGE;break;
				default: duplexValue = PdfName.NONE;
			}
			log.debug("Setting duplex to " + duplexValue.toString());
			writer.addViewerPreference(PdfName.DUPLEX, duplexValue);
		}
		catch(Exception ex) {
			log.error("GxEndDocument failed to apply viewer preferences: ", ex);
		}

		String serverPrinting = props.getGeneralProperty(Const.SERVER_PRINTING);
		boolean fit= props.getGeneralProperty(Const.ADJUST_TO_PAPER).equals("true");
		if ((outputType==Const.OUTPUT_PRINTER || outputType==Const.OUTPUT_STREAM_PRINTER) && (httpContext instanceof HttpContextWeb && serverPrinting.equals("false"))) {
				writer.addJavaScript("var pp = this.getPrintParams();\n");
				String printerAux=printerSettings.getProperty(form, Const.PRINTER);
				String printer = replace(printerAux, "\\", "\\\\");

				if (printer!=null && !printer.equals("")) {
	                writer.addJavaScript("pp.printerName = \"" + printer + "\";\n");
				}
			if (fit) {
				writer.addJavaScript("pp.pageHandling = pp.constants.handling.fit;\n");
			}
			else {
				writer.addJavaScript("pp.pageHandling = pp.constants.handling.none;\n");
			}

				if (printerSettings.getProperty(form, Const.MODE, "3").startsWith("0")){//Show printer dialog Never
					writer.addJavaScript("pp.interactive = pp.constants.interactionLevel.automatic;\n");
					//No print dialog is displayed. During printing a progress monitor and cancel
					//dialog is displayed and removed automatically when printing is complete.
					for(int i = 0; i < copies; i++) {
						writer.addJavaScript("this.print(pp);\n");
					}
				}
				else{ //Show printer dialog is sent directly to printer | always
					writer.addJavaScript("pp.interactive = pp.constants.interactionLevel.full;\n");
					//Displays the print dialog allowing the user to change print settings and requiring
					//the user to press OK to continue. During printing a progress monitor and cancel
					//dialog is displayed and removed automatically when printing is complete.
					writer.addJavaScript("this.print(pp);\n");
				}
				
		}

		document.close();

        log.debug("GxEndDocument!");

		try{ props.save(); } catch(IOException e) { ; }

        // OK, ahora que ya terminamos el PDF, vemos si tenemos que mostrarlo en pantalla
        switch(outputType) {
        case Const.OUTPUT_SCREEN:
            try{ outputStream.close(); } catch(IOException e) { ; } // Cierro el archivo
            try{ showReport(docName, modal); } catch(Exception e) { // Si no se puede mostrar el reporte
                e.printStackTrace();
            }
//  Comento la próxima lú‹ea, porque por manejo interno del Acrobat, si ya habú} una instancia del
//  Acrobat corriendo, el modal no funciona (x que el proceso levantado le avisa al que ya estaba abierto qu\uFFFDE
//  archivo abrir y luego se mata automáticamente)
//            if(modal)if(new File(docName).delete())TemporaryFiles.getInstance().removeFileFromList(docName); // Intento eliminar el docName aqu\uFFFDE
            break;
        case Const.OUTPUT_PRINTER:
            try{ outputStream.close(); } catch(IOException e) { ; } // Cierro el archivo
            try{
				if (!(httpContext instanceof HttpContextWeb) || !serverPrinting.equals("false")) {
					printReport(docName, this.printerOutputMode == 1); 
				}
			} catch(Exception e){ // Si no se puede mostrar el reporte
               	log.error("GxEndDocument: failed to show report ", e);
            }
            break;
        case Const.OUTPUT_FILE:
            try{ outputStream.close(); } catch(IOException e) { log.error("GxEndDocument: failed to save report to file ", e); } // Cierro el archivo
            break;
        case Const.OUTPUT_STREAM:
		case Const.OUTPUT_STREAM_PRINTER:
        default: break;
        }
        outputStream = null;
    }
    public void GxStartPage() {
		boolean ret = document.newPage();
		pages = pages +1;
	}

}
