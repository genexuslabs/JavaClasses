package com.genexus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;

import com.genexus.db.driver.ResourceAccessControlList;
import com.genexus.util.GxFileInfoSourceType;
import com.genexus.util.GXFile;
import org.apache.logging.log4j.Logger;

public class GxImageUtil {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GxImageUtil.class);
	private static int INVALID_CODE = -1;

	private static InputStream getInputStream(String imageFile) throws IOException {
		return getGXFile(imageFile).getStream();
	}

	private static GXFile getGXFile(String imageFile) {
		String basePath = (com.genexus.ModelContext.getModelContext() != null) ? com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath(): "";
		return new GXFile(basePath, imageFile.replace("/", File.separator), ResourceAccessControlList.Default, GxFileInfoSourceType.Unknown);
	}

	public static long getFileSize(String imageFile){
		if (!isValidInput(imageFile))
			return INVALID_CODE;

		return new GXFile(imageFile).getLength();
	}

	public static int getImageHeight(String imageFile) {
		if (!isValidInput(imageFile))
			return INVALID_CODE;

		try (InputStream is = getInputStream(imageFile)) {
			return ImageIO.read(is).getHeight();
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

		try (InputStream is = getInputStream(imageFile)) {
			return ImageIO.read(is).getWidth();
		}
		catch (Exception e) {
			log.error("getImageWidth " + imageFile + " failed" , e);
		}
		return INVALID_CODE;
	}

	public static String crop(String imageFile, int x, int y, int width, int height) {
		if (!isValidInput(imageFile))
			return "";

		try (InputStream is = getInputStream(imageFile)) {
			BufferedImage image = ImageIO.read(is);
			BufferedImage croppedImage = image.getSubimage(x, y, width, height);
			writeImage(croppedImage, imageFile);
		}
		catch (Exception e) {
			log.error("crop " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	private static void writeImage(BufferedImage croppedImage, String destinationFilePathOrUrl) throws IOException {
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			ImageIO.write(croppedImage, CommonUtil.getFileType(destinationFilePathOrUrl), outStream);
			try (ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray())) {
				GXFile file = getGXFile(destinationFilePathOrUrl);
				file.create(inStream, true);
				file.close();
			}
		}
	}

	public static String flipHorizontally(String imageFile) {
		if (!isValidInput(imageFile))
			return "";

		try (InputStream is = getInputStream(imageFile)) {
			BufferedImage image = ImageIO.read(is);
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-image.getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage flippedImage = op.filter(image, null);
			writeImage(flippedImage, imageFile);
		}
		catch (Exception e) {
			log.error("flip horizontal " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String flipVertically(String imageFile) {
		if (!isValidInput(imageFile))
			return "";

		try (InputStream is = getInputStream(imageFile)) {
			BufferedImage image = ImageIO.read(is);
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage flippedImage = op.filter(image, null);
			writeImage(flippedImage, imageFile);
		}
		catch (Exception e) {
			log.error("flip vertical " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String resize(String imageFile, int width, int height, boolean keepAspectRatio) {
		if (!isValidInput(imageFile))
			return "";

		try (InputStream is = getInputStream(imageFile)) {
			BufferedImage image = ImageIO.read(is);
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
			writeImage(resizedImage, imageFile);
		}
		catch (Exception e) {
			log.error("resize " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String scale(String imageFile, short percent) {
		if (!isValidInput(imageFile))
			return "";

		try (InputStream is = getInputStream(imageFile)) {
			BufferedImage image = ImageIO.read(is);
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
		try (InputStream is = getInputStream(imageFile)) {
			BufferedImage image = ImageIO.read(is);
			BufferedImage rotatedImage = rotateImage(image, angle);
			writeImage(rotatedImage, imageFile);
		}
		catch (Exception e) {
			log.error("rotate " + imageFile + " failed" , e);
		}
		return imageFile;
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
