package com.genexus.reports;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.platform.NativeFunctions;
import com.genexus.reports.fonts.PDFFont;
import com.genexus.reports.fonts.PDFFontDescriptor;
import com.genexus.reports.fonts.Type1FontMetrics;
import com.genexus.webpanels.HttpContextWeb;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.font.otf.Glyph;
import com.itextpdf.io.font.otf.GlyphLine;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.PdfViewerPreferences.PdfViewerPreferencesConstants;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants.LineCapStyle;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.properties.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.splitting.DefaultSplitCharacters;
import com.itextpdf.layout.Canvas;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.Deflater;

public class PDFReportItext8 extends GXReportPDFCommons {
	private PageSize pageSize;
	private PdfFont baseFont;
	private Barcode128 barcode = null;
	private Document document;
	private PdfDocument pdfDocument;
	private PdfPage pdfPage;
	private PdfWriter writer;
	private PdfFormXObject template;
	private PdfFont templateFont;
	public boolean lineCapProjectingSquare = true;
	public boolean barcode128AsImage = true;
	ConcurrentHashMap<String, Image> documentImages;

	static {
		log = org.apache.logging.log4j.LogManager.getLogger(PDFReportItext8.class);
	}

	public PDFReportItext8(ModelContext context) {
		super(context);
		document = null;
		pdfDocument = null;
		pageSize = null;
		documentImages = new ConcurrentHashMap<>();
	}

	protected void init() {
		try {
			writer = new PdfWriter(outputStream);
			writer.setCompressionLevel(Deflater.BEST_COMPRESSION);
			pdfDocument = new PdfDocument(writer);
			pdfDocument.setDefaultPageSize(this.pageSize);
			document = new Document(pdfDocument);
			document.setFontProvider(new DefaultFontProvider());
		} catch (Exception e){
			log.error("Failed to initialize new iText7 document: ", e);
		}
	}

	private void drawRectangle(PdfCanvas cb, float x, float y, float w, float h,
							   int styleTop, int styleBottom, int styleRight, int styleLeft,
							   float radioTL, float radioTR, float radioBL, float radioBR, float penAux, boolean hideCorners) {
		try {
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
				if (radioBL>0 && styleBottom!=STYLE_NONE_CONST)
				{
					cb.curveTo(x, y + radioBL * b, x + radioBL * b, y, x + radioBL, y);
				}
			}
			cb.stroke();
		} catch (Exception e) {
			log.error("drawRectangle failed: ", e);
		}
	}
	private void roundRectangle(PdfCanvas cb, float x, float y, float w, float h,
								float radioTL, float radioTR, float radioBL, float radioBR) {
		try {
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
		} catch (Exception e) {
			log.error("drawRectangle failed: ", e);
		}
	}

	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue,
						   int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR) {

		try {
			PdfCanvas cb = new PdfCanvas(pdfPage);

			float penAux = (float)convertScale(pen);
			float rightAux = (float)convertScale(right);
			float bottomAux = (float)convertScale(bottom);
			float leftAux = (float)convertScale(left);
			float topAux = (float)convertScale(top);

			cb.saveState();

			float x1, y1, x2, y2;
			x1 = leftAux + leftMargin;
			y1 = pageSize.getTop() - bottomAux - topMargin - bottomMargin;
			x2 = rightAux + leftMargin;
			y2 = pageSize.getTop() - topAux - topMargin - bottomMargin;

			cb.setLineWidth(penAux);
			cb.setLineCapStyle(LineCapStyle.PROJECTING_SQUARE);

			if (cornerRadioBL==0 && cornerRadioBR==0 && cornerRadioTL==0 && cornerRadioTR==0 && styleBottom==0 && styleLeft==0 && styleRight==0 && styleTop==0) {
				cb.setStrokeColorRgb(foreRed, foreGreen, foreBlue);

				Rectangle rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
				rect.increaseHeight((float) (fontSize * 0.15));
				rect.increaseWidth((float) (fontSize * 0.15));
				cb.rectangle(rect);

				if (backMode!=0) {
					cb.setFillColorRgb(backRed, backGreen, backBlue);
					cb.fill();
				} else {
					cb.stroke();
				}
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

				int max = (int)Math.min(w, h);
				cRadioTL = Math.max(0, Math.min(cRadioTL, max/2));
				cRadioTR = Math.max(0, Math.min(cRadioTR, max/2));
				cRadioBL = Math.max(0, Math.min(cRadioBL, max/2));
				cRadioBR = Math.max(0, Math.min(cRadioBR, max/2));

				if (backMode!=0) {
					cb.setFillColorRgb(backRed, backGreen, backBlue);
					cb.setStrokeColorRgb(foreRed, foreGreen, foreBlue);
					cb.setLineWidth(0);
					roundRectangle(cb, x1, y1, w, h,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR);
					cb.setFillColor(new DeviceRgb(new Color(backRed, backGreen, backBlue)));
					cb.fillStroke();
					cb.setLineWidth(penAux);
				}
				if (pen > 0) {
					cb.setFillColorRgb(foreRed, foreGreen, foreBlue);
					cb.setStrokeColorRgb(foreRed, foreGreen, foreBlue);
					drawRectangle(cb, x1, y1, w, h,
						styleTop, styleBottom, styleRight, styleLeft,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR, penAux, false);
				}
			}
			cb.restoreState();
		} catch (Exception e) {
			log.error("GxDrawRect failed: ", e);
		}

		log.debug("GxDrawRect -> (" + left + "," + top + ") - (" + right + "," + bottom + ")  BackMode: " + backMode + " Pen:" + pen);
	}

	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style) {
		try {
			PdfCanvas cb = new PdfCanvas(pdfPage);

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
			cb.setFillColorRgb(foreRed, foreGreen, foreBlue);
			cb.setLineWidth(widthAux);

			if (lineCapProjectingSquare) {
				cb.setLineCapStyle(LineCapStyle.PROJECTING_SQUARE);
			}
			if (style!=0) {
				float[] dashPattern = getDashedPattern(style);
				cb.setLineDash(dashPattern, 0);
			}
			cb.moveTo(x1, y1);
			cb.lineTo(x2, y2);
			cb.stroke();

			cb.restoreState();
		} catch (Exception e) {
			log.error("GxDrawLine failed:", e);
		}
	}

	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio) {
		try {
			ImageData imageData;
			try {
				if (documentImages != null && documentImages.containsKey(bitmap)) {
					imageData = ImageDataFactory.create(bitmap);
				}
				else {

					if (!NativeFunctions.isWindows() && new File(bitmap).isAbsolute() && bitmap.startsWith(httpContext.getStaticContentBase())) {
						bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
					}

					if (!new File(bitmap).isAbsolute() && !bitmap.toLowerCase().startsWith("http:") && !bitmap.toLowerCase().startsWith("https:")) {
						if (bitmap.startsWith(httpContext.getStaticContentBase())) {
							bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
						}
						imageData = ImageDataFactory.create(defaultRelativePrepend + bitmap);
						if(imageData == null) {
							bitmap = webAppDir + bitmap;
							imageData = ImageDataFactory.create(bitmap);
						}
						else {
							bitmap = defaultRelativePrepend + bitmap;
						}
					}
					else {
						imageData = ImageDataFactory.create(bitmap);
					}
				}
			}
			catch(java.lang.IllegalArgumentException ex) {
				URL url= new java.net.URL(bitmap);
				imageData = ImageDataFactory.create(url);
			}

			if (documentImages == null) {
				documentImages = new ConcurrentHashMap<>();
			}
			documentImages.putIfAbsent(bitmap, new Image(imageData));

			log.debug("GxDrawBitMap -> '" + bitmap + "' [" + left + "," + top + "] - Size: (" + (right - left) + "," + (bottom - top) + ")");

			if(imageData != null) {
				float rightAux = (float)convertScale(right);
				float bottomAux = (float)convertScale(bottom);
				float leftAux = (float)convertScale(left);
				float topAux = (float)convertScale(top);

				Image image = new Image(imageData);
				image.setFixedPosition(page,leftAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin);
				if (aspectRatio == 0)
					image.scaleAbsolute(rightAux - leftAux , bottomAux - topAux);
				else
					image.scaleToFit(rightAux - leftAux , bottomAux - topAux);
				document.add(image);
			}
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
		boolean embeddedFont = isEmbeddedFont(fontName);
		String originalFontName = fontName;
		if (!embeddedFont) {
			fontName = getSubstitute(fontName);
		}

		String fontSubstitute = "";
		if (!originalFontName.equals(fontName)) {
			fontSubstitute = "Original Font: " + originalFontName + " Substitute";
		}
		log.debug("GxAttris: ");
		log.debug("\\-> " + fontSubstitute + "Font: " + fontName + " (" + fontSize + ")" + (fontBold ? " BOLD" : "") + (fontItalic ? " ITALIC" : "") + (fontStrikethru ? " Strike" : ""));
		log.debug("\\-> Fore (" + foreRed + ", " + foreGreen + ", " + foreBlue + ")");
		log.debug("\\-> Back (" + backRed + ", " + backGreen + ", " + backBlue + ")");

		if (barcode128AsImage && fontName.toLowerCase().indexOf("barcode 128") >= 0 || fontName.toLowerCase().indexOf("barcode128") >= 0) {
			barcode = new Barcode128(pdfDocument);
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
					else if (fontItalic)
						style = style + 2;
					else if (fontBold)
						style = style + 1;
					for(int i=0;i<PDFFont.base14.length;i++) {
						if(PDFFont.base14[i][0].equalsIgnoreCase(fontName)) {
							fontName =  PDFFont.base14[i][1+style].substring(1);
							break;
						}
					}
					baseFont = PdfFontFactory.createFont(fontName, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_NOT_EMBEDDED);
				}
			}
			else {
				String fontPath = getFontLocation(fontName);
				if (fontPath.equals("")) {
					fontPath = PDFFontDescriptor.getTrueTypeFontLocation(fontName, props);
					if (fontPath.equals(""))
						baseFont = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_NOT_EMBEDDED);
					else
						baseFont = PdfFontFactory.createFont(fontPath, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_NOT_EMBEDDED);
				}
				else
					baseFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);
			}
		document.getFontProvider().addFont(baseFont.getFontProgram());
		}
		catch(IOException ioe) {
			log.error("GxAttris failed: ", ioe);
		}
	}

	public void setAsianFont(String fontName, String style) {
		try {
			if (fontName.equals("Japanese"))
				baseFont = PdfFontFactory.createFont("HeiseiMin-W3," + style, "UniJIS-UCS2-HW-H", EmbeddingStrategy.PREFER_EMBEDDED);
			if (fontName.equals("Japanese2"))
				baseFont = PdfFontFactory.createFont("HeiseiKakuGo-W5," + style, "UniJIS-UCS2-H", EmbeddingStrategy.PREFER_EMBEDDED);
			if (fontName.equals("SimplifiedChinese"))
				baseFont = PdfFontFactory.createFont("STSong-Light," + style, "UniGB-UCS2-H", EmbeddingStrategy.PREFER_EMBEDDED);
			if (fontName.equals("TraditionalChinese"))
				baseFont = PdfFontFactory.createFont("MHei-Medium," + style, "UniCNS-UCS2-H", EmbeddingStrategy.PREFER_EMBEDDED);
			if (fontName.equals("Korean"))
				baseFont = PdfFontFactory.createFont("HYSMyeongJo-Medium," + style, "UniKS-UCS2-H", EmbeddingStrategy.PREFER_EMBEDDED);
		}
		catch(IOException ioe) {
			log.error("setAsianFont failed: ", ioe);
		}
	}

	public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border, int valign) {
		boolean printRectangle = false;
		if (props.getBooleanGeneralProperty(Const.BACK_FILL_IN_CONTROLS, true))
			printRectangle = true;

		if (printRectangle && (border == 1 || backFill))
			GxDrawRect(left, top, right, bottom, border, foreColor.getRed(), foreColor.getGreen(), foreColor.getBlue(), backFill ? 1 : 0, backColor.getRed(), backColor.getGreen(), backColor.getBlue(), 0, 0);

		PdfCanvas cb = new PdfCanvas(pdfPage);
		sTxt = CommonUtil.rtrim(sTxt);

		PdfFont font = baseFont;
		cb.setFontAndSize(baseFont, fontSize);
		cb.setFillColor(new DeviceRgb(foreColor));
		float captionHeight = baseFont.getAscent(sTxt,fontSize) / 1000;
		float rectangleWidth = baseFont.getWidth(sTxt, fontSize);
		float lineHeight = (1 / 1000) * (baseFont.getAscent(sTxt,fontSize) - baseFont.getDescent(sTxt,fontSize)) + (fontSize * 1.2f);
		float textBlockHeight = (float)convertScale(bottom-top);
		int linesCount =   (int)(textBlockHeight/lineHeight);
		int bottomOri = bottom;
		int topOri = top;

		if (linesCount >= 2 && !((align & 16) == 16) && htmlformat != 1)
			if (valign == VerticalAlign.TOP.value())
				bottom = top + (int)reconvertScale(lineHeight);
			else if (valign == VerticalAlign.BOTTOM.value())
				top = bottom - (int)reconvertScale(lineHeight);

		float bottomAux = (float)convertScale(bottom) - ((float)convertScale(bottom-top) - captionHeight)/2;
		float topAux;

		float leftAux = (float)convertScale(left);
		float rightAux = (float)convertScale(right);
		int alignment = align & 3;
		boolean autoResize = (align & 256) == 256;

		if (htmlformat == 1) {
			log.info("As of now, you might experience unexpected behaviour since not all possible HTML code is supported");
			try {
				bottomAux = (float)convertScale(bottom);
				topAux = (float)convertScale(top);
				float drawingPageHeight = this.pageSize.getTop() - topMargin - bottomMargin;

				float llx = leftAux + leftMargin;
				float lly = drawingPageHeight - bottomAux;
				float urx = rightAux + leftMargin;
				float ury = drawingPageHeight - topAux;

				// Define the rectangle where the content will be displayed
				Rectangle htmlRectangle = new Rectangle(llx, lly, urx - llx, ury - lly);
				Canvas canvas = new Canvas(pdfDocument.getPage(page), htmlRectangle);
				YPosition yPosition = new YPosition(htmlRectangle.getTop());
				TextAlignment txtAlignment = getTextAlignment(alignment);

				ConverterProperties converterProperties = new ConverterProperties();
				converterProperties.setFontProvider(document.getFontProvider());
				//Iterate over the elements (a.k.a the parsed HTML string) and handle each case accordingly
				List<IElement> elements = HtmlConverter.convertToElements(sTxt, converterProperties);
				for (IElement element : elements)
					processHTMLElement(htmlRectangle, yPosition, txtAlignment, (IBlockElement) element);

			} catch (Exception e) {
				log.error("GxDrawText failed to print HTML text : ", e);
			}
		}
		else
		if (barcode != null) {
			log.debug("Barcode: --> " + barcode.getClass().getName());
			try {
				barcode.setCode(sTxt);
				barcode.setTextAlignment(alignment);
				Rectangle rectangle = new Rectangle(0, 0);
				switch (alignment) {
					case 1: // Center Alignment
						rectangle = new Rectangle((leftAux + rightAux) / 2 + leftMargin - rectangleWidth / 2,
							this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							Math.abs(((leftAux + rightAux) / 2 + leftMargin + rectangleWidth / 2) - ((leftAux + rightAux) / 2 + leftMargin - rectangleWidth / 2)) ,
							Math.abs((this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin) - (this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin)));
						break;
					case 2: // Right Alignment
						rectangle = new Rectangle(rightAux + leftMargin - rectangleWidth,
							this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							Math.abs((rightAux + leftMargin) - (rightAux + leftMargin - rectangleWidth)),
							Math.abs((this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin) - (this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin)));
						break;
					case 0: // Left Alignment
						rectangle = new Rectangle(leftAux + leftMargin,
							this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							Math.abs((leftAux + leftMargin + rectangleWidth) - (leftAux + leftMargin)),
							Math.abs((this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin) - (this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin)));
						break;
				}
				barcode.setAltText("");
				barcode.setBaseline(0);

				if (fontSize < Const.LARGE_FONT_SIZE)
					barcode.setX(Const.OPTIMAL_MINIMU_BAR_WIDTH_SMALL_FONT);
				else
					barcode.setX(Const.OPTIMAL_MINIMU_BAR_WIDTH_LARGE_FONT);

				Image imageCode = new Image(ImageDataFactory.create(barcode.createAwtImage(backFill ? backColor : null, foreColor), foreColor));
				imageCode.setFixedPosition(leftAux + leftMargin, rectangle.getBottom());
				barcode.setBarHeight(rectangle.getHeight());
				imageCode.scaleToFit(rectangle.getWidth(), rectangle.getHeight());
				document.add(imageCode);
			}
			catch (Exception ex) {
				log.error("GxDrawText: Error generating Barcode " + barcode.getClass().getName(), ex);
			}
		}
		else {

			if(sTxt.trim().equalsIgnoreCase("{{Pages}}")) {
				if (!templateCreated) {
					template = new PdfFormXObject(new Rectangle(right - left, bottom - top));
					templateCreated = true;
				}
				cb.addXObjectAt(template, leftAux + leftMargin, this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin);
				templateFont = baseFont;
				templateFontSize = fontSize;
				templateColorFill = foreColor;
				return;
			}

			float textBlockWidth = rightAux - leftAux;
			float TxtWidth = baseFont.getWidth(sTxt, fontSize);
			boolean justified = (alignment == 3) && textBlockWidth < TxtWidth;
			boolean wrap = ((align & 16) == 16);

			float leading = (float)(Double.valueOf(props.getGeneralProperty(Const.LEADING)).doubleValue());
			Style style = new Style();
			if (fontBold) style.setBold();
			if (fontItalic) style.setItalic();
			if (fontStrikethru) style.setUnderline(fontSize / 6, fontSize / 2);
			if (fontUnderline) style.setUnderline(fontSize / 6,0);
			style.setFont(font);
			style.setFontSize(fontSize);
			style.setFontColor(new DeviceRgb(foreColor));

			if (wrap || justified) {
				bottomAux = (float)convertScale(bottomOri);
				topAux = (float)convertScale(topOri);

				float llx = leftAux + leftMargin;
				float lly = this.pageSize.getTop() - bottomAux - topMargin - bottomMargin;
				float urx = rightAux + leftMargin;
				float ury = this.pageSize.getTop() - topAux - topMargin - bottomMargin;

				try{
					DrawTextColumn(llx, lly, urx, ury, sTxt, leading, valign, alignment, style, wrap);
				}
				catch (Exception ex) {
					log.error("Text wrap in GxDrawText failed: ", ex);
				}
			} else {
				try {
					if (!autoResize) {
						String newsTxt = sTxt;
						while(TxtWidth > textBlockWidth && (newsTxt.length()-1>=0)) {
							sTxt = newsTxt;
							newsTxt = newsTxt.substring(0, newsTxt.length()-1);
							TxtWidth = baseFont.getWidth(newsTxt, fontSize);
						}
					}

					Paragraph p = new Paragraph(sTxt);
					p.addStyle(style);

					switch(alignment) {
						case 1: // Center Alignment
							document.showTextAligned(p, ((leftAux + rightAux) / 2) + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin, page, TextAlignment.CENTER, VerticalAlignment.MIDDLE,0);
							break;
						case 2: // Right Alignment
							document.showTextAligned(p, rightAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin, page, TextAlignment.RIGHT, VerticalAlignment.MIDDLE,0);
							break;
						case 0: // Left Alignment
						case 3: // Justified, only one text line
							document.showTextAligned(p, leftAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin, page, TextAlignment.LEFT, VerticalAlignment.MIDDLE,0);
							break;
					}
				} catch (Exception e) {
					log.error("GxDrawText failed to draw simple text: ", e);
				}
			}
		}
	}

	void processHTMLElement(Rectangle htmlRectangle, YPosition currentYPosition, TextAlignment txtAlignment, IBlockElement blockElement){
		if (blockElement instanceof Paragraph){
			Paragraph p = (Paragraph) blockElement;
			float paragraphHeight = getBlockElementHeight(blockElement, htmlRectangle);
			p.setFixedPosition(page, htmlRectangle.getX(), currentYPosition.getCurrentYPosition() - paragraphHeight, htmlRectangle.getWidth());
			currentYPosition.setCurrentYPosition(currentYPosition.getCurrentYPosition() - paragraphHeight);
			document.add(p);
		} else if (blockElement instanceof Table){
			Table table = (Table) blockElement;
			float tableHeight = getBlockElementHeight(blockElement, htmlRectangle);
			table.setFixedPosition(page, htmlRectangle.getX(), currentYPosition.getCurrentYPosition() - tableHeight, htmlRectangle.getWidth());
			currentYPosition.setCurrentYPosition(currentYPosition.getCurrentYPosition() - tableHeight);
			document.add(table);
		} else if (blockElement instanceof com.itextpdf.layout.element.List){
			com.itextpdf.layout.element.List list = (com.itextpdf.layout.element.List) blockElement;
			float listHeight = getBlockElementHeight(blockElement, htmlRectangle);
			list.setFixedPosition(page, htmlRectangle.getX(),currentYPosition.getCurrentYPosition() - listHeight, htmlRectangle.getWidth());
			currentYPosition.setCurrentYPosition(currentYPosition.getCurrentYPosition() - listHeight);
			document.add(list);
		} else if (blockElement instanceof Div) {
			Div div = (Div) blockElement;
			// Iterate through the children of the Div and process each child element recursively
			for (IElement child : div.getChildren())
				if (child instanceof IBlockElement)
					processHTMLElement(htmlRectangle, currentYPosition, txtAlignment, (IBlockElement) child);
		}
	}

	private float getBlockElementHeight(IBlockElement blockElement, Rectangle htmlRectangle) throws RuntimeException{
		if (blockElement instanceof Paragraph){
			Paragraph p = (Paragraph) blockElement;
			return p.createRendererSubTree().setParent(document.getRenderer()).layout(new LayoutContext(new LayoutArea(page, htmlRectangle))).getOccupiedArea().getBBox().getHeight();
		} else if (blockElement instanceof Table){
			Table table = (Table) blockElement;
			return table.createRendererSubTree().setParent(document.getRenderer()).layout(new LayoutContext(new LayoutArea(page, htmlRectangle))).getOccupiedArea().getBBox().getHeight();
		} else if (blockElement instanceof com.itextpdf.layout.element.List){
			com.itextpdf.layout.element.List list = (com.itextpdf.layout.element.List) blockElement;
			return list.createRendererSubTree().setParent(document.getRenderer()).layout(new LayoutContext(new LayoutArea(page, htmlRectangle))).getOccupiedArea().getBBox().getHeight();
		}
		throw new RuntimeException("getBlockElementHeight failed to calculate the height of the block element");
	}

	private class YPosition {
		float currentYPosition;

		public YPosition(float currentYPosition) {
			this.currentYPosition = currentYPosition;
		}

		public float getCurrentYPosition() {
			return currentYPosition;
		}

		public void setCurrentYPosition(float currentYPosition) {
			this.currentYPosition = currentYPosition;
		}
	}

	boolean pageHeightExceeded(float bottomAux, float drawingPageHeight){
		return super.pageHeightExceeded(bottomAux,drawingPageHeight);
	}

	private TextAlignment getTextAlignment(int alignment){
		switch(alignment) {
			case 1: // Center Alignment
				return TextAlignment.CENTER;
			case 2: // Right Alignment
				return TextAlignment.RIGHT;
			case 0: // Left Alignment
			case 3: // Justified, only one text line
				return TextAlignment.LEFT;
			default:
				return TextAlignment.JUSTIFIED;
		}
	}

	void DrawTextColumn(float llx, float lly, float urx, float ury, String text, float leading, int valign, int alignment, Style style, boolean wrap){
		float y = lly;
		if (valign == VerticalAlign.MIDDLE.value())
			ury = ury - ((y - lly) / 2) + leading;
		else if (valign == VerticalAlign.BOTTOM.value())
			ury = ury - (y - lly- leading);
		else if (valign == VerticalAlign.TOP.value())
			ury = ury + leading/2;

		Paragraph p = new Paragraph(text);
		p.addStyle(style);
		TextAlignment txtAlignment = getTextAlignment(alignment);
		p.setTextAlignment(txtAlignment);
		if (wrap) {
			p.setProperty(Property.SPLIT_CHARACTERS, new CustomSplitCharacters());
			Table table = new Table(1);
			table.setFixedPosition(page,llx, lly, urx - llx);
			Cell cell = new Cell();
			cell.setWidth(urx - llx);
			cell.setHeight(ury - lly);
			cell.setBorder(Border.NO_BORDER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			cell.add(p);
			table.addCell(cell);
			document.add(table);
		} else
			document.showTextAligned(p, llx, lly, this.page, txtAlignment, VerticalAlignment.MIDDLE,0);
	}

	private static class CustomSplitCharacters extends DefaultSplitCharacters {
		@Override
		public boolean isSplitCharacter(GlyphLine text, int glyphPos) {
			if (!text.get(glyphPos).hasValidUnicode()) {
				return false;
			}
			boolean baseResult = super.isSplitCharacter(text, glyphPos);
			boolean myResult = false;
			Glyph glyph = text.get(glyphPos);
			if (glyph.getUnicode() == '_') {
				myResult = true;
			}
			return myResult || baseResult;
		}
	}

	public boolean GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) {
		boolean preResult = super.GxPrintInit(output, gxXPage, gxYPage, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);
		try {
			this.pageSize = computePageSize(leftMargin, topMargin, pageWidth, pageLength, props.getBooleanGeneralProperty(Const.MARGINS_INSIDE_BORDER, false));
			gxXPage[0] = (int)this.pageSize.getRight();
			if (props.getBooleanGeneralProperty(Const.FIX_SAC24437, true))
				gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y);
			else
				gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y_OLD);

			init();

			if (!preResult)
				return !preResult;
			else
				return true;
		} catch (Exception e){
			log.error("GxPrintInit faile" , e);
			return false;
		}
	}

	private PageSize computePageSize(float leftMargin, float topMargin, int width, int length, boolean marginsInsideBorder) {
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
				return new PageSize(new Rectangle((int)(width / PAGE_SCALE_X) , (int)(length / PAGE_SCALE_Y) ));
		}
		return new PageSize(new Rectangle((int)(width / PAGE_SCALE_X) + leftMargin, (int)(length / PAGE_SCALE_Y) + topMargin));
	}

	public void GxEndDocument() {
		if(pdfDocument.getNumberOfPages() == 0) {
			pdfPage = pdfDocument.addNewPage();
			pages++;
		}

		if (template != null) {
			PdfCanvas cb = new PdfCanvas(pdfPage);
			cb.beginText();
			cb.setFontAndSize(templateFont, templateFontSize);
			cb.setTextMatrix(0,0);
			cb.setFillColor(new DeviceRgb(templateColorFill));
			cb.showText(String.valueOf(pages));
			cb.endText();
		}
		int copies = 1;
		try {
			copies = Integer.parseInt(printerSettings.getProperty(form, Const.COPIES));
			log.debug("Setting number of copies to " + copies);
			PdfViewerPreferences viewerPreferences = new PdfViewerPreferences();
			viewerPreferences.setNumCopies(copies);

			int duplex= Integer.parseInt(printerSettings.getProperty(form, Const.DUPLEX));
			PdfViewerPreferencesConstants duplexValue;
			switch (duplex){
				case 1: duplexValue = PdfViewerPreferencesConstants.SIMPLEX; break;
				case 2:
				case 3:
					duplexValue = PdfViewerPreferencesConstants.DUPLEX_FLIP_SHORT_EDGE; break;
				case 4: duplexValue = PdfViewerPreferencesConstants.DUPLEX_FLIP_LONG_EDGE; break;
				default: duplexValue = PdfViewerPreferencesConstants.NONE;
			}
			log.debug("Setting duplex to " + duplexValue);
			viewerPreferences.setDuplex(duplexValue);
			pdfDocument.getCatalog().setViewerPreferences(viewerPreferences);
		}
		catch(Exception ex) {
			log.error("GxEndDocument failed to add viewer preferences: ", ex);
		}

		String serverPrinting = props.getGeneralProperty(Const.SERVER_PRINTING);
		boolean fit= props.getGeneralProperty(Const.ADJUST_TO_PAPER).equals("true");
		if ((outputType==Const.OUTPUT_PRINTER || outputType==Const.OUTPUT_STREAM_PRINTER) && (httpContext instanceof HttpContextWeb && serverPrinting.equals("false"))) {
			pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("var pp = this.getPrintParams();\n"));
			String printerAux=printerSettings.getProperty(form, Const.PRINTER);
			String printer = replace(printerAux, "\\", "\\\\");

			if (printer!=null && !printer.equals("")) {
				pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("pp.printerName = \"" + printer + "\";\n"));
			}
			if (fit) {
				pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("pp.pageHandling = pp.constants.handling.fit;\n"));
			}
			else {
				pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("pp.pageHandling = pp.constants.handling.none;\n"));
			}

			if (printerSettings.getProperty(form, Const.MODE, "3").startsWith("0")){
				pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("pp.interactive = pp.constants.interactionLevel.automatic;\n"));
				for(int i = 0; i < copies; i++) {
					pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("this.print(pp);\n"));
				}
			}
			else{
				pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("pp.interactive = pp.constants.interactionLevel.full;\n"));
				pdfDocument.getCatalog().setAdditionalAction(PdfName.WC, PdfAction.createJavaScript("this.print(pp);\n"));
			}
		}

		document.close();

		log.debug("GxEndDocument!");

		try{ props.save(); } catch(IOException e) { ; }

		switch(outputType) {
			case Const.OUTPUT_SCREEN:
				try{ outputStream.close(); } catch(IOException e) { ; }
				try{ showReport(docName, modal); } catch(Exception e) {
					log.error("GxEndDocument: failed to show report on screen ", e );
				}
				break;
			case Const.OUTPUT_PRINTER:
				try{ outputStream.close(); } catch(IOException e) { ; }
				try{
					if (!(httpContext instanceof HttpContextWeb) || !serverPrinting.equals("false")) {
						printReport(docName, this.printerOutputMode == 1);
					}
				} catch(Exception e){
					log.error("GxEndDocument: failed to show report ", e);
				}
				break;
			case Const.OUTPUT_FILE:
				try{ outputStream.close(); } catch(IOException e) { log.error("GxEndDocument: failed to save report to file ", e); }
				break;
			case Const.OUTPUT_STREAM:
			case Const.OUTPUT_STREAM_PRINTER:
			default: break;
		}
		outputStream = null;
	}

	public void GxStartPage() {
		pdfPage = pdfDocument.addNewPage();
		pages = pages +1;
	}

}
