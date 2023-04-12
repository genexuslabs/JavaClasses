package com.genexus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

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
				URL url = new URL(imageFile);
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
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

	private static String writeImage(BufferedImage croppedImage, String destinationFilePathOrUrl) throws IOException {
		String newFileName = PrivateUtilities.getTempFileName(CommonUtil.getFileType(destinationFilePathOrUrl));
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			ImageIO.write(croppedImage, CommonUtil.getFileType(newFileName), outStream);
			outStream.flush();
			byte[] imageInByte = outStream.toByteArray();
			return GXutil.blobFromBytes(imageInByte);
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
			BufferedImage image = createBufferedImageFromURI(imageFile);
			BufferedImage rotatedImage = rotateImage(image, angle);
			return writeImage(rotatedImage, imageFile);
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

		int nWidth = (int) Math.floor((double) width * cos + (double) height * sin);
		int nHeight = (int) Math.floor((double) height * cos + (double) width * sin);

		BufferedImage rotatedImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = rotatedImage.createGraphics();
		AffineTransform at = new AffineTransform();
		at.translate((nWidth - width) / 2, (nHeight - height) / 2);
		at.rotate(radian, (double) (width / 2), (double) (height / 2));
		graphics.setTransform(at);
		graphics.drawImage(buffImage, 0, 0, null);
		graphics.drawRect(0, 0, nWidth - 1, nHeight - 1);
		graphics.dispose();

		return rotatedImage;
	}
}
