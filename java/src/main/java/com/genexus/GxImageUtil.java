package com.genexus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import com.genexus.db.driver.ResourceAccessControlList;
import com.genexus.util.GxFileInfoSourceType;
import com.genexus.util.GXFile;
import org.apache.logging.log4j.Logger;

public class GxImageUtil {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GxImageUtil.class);
	private static int INVALID_CODE = -1;

	private static InputStream getInputStream(String filePathOrUrl) throws IOException {
		return getGXFile(filePathOrUrl).getStream();
	}

	private static BufferedImage createBufferedImageFromURI(String filePathOrUrl) throws IOException
	{
		IHttpContext httpContext = com.genexus.ModelContext.getModelContext().getHttpContext();
		InputStream is = null;
		try{
			if (filePathOrUrl.toLowerCase().startsWith("http://") || filePathOrUrl.toLowerCase().startsWith("https://") ||
				(httpContext.isHttpContextWeb() && filePathOrUrl.startsWith(httpContext.getContextPath())))
				is = new URL(GXDbFile.pathToUrl( filePathOrUrl, httpContext)).openStream();
			else
				is = getGXFile(filePathOrUrl).getStream();
			return ImageIO.read(is);
		} catch (IOException e) {
			log.error("Failed to read image stream: " + filePathOrUrl);
			throw e;
		} finally {is.close();}
	}

	private static GXFile getGXFile(String filePathOrUrl) {
		String basePath = (com.genexus.ModelContext.getModelContext() != null) ? com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath(): "";
		return new GXFile(basePath, filePathOrUrl, ResourceAccessControlList.Default, GxFileInfoSourceType.Unknown);
	}

	public static long getFileSize(String imageFile){
		if (!isValidInput(imageFile))
			return INVALID_CODE;
		IHttpContext httpContext = com.genexus.ModelContext.getModelContext().getHttpContext();
		if (imageFile.toLowerCase().startsWith("http://") || imageFile.toLowerCase().startsWith("https://") ||
			(httpContext.isHttpContextWeb() && imageFile.startsWith(httpContext.getContextPath()))){
			try {
				URL url = new URL(GXDbFile.pathToUrl(imageFile, httpContext));
				URLConnection connection = url.openConnection();
				return Long.parseLong(connection.getHeaderField("Content-Length"));
			} catch (Exception e) {
				log.error("getFileSize " + imageFile + " failed" , e);
			}
		} else
			return getGXFile(imageFile).getLength();
		return INVALID_CODE;
	}

	public static int getImageHeight(String imageFile) {
		if (!isValidInput(imageFile))
			return INVALID_CODE;

		try {
			return createBufferedImageFromURI(imageFile).getHeight();
		}
		catch (Exception e) {
			log.error("getImageHeight " + imageFile + " failed" , e);
		}
		return INVALID_CODE;
	}

	private static boolean isValidInput(String imageFile) {
		boolean isValid =  imageFile != null && imageFile.length() > 0;
		if (!isValid) {
			log.debug("Image Api - FileName cannot be empty");
		}
		return isValid;
	}

	public static int getImageWidth(String imageFile) {
		if (!isValidInput(imageFile))
			return INVALID_CODE;

		try {
			return createBufferedImageFromURI(imageFile).getWidth();
		}
		catch (Exception e) {
			log.error("getImageWidth " + imageFile + " failed" , e);
		}
		return INVALID_CODE;
	}

	public static String crop(String imageFile, int x, int y, int width, int height) {
		if (!isValidInput(imageFile))
			return "";
		try {
			BufferedImage image = createBufferedImageFromURI(imageFile);
			BufferedImage croppedImage = image.getSubimage(x, y, width, height);
			return writeImage(croppedImage, imageFile);
		}
		catch (Exception e) {
			log.error("crop " + imageFile + " failed" , e);
		}
		return "";
	}

	private static final Pattern IMAGE_PATTERN = Pattern.compile("\\.(jpg|jpeg|png|bmp|webp|jfif)([/?]|$)", Pattern.CASE_INSENSITIVE);

	private static String writeImage(BufferedImage bufferedImage, String destinationFilePathOrUrl) throws IOException {
		String newFileName;
		if (!IMAGE_PATTERN.matcher(destinationFilePathOrUrl).find()) {
			URL imageUrl = new URL(destinationFilePathOrUrl);
			HttpURLConnection connection = null;
			String format;
			try {
				connection = (HttpURLConnection) imageUrl.openConnection();
				format = connection.getContentType().split("/")[1];
			} finally {
				if (connection != null) connection.disconnect();
			}
			newFileName = PrivateUtilities.getTempFileName(format);
		} else
			newFileName = PrivateUtilities.getTempFileName(CommonUtil.getFileType(destinationFilePathOrUrl));

		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			ImageIO.write(bufferedImage, CommonUtil.getFileType(newFileName), outStream);
			try (ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray())) {
				GXFile file = getGXFile(Preferences.getDefaultPreferences().getPRIVATE_PATH() + newFileName);
				file.create(inStream, true);
				file.close();
				return file.getURI();
			}
		}
	}

	public static String flipHorizontally(String imageFile) {
		if (!isValidInput(imageFile))
			return "";
		try {
			BufferedImage image = createBufferedImageFromURI(imageFile);
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-image.getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage flippedImage = op.filter(image, null);
			return writeImage(flippedImage, imageFile);
		}
		catch (Exception e) {
			log.error("flip horizontal " + imageFile + " failed" , e);
		}
		return "";
	}

	public static String flipVertically(String imageFile) {
		if (!isValidInput(imageFile))
			return "";
		try {
			BufferedImage image = createBufferedImageFromURI(imageFile);
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage flippedImage = op.filter(image, null);
			return writeImage(flippedImage, imageFile);
		}
		catch (Exception e) {
			log.error("flip vertical " + imageFile + " failed" , e);
		}
		return "";
	}

	private static BufferedImage jpgToPng(String imageFile){
		try {
			BufferedImage originalImage = createBufferedImageFromURI(imageFile);
			if (imageFile.indexOf(".jpg") > 0 || imageFile.indexOf(".jpeg") > 0)
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
					ImageIO.write(originalImage, "png", baos);
					byte[] bytes = baos.toByteArray();
					try (InputStream is = new ByteArrayInputStream(bytes)) {originalImage = ImageIO.read(is);}
				}
			return originalImage;
		} catch (IOException ioe) {
			log.error("format conversion for " + imageFile + " failed" , ioe);
			return null;
		}
	}

	public static String roundBorders(String imageFile, int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius) {
		if (!isValidInput(imageFile)) return "";
		try {
			// Rounded images are basically images with transparent rounded borders and jpg and jpeg formats do not
			// support transparency, so we have to create a new identical image but in png format if working with a jpg image
			BufferedImage originalImage = jpgToPng(imageFile);
			String newImageFile = imageFile;
			if (imageFile.indexOf(".jpg") != -1)
				newImageFile = imageFile.substring(0, imageFile.indexOf(".jpg")) + ".png";
			else if (imageFile.indexOf(".jpeg") != -1)
				newImageFile = imageFile.substring(0, imageFile.indexOf(".jpeg")) + ".png";

			int w = originalImage.getWidth();
			int h = originalImage.getHeight();

			BufferedImage roundedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = roundedImage.createGraphics();
			g2.setComposite(AlphaComposite.Src);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.WHITE);

			GeneralPath path = new GeneralPath();
			path.moveTo(0, topLeftRadius);
			path.quadTo(0, 0, topLeftRadius, 0);
			path.lineTo(w - topRightRadius, 0);
			path.quadTo(w, 0, w, topRightRadius);
			path.lineTo(w, h - bottomRightRadius);
			path.quadTo(w, h, w - bottomRightRadius, h);
			path.lineTo(bottomLeftRadius, h);
			path.quadTo(0, h, 0, h - bottomLeftRadius);
			path.closePath();

			g2.fill(path);
			g2.setComposite(AlphaComposite.SrcIn);
			g2.drawImage(originalImage, 0, 0, null);
			g2.dispose();

			return writeImage(roundedImage,newImageFile);

		} catch (Exception e){
			log.error("round borders for " + imageFile + " failed" , e);
			return "";
		}
	}

	public static String resize(String imageFile, int width, int height, boolean keepAspectRatio) {
		if (!isValidInput(imageFile))
			return "";
		try {
			BufferedImage image = createBufferedImageFromURI(imageFile);
			if (keepAspectRatio) {
				double imageHeight = image.getHeight();
				double imageWidth = image.getWidth();

				if (imageHeight/height > imageWidth/width) {
					width = (int) (height * imageWidth / imageHeight);
				} else {
					height = (int) (width * imageHeight / imageWidth);
				}
			}
			BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
			Graphics2D g2d = resizedImage.createGraphics();
			g2d.drawImage(image, 0, 0, width, height, null);
			g2d.dispose();
			return writeImage(resizedImage, imageFile);
		}
		catch (Exception e) {
			log.error("resize " + imageFile + " failed" , e);
		}
		return "";
	}

	public static String scale(String imageFile, short percent) {
		if (!isValidInput(imageFile))
			return "";

		try {
			BufferedImage image = createBufferedImageFromURI(imageFile);
			imageFile = resize(imageFile, image.getWidth() * percent / 100, image.getHeight() * percent / 100,true);
		}
		catch (Exception e) {
			log.error("scale " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String rotate(String imageFile, short angle) {
		if (!isValidInput(imageFile))
			return "";
		try {
			// The process of rotating an image implies fitting it inside a rectangle and filling the excess
			// with transparent background and jpg and jpeg formats do not transparency, so we have to create a new
			// identical image but in png format if working with a jpg image
			BufferedImage originalImage = jpgToPng(imageFile);
			String newImageFile = imageFile;
			if (imageFile.indexOf(".jpg") != -1)
				newImageFile = imageFile.substring(0, imageFile.indexOf(".jpg")) + ".png";
			else if (imageFile.indexOf(".jpeg") != -1)
				newImageFile = imageFile.substring(0, imageFile.indexOf(".jpeg")) + ".png";
			BufferedImage rotatedImage = rotateImage(originalImage, angle);
			return writeImage(rotatedImage, newImageFile);
		}
		catch (Exception e) {
			log.error("rotate " + imageFile + " failed" , e);
		}
		return "";
	}

	private static BufferedImage rotateImage(BufferedImage buffImage, double angle) {
		double radian = Math.toRadians(angle);
		double sin = Math.abs(Math.sin(radian));
		double cos = Math.abs(Math.cos(radian));

		int width = buffImage.getWidth();
		int height = buffImage.getHeight();

		int nWidth = (int) Math.floor(width * cos + height * sin);
		int nHeight = (int) Math.floor(height * cos + width * sin);

		BufferedImage rotatedImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = rotatedImage.createGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AffineTransform at = new AffineTransform();
		at.translate((double) (nWidth - width) / 2, (double) (nHeight - height) / 2);
		at.rotate(radian, width / 2.0, height / 2.0);

		graphics.setTransform(at);
		graphics.drawImage(buffImage, 0, 0, null);
		graphics.dispose();

		return rotatedImage;
	}
}
