package com.genexus.reports;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.platform.NativeFunctions;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.reports.fonts.PDFFont;
import com.genexus.reports.fonts.PDFFontDescriptor;
import com.genexus.reports.fonts.Type1FontMetrics;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.util.Matrix;

public class PDFReportPDFBox extends GXReportPDFCommons{
	private PDRectangle pageSize;
	private PDFont baseFont;
	private String baseFontName;
	private BitMatrix barcode = null;
	private String barcodeType = null;
	private PDDocument document;
	private PDDocumentCatalog writer;
	private PDPageContentStream template;
	private PDFormXObject formXObjecttemplate;
	private PDType0Font templateFont;
	public boolean lineCapProjectingSquare = true;
	public boolean barcode128AsImage = true;
	ConcurrentHashMap<String, PDImageXObject> documentImages;
	public int runDirection = 0;
	private int page;

	static {
		log = org.apache.logging.log4j.LogManager.getLogger(PDFReportPDFBox.class);
	}

	public PDFReportPDFBox(ModelContext context) {
		super(context);
		document = null;
		pageSize = null;
		documentImages = new ConcurrentHashMap<>();
	}

	protected void init() {
		try {
			document = new PDDocument();
		}
		catch(Exception e) {
			log.error("Failed to initialize new PDFBox document: ", e);
		}
	}

	private void drawRectangle(PDPageContentStream cb, float x, float y, float w, float h,
							   int styleTop, int styleBottom, int styleRight, int styleLeft,
							   float radioTL, float radioTR, float radioBL, float radioBR, float penAux, boolean hideCorners) {
		float[] dashPatternTop = getDashedPattern(styleTop);
		float[] dashPatternBottom = getDashedPattern(styleBottom);
		float[] dashPatternLeft = getDashedPattern(styleLeft);
		float[] dashPatternRight = getDashedPattern(styleRight);

		try {
			if (styleBottom!=STYLE_NONE_CONST) {
				cb.setLineDashPattern(dashPatternBottom, 0);
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
			if (styleBottom!=STYLE_NONE_CONST) {
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
			if (styleRight!=STYLE_NONE_CONST && dashPatternRight!=dashPatternBottom) {
				cb.stroke();
				cb.setLineDashPattern(dashPatternRight, 0);
				if (hideCorners && styleBottom==STYLE_NONE_CONST && radioBR==0) {
					cb.moveTo(x + w, y + penAux);
				}
				else {
					cb.moveTo(x + w, y + radioBR);
				}
			}
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
			if (styleTop!=STYLE_NONE_CONST && dashPatternTop!=dashPatternRight) {
				cb.stroke();
				cb.setLineDashPattern(dashPatternTop, 0);
				if (hideCorners && styleRight==STYLE_NONE_CONST && radioTR==0) {
					cb.moveTo(x + w - penAux, y + h);
				}
				else {
					cb.moveTo(x + w - radioTR, y + h);
				}
			}
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
			if (styleLeft!=STYLE_NONE_CONST  && dashPatternLeft!=dashPatternTop) {
				cb.stroke();
				cb.setLineDashPattern(dashPatternLeft, 0);
				if (hideCorners && styleTop==STYLE_NONE_CONST && radioTL==0) {
					cb.moveTo(x, y + h - penAux);
				}
				else {
					cb.moveTo(x, y + h - radioTL);
				}
			}
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
		} catch (IOException ioe) {
			log.error("roundRectangle failed: ", ioe);
		}
	}

	private void roundRectangle(PDPageContentStream cb, float x, float y, float w, float h,
								float radioTL, float radioTR, float radioBL, float radioBR) {
		try {
			float b = 0.4477f;
			if (radioBL>0) {
				cb.moveTo(x + radioBL, y);
			}
			else {
				cb.moveTo(x, y);
			}
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
		} catch (IOException ioe) {
			log.error("drawRectangle failed: ", ioe);
		}
	}

	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue,
						   int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR) {
		try (PDPageContentStream cb = new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false)){

			float penAux = (float)convertScale(pen);
			float rightAux = (float)convertScale(right);
			float bottomAux = (float)convertScale(bottom);
			float leftAux = (float)convertScale(left);
			float topAux = (float)convertScale(top);

			cb.saveGraphicsState();

			float x1, y1, x2, y2;
			x1 = leftAux + leftMargin;
			y1 = pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin;
			x2 = rightAux + leftMargin;
			y2 = pageSize.getUpperRightY() - topAux - topMargin -bottomMargin;

			cb.setLineWidth(penAux);
			cb.setLineCapStyle(2);

			if (cornerRadioBL==0 && cornerRadioBR==0 && cornerRadioTL==0 && cornerRadioTR==0 && styleBottom==0 && styleLeft==0 && styleRight==0 && styleTop==0) {
				if (pen > 0)
					cb.setStrokingColor(foreRed, foreGreen, foreBlue);
				else
					cb.setStrokingColor (backRed, backGreen, backBlue);

				cb.addRect(x1, y1, x2 - x1, y2 - y1);

				if (backMode!=0) {
					cb.setNonStrokingColor(new Color(backRed, backGreen, backBlue));
					cb.fillAndStroke();
				}
				cb.closePath();
				cb.stroke();
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
					cb.setStrokingColor(backRed, backGreen, backBlue);
					cb.setLineWidth(0);
					roundRectangle(cb, x1, y1, w, h,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR);
					cb.setNonStrokingColor(new Color(backRed, backGreen, backBlue));
					cb.fillAndStroke();
					cb.setLineWidth(penAux);
				}
				if (pen > 0) {
					cb.setStrokingColor(foreRed, foreGreen, foreBlue);
					drawRectangle(cb, x1, y1, w, h,
						styleTop, styleBottom, styleRight, styleLeft,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR, penAux, false);
				}
			}
			cb.restoreGraphicsState();

			log.debug("GxDrawRect -> (" + left + "," + top + ") - (" + right + "," + bottom + ")  BackMode: " + backMode + " Pen:" + pen);
		} catch (Exception e) {
			log.error("GxDrawRect failed: ", e);
		}
	}

	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style) {
		try (PDPageContentStream cb = new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false)){

			float widthAux = (float)convertScale(width);
			float rightAux = (float)convertScale(right);
			float bottomAux = (float)convertScale(bottom);
			float leftAux = (float)convertScale(left);
			float topAux = (float)convertScale(top);

			log.debug("GxDrawLine -> (" + left + "," + top + ") - (" + right + "," + bottom + ") Width: " + width);

			float x1, y1, x2, y2;

			x1 = leftAux + leftMargin;
			y1 = pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin;
			x2 = rightAux + leftMargin;
			y2 = pageSize.getUpperRightY() - topAux - topMargin -bottomMargin;

			cb.saveGraphicsState();
			cb.setStrokingColor(foreRed, foreGreen, foreBlue);
			cb.setLineWidth(widthAux);

			if (lineCapProjectingSquare) {
				cb.setLineCapStyle(2);
			}
			if (style!=0) {
				float[] dashPattern = getDashedPattern(style);
				cb.setLineDashPattern(dashPattern, 0);
			}
			cb.moveTo(x1, y1);
			cb.lineTo(x2, y2);
			cb.stroke();

			cb.restoreGraphicsState();
		} catch (IOException ioe){
			log.error("GxDrawLine failed:", ioe);
		}
	}

	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio) {
		try (PDPageContentStream cb = new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false)){
			PDImageXObject image;
			try {
				if (documentImages != null && documentImages.containsKey(bitmap)) {
					image = documentImages.get(bitmap);
				}
				else {

					if (!NativeFunctions.isWindows() && new File(bitmap).isAbsolute() && bitmap.startsWith(httpContext.getStaticContentBase())) {
						bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
					}

					if (!new File(bitmap).isAbsolute() && !bitmap.toLowerCase().startsWith("http:") && !bitmap.toLowerCase().startsWith("https:")) {
						if (bitmap.startsWith(httpContext.getStaticContentBase()))
						{
							bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
						}
						image = PDImageXObject.createFromFile(defaultRelativePrepend + bitmap,document);
						if(image == null) {
							bitmap = webAppDir + bitmap;
							image = PDImageXObject.createFromFile(bitmap,document);
						}
						else {
							bitmap = defaultRelativePrepend + bitmap;
						}
					}
					else {
						image = PDImageXObject.createFromFile(bitmap,document);
					}
				}
			}
			catch(java.lang.IllegalArgumentException ex) {
				URL url= new java.net.URL(bitmap);
				image = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(url.openStream()),bitmap);
			}

			if (documentImages == null) {
				documentImages = new ConcurrentHashMap<>();
			}
			documentImages.putIfAbsent(bitmap, image);

			log.debug("GxDrawBitMap -> '" + bitmap + "' [" + left + "," + top + "] - Size: (" + (right - left) + "," + (bottom - top) + ")");

			if(image != null) {
				float rightAux = (float)convertScale(right);
				float bottomAux = (float)convertScale(bottom);
				float leftAux = (float)convertScale(left);
				float topAux = (float)convertScale(top);

				float x = leftAux + leftMargin;
				float y = this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin;

				if (aspectRatio == 0)
					cb.drawImage(image, x, y, rightAux - leftAux, bottomAux - topAux);
				else
					cb.drawImage(image, x, y, (rightAux - leftAux) * aspectRatio, (bottomAux - topAux) * aspectRatio);
			}
		}
		catch(IOException ioe) {
			log.error("GxDrawBitMap failed:", ioe);
		}
	}

	public void GxAttris(String fontName, int fontSize, boolean fontBold, boolean fontItalic, boolean fontUnderline, boolean fontStrikethru, int Pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue) {
		boolean isCJK = false;
		boolean embeedFont = isEmbeddedFont(fontName);
		String originalFontName = fontName;
		if (!embeedFont) {
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

		if (barcode128AsImage && fontName.toLowerCase().indexOf("barcode 128") >= 0 || fontName.toLowerCase().indexOf("barcode128") >= 0)
			barcodeType = "barcode128";

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
					baseFont = createPDType1FontFromName(fontName);
					if (baseFont != null)
						baseFontName = baseFont.getName();
					if (baseFont == null){
						baseFont = PDType0Font.load(document, new File(getFontLocation(fontName)));
						baseFontName = fontName;
					}

				}
			}
			else {
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
						baseFont = PDType1Font.HELVETICA;
						baseFontName = baseFont.getName();
						foundFont = false;
					}
				}
				if (foundFont){
					baseFont = createPDType1FontFromName(fontName);
					if (baseFont != null)
						baseFontName = baseFont.getName();
					else{
						baseFont = PDType0Font.load(document, new File(getFontLocation(fontName)));
						baseFontName = fontName;
					}
				}
			}
		}
		catch(Exception e) {
			log.error("GxAttris failed: ", e);
		}
	}

	private static PDType1Font createPDType1FontFromName(String fontName) {
		switch (fontName) {
			case "Times-Roman":
				return PDType1Font.TIMES_ROMAN;
			case "Times-Bold":
				return PDType1Font.TIMES_BOLD;
			case "Times-Italic":
				return PDType1Font.TIMES_ITALIC;
			case "Times-BoldItalic":
				return PDType1Font.TIMES_BOLD_ITALIC;
			case "Helvetica":
				return PDType1Font.HELVETICA;
			case "Helvetica-Bold":
				return PDType1Font.HELVETICA_BOLD;
			case "Helvetica-Oblique":
				return PDType1Font.HELVETICA_OBLIQUE;
			case "Helvetica-BoldOblique":
				return PDType1Font.HELVETICA_BOLD_OBLIQUE;
			case "Courier":
				return PDType1Font.COURIER;
			case "Courier-Bold":
				return PDType1Font.COURIER_BOLD;
			case "Courier-Oblique":
				return PDType1Font.COURIER_OBLIQUE;
			case "Courier-BoldOblique":
				return PDType1Font.COURIER_BOLD_OBLIQUE;
			case "Symbol":
				return PDType1Font.SYMBOL;
			case "ZapfDingbats":
				return PDType1Font.ZAPF_DINGBATS;
			default:
				return null;
		}
	}

	public void setAsianFont(String fontName, String style) {
		try {
			String fontPath = getFontLocation(fontName);
			baseFont = PDType0Font.load(document, new File(fontPath));
			baseFontName = fontName;
		}
		catch(Exception e) {
			log.error("setAsianFont failed: ", e);
		}
	}
	public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border, int valign) {
		try (PDPageContentStream cb =  new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false)){
			boolean printRectangle = false;
			if (props.getBooleanGeneralProperty(Const.BACK_FILL_IN_CONTROLS, true))
				printRectangle = true;

			if (printRectangle && (border == 1 || backFill)) {
				GxDrawRect(left, top, right, bottom, border, foreColor.getRed(), foreColor.getGreen(), foreColor.getBlue(), backFill ? 1 : 0, backColor.getRed(), backColor.getGreen(), backColor.getBlue(), 0, 0);
			}

			sTxt = CommonUtil.rtrim(sTxt);

			PDFont font = createPDType1FontFromName(baseFont.getFontDescriptor().getFontName());
			if (font == null)
				font = PDType0Font.load(document, new File(getFontLocation(baseFontName)));

			cb.setFont(font,fontSize);
			cb.setNonStrokingColor(foreColor);
			float captionHeight = baseFont.getFontMatrix().transformPoint(0, font.getFontDescriptor().getCapHeight()).y * fontSize;
			float rectangleWidth = baseFont.getStringWidth(sTxt) / 1000 * fontSize;
			float lineHeight = (baseFont.getFontDescriptor().getFontBoundingBox().getUpperRightY() - baseFont.getFontDescriptor().getFontBoundingBox().getLowerLeftY())/ 1000 * fontSize;
			float textBlockHeight = (float)convertScale(bottom-top);
			int linesCount =   (int)(textBlockHeight/lineHeight);
			int bottomOri = bottom;
			int topOri = top;

			if (linesCount >= 2 && !((align & 16) == 16) && htmlformat != 1) {
				if (valign == PDFReportPDFBox.VerticalAlign.TOP.value())
					bottom = top + (int)reconvertScale(lineHeight);
				else if (valign == PDFReportPDFBox.VerticalAlign.BOTTOM.value())
					top = bottom - (int)reconvertScale(lineHeight);
			}

			float bottomAux = (float)convertScale(bottom) - ((float)convertScale(bottom-top) - captionHeight)/2;
			float topAux = (float)convertScale(top) + ((float)convertScale(bottom-top) - captionHeight)/2;

			float startHeight = bottomAux - topAux - captionHeight;

			float leftAux = (float)convertScale(left);
			float rightAux = (float)convertScale(right);
			int alignment = align & 3;
			boolean autoResize = (align & 256) == 256;

			if (htmlformat == 1) {
				log.info("As for now, HTML printing is not supported while generating reports using PDFBox");
			}
			else
			if (barcodeType != null){
				log.debug("Barcode: --> " + barcode.getClass().getName());
				try {
					PDRectangle rectangle = new PDRectangle();
					switch (alignment) {
						case 1: // Center Alignment
							rectangle.setLowerLeftX( (leftAux + rightAux) / 2 + leftMargin - rectangleWidth / 2);
							rectangle.setLowerLeftY(this.pageSize.getUpperRightY() - (float)convertScale(bottom) - topMargin - bottomMargin);
							rectangle.setUpperRightX((leftAux + rightAux) / 2 + leftMargin + rectangleWidth / 2);
							rectangle.setUpperRightY(this.pageSize.getUpperRightY() - (float)convertScale(top) - topMargin - bottomMargin);
							break;
						case 2: // Right Alignment
							rectangle.setLowerLeftX( rightAux + leftMargin - rectangleWidth);
							rectangle.setLowerLeftY(this.pageSize.getUpperRightY() - (float)convertScale(bottom) - topMargin - bottomMargin);
							rectangle.setUpperRightX(rightAux + leftMargin);
							rectangle.setUpperRightY(this.pageSize.getUpperRightY() - (float)convertScale(top) - topMargin - bottomMargin);
							break;
						case 0: // Left Alignment
							rectangle.setLowerLeftX( leftAux + leftMargin);
							rectangle.setLowerLeftY(this.pageSize.getUpperRightY() - (float)convertScale(bottom) - topMargin - bottomMargin);
							rectangle.setUpperRightX(leftAux + leftMargin + rectangleWidth);
							rectangle.setUpperRightY(this.pageSize.getUpperRightY() - (float)convertScale(top) - topMargin - bottomMargin);
							break;
					}
					if (barcodeType.equals("barcode128")){
						Code128Writer barcodeWriter = new Code128Writer();
						barcode = barcodeWriter.encode(sTxt, BarcodeFormat.CODE_128, Math.round(rectangle.getWidth()), Math.round(rectangle.getHeight()));
					}

					BufferedImage imageCode = new BufferedImage(barcode.getWidth(), barcode.getHeight(), BufferedImage.TYPE_INT_RGB);
					for (int x = 0; x < barcode.getWidth(); x++)
						for (int y = 0; y < barcode.getHeight(); y++) {
							int color;
							if (foreColor == null || backColor == null)
								color = Color.BLACK.getRGB();
							else
								color = barcode.get(x, y) ? foreColor.getRGB() : backColor.getRGB();
							imageCode.setRGB(x, y, color);
						}
					float scale = Math.min(rectangle.getHeight() / imageCode.getHeight(), 1.0f);
					float newImageWidth = imageCode.getWidth() * scale;
					float newImageHeight = imageCode.getHeight() * scale;
					cb.drawImage(LosslessFactory.createFromImage(document, imageCode),leftAux + leftMargin, rectangle.getLowerLeftY(),newImageWidth, newImageHeight);
				}
				catch (Exception ex) {
					log.error("GxDrawText: Error generating Barcode " + barcode.getClass().getName(), ex);
				}
			}
			else {

				if(backFill) {
					PDRectangle rectangle = new PDRectangle();
					switch(alignment) {
						case 1: // Center Alignment
							rectangle.setLowerLeftX((leftAux + rightAux)/2 + leftMargin - rectangleWidth/2);
							rectangle.setLowerLeftY(this.pageSize.getUpperRightY() -  bottomAux - topMargin -bottomMargin);
							rectangle.setUpperRightX((leftAux + rightAux)/2 + leftMargin + rectangleWidth/2);
							rectangle.setUpperRightY(this.pageSize.getUpperRightY() - topAux - topMargin -bottomMargin);
							break;
						case 2: // Right Alignment
							rectangle.setLowerLeftX(rightAux + leftMargin - rectangleWidth);
							rectangle.setLowerLeftY(this.pageSize.getUpperRightY() -  bottomAux - topMargin -bottomMargin);
							rectangle.setUpperRightX(rightAux + leftMargin);
							rectangle.setUpperRightY(this.pageSize.getUpperRightY() - topAux - topMargin -bottomMargin);
							break;
						case 0: // Left Alignment
							rectangle.setLowerLeftX(leftAux + leftMargin);
							rectangle.setLowerLeftY(this.pageSize.getUpperRightY() -  bottomAux - topMargin -bottomMargin);
							rectangle.setUpperRightX(leftAux + leftMargin + rectangleWidth);
							rectangle.setUpperRightY(this.pageSize.getUpperRightY() - topAux - topMargin -bottomMargin);
							break;
					}
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false);
					contentStream.setNonStrokingColor(backColor);
					contentStream.addRect(rectangle.getLowerLeftX(), rectangle.getLowerLeftY(),rectangle.getWidth(), rectangle.getHeight());
					contentStream.fill();
					contentStream.close();
				}

				float underlineSeparation = lineHeight / 5;
				int underlineHeight = (int)underlineSeparation + (int)(underlineSeparation/4);
				PDRectangle underline;

				if (fontUnderline) {
					underline = new PDRectangle();

					switch(alignment) {
						case 1: // Center Alignment
							underline.setLowerLeftX((leftAux + rightAux)/2 + leftMargin - rectangleWidth/2);
							underline.setLowerLeftY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation);
							underline.setUpperRightX((leftAux + rightAux)/2 + leftMargin + rectangleWidth/2);
							underline.setUpperRightY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight);
							break;
						case 2: // Right Alignment
							underline.setLowerLeftX(rightAux + leftMargin - rectangleWidth);
							underline.setLowerLeftY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation);
							underline.setUpperRightX(rightAux + leftMargin);
							underline.setUpperRightY(this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);
							break;
						case 0: // Left Alignment
							underline.setLowerLeftX(leftAux + leftMargin);
							underline.setLowerLeftY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation);
							underline.setUpperRightX(leftAux + leftMargin + rectangleWidth);
							underline.setUpperRightY(this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);
							break;
					}
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false);
					contentStream.setNonStrokingColor(foreColor);
					contentStream.addRect(underline.getLowerLeftX(), underline.getLowerLeftY(),underline.getWidth(), underline.getHeight());
					contentStream.fill();
					contentStream.close();
				}

				if (fontStrikethru) {
					underline = new PDRectangle();
					float strikethruSeparation = lineHeight / 2;

					switch(alignment) {
						case 1: // Center Alignment
							underline.setLowerLeftX((leftAux + rightAux)/2 + leftMargin - rectangleWidth/2);
							underline.setLowerLeftY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation);
							underline.setUpperRightX((leftAux + rightAux)/2 + leftMargin + rectangleWidth/2);
							underline.setUpperRightY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);
							break;
						case 2: // Right Alignment
							underline.setLowerLeftX(rightAux + leftMargin - rectangleWidth);
							underline.setLowerLeftY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation);
							underline.setUpperRightX(rightAux + leftMargin);
							underline.setUpperRightY(this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);
							break;
						case 0: // Left Alignment
							underline.setLowerLeftX(leftAux + leftMargin);
							underline.setLowerLeftY(this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation);
							underline.setUpperRightX(leftAux + leftMargin + rectangleWidth);
							underline.setUpperRightY(this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);
							break;
					}
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false);
					contentStream.setNonStrokingColor(foreColor);
					contentStream.addRect(underline.getLowerLeftX(), underline.getLowerLeftY() - strikethruSeparation * 1/3, underline.getWidth(), underline.getHeight());
					contentStream.fill();
					contentStream.close();
				}

				if(sTxt.trim().equalsIgnoreCase("{{Pages}}")) {
					if (!templateCreated) {
						formXObjecttemplate = new PDFormXObject(document);
						template = new PDPageContentStream(document, formXObjecttemplate, outputStream);
						formXObjecttemplate.setResources(new PDResources());
						formXObjecttemplate.setBBox(new PDRectangle(right - left, bottom - top));
						templateCreated = true;
					}
					PDFormXObject form = new PDFormXObject(document);
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1),PDPageContentStream.AppendMode.APPEND,false);
					contentStream.transform(Matrix.getTranslateInstance(leftAux + leftMargin, leftAux + leftMargin));
					contentStream.drawForm(form);
					contentStream.close();
					templateFont = new PDType0Font(baseFont.getCOSObject());
					templateFontSize = fontSize;
					templateColorFill = foreColor;
					return;
				}

				float textBlockWidth = rightAux - leftAux;
				float TxtWidth = baseFont.getStringWidth(sTxt)/ 1000 * fontSize;
				boolean justified = (alignment == 3) && textBlockWidth < TxtWidth;
				boolean wrap = ((align & 16) == 16);

				if (wrap || justified) {
					bottomAux = (float)convertScale(bottomOri);
					topAux = (float)convertScale(topOri);

					float llx = leftAux + leftMargin;
					float lly = this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin;
					float urx = rightAux + leftMargin;
					float ury = this.pageSize.getUpperRightY() - topAux - topMargin - bottomMargin;

					showWrappedTextAligned(cb, font, alignment, sTxt, llx, lly, urx, ury);

				}
				else {
					startHeight=0;
					if (!autoResize) {
						String newsTxt = sTxt;
						while(TxtWidth > textBlockWidth && (newsTxt.length()-1>=0)) {
							sTxt = newsTxt;
							newsTxt = newsTxt.substring(0, newsTxt.length()-1);
							TxtWidth = baseFont.getStringWidth(newsTxt) / 1000 * fontSize;
						}
					}
					switch(alignment) {
						case 1: // Center Alignment
							showTextAligned(cb, font,alignment, sTxt, ((leftAux + rightAux) / 2) + leftMargin, this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin + startHeight);
							break;
						case 2: // Right Alignment
							showTextAligned(cb, font, alignment, sTxt, rightAux + leftMargin, this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin + startHeight);
							break;
						case 0: // Left Alignment
						case 3: // Justified, only one text line
							showTextAligned(cb, font, alignment, sTxt, leftAux + leftMargin, this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin + startHeight);
							break;
					}
				}
			}
		} catch (IOException ioe){
			log.error("GxDrawText failed: ", ioe);
		}
	}

	private void resolveTextStyling(PDPageContentStream contentStream, String text, float x, float y, boolean isWrapped){
		try {
			if (this.fontBold && this.fontItalic){
				contentStream.setStrokingColor(foreColor);
				contentStream.setLineWidth(fontSize * 0.05f);
				contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
				contentStream.beginText();
				contentStream.moveTextPositionByAmount(x, y);
				contentStream.setTextMatrix(new Matrix(1, 0, 0.2f, 1, x + 0.2f * y, y));
				contentStream.newLineAtOffset(-0.2f * y, 0);
			} else if (this.fontBold && !this.fontItalic){
				contentStream.setStrokingColor(foreColor);
				contentStream.setLineWidth(fontSize * 0.05f);
				contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
				contentStream.beginText();
				contentStream.moveTextPositionByAmount(x, y);
			} else if (!this.fontBold && this.fontItalic){
				contentStream.beginText();
				contentStream.moveTextPositionByAmount(x, y);
				contentStream.setTextMatrix(new Matrix(1, 0, 0.2f, 1, x + 0.2f * y, y));
				contentStream.newLineAtOffset(-0.2f * y, 0);
			} else {
				contentStream.beginText();
				contentStream.newLineAtOffset(x, y);
			}
			contentStream.showText(text);
			if (isWrapped) contentStream.newLineAtOffset(-x, -(float)(Double.valueOf(props.getGeneralProperty(Const.LEADING)).doubleValue()));
			contentStream.endText();
			contentStream.setLineWidth(1f); // Default line width for PDFBox 2.0.27
			contentStream.setRenderingMode(RenderingMode.FILL); // Default text rendering mode for PDFBox 2.0.27
		} catch (IOException ioe) {
			log.error("failed to apply text styling: ", ioe);
		}
	}

	private void showWrappedTextAligned(PDPageContentStream contentStream, PDFont font, int alignment, String text, float llx, float lly, float urx, float ury) {
		try {
			List<String> lines = new ArrayList<>();
			String[] words = text.split(" ");
			StringBuilder currentLine = new StringBuilder();
			for (String word : words) {
				float currentLineWidth = font.getStringWidth(currentLine + " " + word) / 1000 * fontSize;
				if (currentLineWidth < urx - llx) {
					if (currentLine.length() > 0) {
						currentLine.append(" ");
					}
					currentLine.append(word);
				} else {
					lines.add(currentLine.toString());
					currentLine.setLength(0);
					currentLine.append(word);
				}
			}
			lines.add(currentLine.toString());

			float leading = lines.size() == 1 ? fontSize : 1.2f * fontSize;
			float totalTextHeight = fontSize * lines.size() + leading * (lines.size() - 1);
			float startY = lines.size() == 1 ? lly + (ury - lly - totalTextHeight) / 2 : lly + (ury - lly - totalTextHeight) / 2 + (lines.size() - 1) * (fontSize + leading) + font.getFontDescriptor().getDescent() / 1000 * fontSize;

			for (String line : lines) {
				float lineWidth = font.getStringWidth(line) / 1000 * fontSize;
				float startX;

				switch (alignment) {
					case 1: // Center-aligned
						startX = llx + (urx - llx - lineWidth) / 2;
						break;
					case 2: // Right-aligned
						startX = urx - lineWidth;
						break;
					default: // Left-aligned & Justified, only one text line
						startX = llx;
						break;
				}

				resolveTextStyling(contentStream, line, startX, startY, true);
				startY -= leading;
			}
		} catch (IOException ioe) {
			log.error("failed to draw wrapped text: ", ioe);
		}
	}

	private void showTextAligned(PDPageContentStream contentStream, PDFont font, int alignment, String text, float x, float y){
		try {
			contentStream.setLeading((float)(Double.valueOf(props.getGeneralProperty(Const.LEADING)).doubleValue()));
			float textWidth = font.getStringWidth(text) / 1000 * fontSize;
			switch (alignment) {
				case 0: // Left-aligned
				case 3: // Justified, only one text line
					break;
				case 1: // Center-aligned
					x = x - textWidth / 2;
					break;
				case 2: // Right-aligned
					x = x - textWidth;
					break;
			}
			resolveTextStyling(contentStream,text, x, y,false);
		} catch (IOException ioe) {
			log.error("failed to draw aligned text: ", ioe);
		}
	}

	public boolean GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) {
		try {
			boolean preResult = super.GxPrintInit(output, gxXPage, gxYPage, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);

			runDirection = Integer.valueOf(props.getGeneralProperty(Const.RUN_DIRECTION)).intValue();

			this.pageSize = computePageSize(leftMargin, topMargin, pageWidth, pageLength, props.getBooleanGeneralProperty(Const.MARGINS_INSIDE_BORDER, false));
			gxXPage[0] = (int)this.pageSize.getUpperRightX ();
			if (props.getBooleanGeneralProperty(Const.FIX_SAC24437, true))
				gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y);
			else
				gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y_OLD);

			init();

			if (!preResult)
				return !preResult;
			else
				return true;
		} catch (Exception e) {
			log.error("GxPrintInit failed" , e);
			return false;
		}
	}

	private PDRectangle computePageSize(float leftMargin, float topMargin, int width, int length, boolean marginsInsideBorder) {
		if ((leftMargin == 0 && topMargin == 0)||marginsInsideBorder) {
			if (length == 23818 && width == 16834)
				return PDRectangle.A3;
			else if (length == 16834 && width == 11909)
				return PDRectangle.A4;
			else if (length == 11909 && width == 8395)
				return PDRectangle.A5;
			else if (length == 20016 && width == 5731)
				return new PDRectangle(250f, 353f);
			else if (length == 14170 && width == 9979)
				return new PDRectangle(176f, 250f);
			else if (length == 15120 && width == 10440)
				return new PDRectangle(184.15f, 266.7f);
			else if (length == 20160 && width == 12240)
				return PDRectangle.LEGAL;
			else if (length == 15840 && width == 12240)
				return PDRectangle.LETTER;
			else
				return new PDRectangle((int)(width / PAGE_SCALE_X) , (int)(length / PAGE_SCALE_Y) );
		}
		return new PDRectangle((int)(width / PAGE_SCALE_X) + leftMargin, (int)(length / PAGE_SCALE_Y) + topMargin);
	}

	public void GxEndDocument() {
		try {
			if(document.getNumberOfPages() == 0) {
				document.addPage(new PDPage(this.pageSize));
				pages++;
			}
			if (template != null) {
				try{
					template.beginText();
					template.setFont(baseFont, fontSize);
					template.setTextMatrix(new Matrix());
					template.setNonStrokingColor(templateColorFill);
					template.showText(String.valueOf(pages));
					template.endText();
					template.close();
					for (PDPage page : document.getPages()){
						try (PDPageContentStream templatePainter = new PDPageContentStream(document, page,PDPageContentStream.AppendMode.APPEND,false)) {
							templatePainter.drawForm(formXObjecttemplate);
						}
					}
					template.close();
				} catch (IOException e){
					log.error("GxEndDocument: failed to apply template" , e);;
				}
			}
			int copies = 1;
			try {
				copies = Integer.parseInt(printerSettings.getProperty(form, Const.COPIES));
				log.debug("Setting number of copies to " + copies);

				writer = document.getDocumentCatalog();

				COSDictionary dict = new COSDictionary();
				if (writer.getViewerPreferences() != null && writer.getViewerPreferences().getCOSObject() != null)
					dict = writer.getViewerPreferences().getCOSObject();
				PDViewerPreferences viewerPreferences = new PDViewerPreferences(dict);
				viewerPreferences.setPrintScaling(PDViewerPreferences.PRINT_SCALING.None);
				dict.setInt("NumCopies", copies);
				writer.setViewerPreferences(viewerPreferences);

				int duplex= Integer.parseInt(printerSettings.getProperty(form, Const.DUPLEX));
				COSName duplexValue;
				switch (duplex){
					case 1: duplexValue = COSName.HELV; break;
					case 2: duplexValue = COSName.DUPLEX; break;
					case 3: duplexValue = COSName.DUPLEX; break;
					case 4: duplexValue = COSName.DUPLEX; break;
					default: duplexValue = COSName.NONE;
				}
				log.debug("Setting duplex to " + duplexValue);
				writer = document.getDocumentCatalog();
				dict = writer.getViewerPreferences().getCOSObject();
				if (dict == null) {dict = new COSDictionary();}
				viewerPreferences = new PDViewerPreferences(dict);
				viewerPreferences.setPrintScaling(PDViewerPreferences.PRINT_SCALING.None);
				dict.setName(COSName.DUPLEX, duplexValue.toString());
				writer.setViewerPreferences(viewerPreferences);
			}
			catch(Exception e) {
				log.error("GxEndDocument: failed to apply viewer preferences ", e);
			}

			String serverPrinting = props.getGeneralProperty(Const.SERVER_PRINTING);
			boolean fit= props.getGeneralProperty(Const.ADJUST_TO_PAPER).equals("true");
			if ((outputType==Const.OUTPUT_PRINTER || outputType==Const.OUTPUT_STREAM_PRINTER) && (httpContext instanceof HttpContextWeb && serverPrinting.equals("false"))) {
				PDDocumentCatalog catalog = document.getDocumentCatalog();
				StringBuffer jsActions = new StringBuffer();
				jsActions.append("var pp = this.getPrintParams();\n");
				String printerAux=printerSettings.getProperty(form, Const.PRINTER);
				String printer = replace(printerAux, "\\", "\\\\");

				if (printer!=null && !printer.equals("")) {
					jsActions.append("pp.printerName = \"" + printer + "\";\n");
				}

				if (fit) {
					jsActions.append("pp.pageHandling = pp.constants.handling.fit;\n");
				}
				else {
					jsActions.append("pp.pageHandling = pp.constants.handling.none;\n");
				}

				if (printerSettings.getProperty(form, Const.MODE, "3").startsWith("0")) {
					jsActions.append("pp.interactive = pp.constants.interactionLevel.automatic;\n");
					for(int i = 0; i < copies; i++)
					{
						jsActions.append("this.print(pp);\n");
					}
				}
				else {
					jsActions.append("pp.interactive = pp.constants.interactionLevel.full;\n");
					jsActions.append("this.print(pp);\n");
				}
				PDActionJavaScript openActions = new PDActionJavaScript(jsActions.toString());
				catalog.setOpenAction(openActions);
			}
			try {
				document.save(outputStream);
				document.close();
			} catch (IOException ioe) {
				log.error("GxEndDocument: failed to save document to the output stream", ioe);
			}

			log.debug("GxEndDocument!");

			try{ props.save(); } catch(IOException e) { ; }

			switch(outputType) {
				case Const.OUTPUT_SCREEN:
					try{ outputStream.close(); } catch(IOException e) { }
					try{ showReport(docName, modal); }
					catch(Exception e) {
						log.error("GxEndDocument: failed to show report ", e);
					}
					break;
				case Const.OUTPUT_PRINTER:
					try{ outputStream.close(); } catch(IOException e) { }
					try{
						if (!(httpContext instanceof HttpContextWeb) || !serverPrinting.equals("false")) {
							printReport(docName, this.printerOutputMode == 1);
						}
					} catch(Exception e){
						log.error("GxEndDocument: failed to print report ", e);
					}
					break;
				case Const.OUTPUT_FILE:
					try{ outputStream.close(); } catch (IOException e) { log.error("GxEndDocument: failed to save report to file ", e); }
					break;
				case Const.OUTPUT_STREAM:
				case Const.OUTPUT_STREAM_PRINTER:
				default: break;
			}
			outputStream = null;
		} catch (Exception e){
			log.error("GxEndDocument failed: ", e);;
		}
	}
	public void GxStartPage() {
		document.addPage(new PDPage(this.pageSize));
		pages = pages + 1;
		page = page + 1;
	}

}