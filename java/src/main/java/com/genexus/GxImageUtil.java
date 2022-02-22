package com.genexus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.genexus.util.GXFile;
import org.apache.logging.log4j.Logger;

public class GxImageUtil {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GxImageUtil.class);

	private static String getImageAbsolutePath(String imageFile){
		if (CommonUtil.isUploadPrefix(imageFile)) {
			return new GXFile(imageFile).getAbsolutePath();
		}
		String defaultPath = com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath();
		return imageFile.startsWith(defaultPath)? imageFile : defaultPath + imageFile.replace("/", File.separator);
	}

	private static InputStream getInputStream(String imageFile) throws IOException {
		return new GXFile(imageFile).getStream();
	}
	public static long getFileSize(String imageFile){

		return new GXFile(imageFile).getLength();
	}

	public static int getImageHeight(String imageFile) {
		try (InputStream is = getInputStream(imageFile)) {
			return ImageIO.read(is).getHeight();
		}
		catch (Exception e) {
			log.error("getImageHeight " + imageFile + " failed" , e);
			return 0;
		}
	}

	public static int getImageWidth(String imageFile) {
		try (InputStream is = getInputStream(imageFile)) {
			return ImageIO.read(is).getWidth();
		}
		catch (Exception e) {
			log.error("getImageWidth " + imageFile + " failed" , e);
			return 0;
		}
	}

	public static String crop(String imageFile, int x, int y, int width, int height){
		try {
			String absolutePath = getImageAbsolutePath(imageFile);
			BufferedImage image = ImageIO.read(new File(absolutePath));
			BufferedImage cropedImage = image.getSubimage(x, y, width, height);
			ImageIO.write(cropedImage, CommonUtil.getFileType(absolutePath), new FileOutputStream(absolutePath));
		}
		catch (IOException e) {
			log.error("crop " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String flipHorizontally(String imageFile){
		try {
			String absolutePath = getImageAbsolutePath(imageFile);
			BufferedImage image = ImageIO.read(new File(absolutePath));
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-image.getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage flipedImage = op.filter(image, null);
			ImageIO.write(flipedImage, CommonUtil.getFileType(absolutePath), new FileOutputStream(absolutePath));
		}
		catch (IOException e) {
			log.error("flip horizontal " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String flipVertically(String imageFile){
		try {
			String absolutePath = getImageAbsolutePath(imageFile);
			BufferedImage image = ImageIO.read(new File(absolutePath));
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			BufferedImage flipedImage = op.filter(image, null);
			ImageIO.write(flipedImage, CommonUtil.getFileType(absolutePath), new FileOutputStream(absolutePath));
		}
		catch (IOException e) {
			log.error("flip vertical " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String resize(String imageFile, int width, int height, boolean keepAspectRatio){
		try {
			String absolutePath = getImageAbsolutePath(imageFile);
			BufferedImage image = ImageIO.read(new File(absolutePath));
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
			ImageIO.write(resizedImage, CommonUtil.getFileType(absolutePath), new FileOutputStream(absolutePath));
		}
		catch (IOException e) {
			log.error("resize " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String scale(String imageFile, short percent){
		try {
			String absolutePath = getImageAbsolutePath(imageFile);
			BufferedImage image = ImageIO.read(new File(absolutePath));
			imageFile = resize(imageFile, image.getWidth() * percent / 100, image.getHeight() * percent / 100,true);
		}
		catch (IOException e) {
			log.error("scale " + imageFile + " failed" , e);
		}
		return imageFile;
	}

	public static String rotate(String imageFile, short angle){
		try {
			String absolutePath = getImageAbsolutePath(imageFile);
			BufferedImage image = ImageIO.read(new File(absolutePath));
			BufferedImage rotatedImage = rotateImage(image, angle);
			ImageIO.write(rotatedImage, CommonUtil.getFileType(absolutePath), new FileOutputStream(absolutePath));
		}
		catch (IOException e) {
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
