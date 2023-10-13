package com.genexus;

import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestGxImageUtil {

	private String FILE_NAME = "bird-thumbnail.jpg";
	private String FILE_NAME_COPY = "bird-thumbnail-%s-%s.jpg";

	private int IMAGE_HEIGHT = 900;
	private int IMAGE_WIDTH = 720;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testImageWidth()
	{
		int imageWidth = GxImageUtil.getImageWidth(initialize("imageWidth"));
		Assert.assertEquals(IMAGE_WIDTH, imageWidth);
	}

	private String initialize(String name)
	{
		Connect.init();
		Application.init(GXcfg.class);
		LogManager.initialize(".");

		String copiedFileName = String.format(FILE_NAME_COPY, name, java.util.UUID.randomUUID().toString());
		File resourcesDirectory = new File("src/test/resources");
		Path originalFileLocation = Paths.get(resourcesDirectory.getPath(), FILE_NAME);
		Path copyFileLocation = Paths.get(tempFolder.getRoot().getPath(), copiedFileName);

		try {
			Files.copy(originalFileLocation, copyFileLocation);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return copyFileLocation.toString();
	}

	@Test
	public void testImageHeight()
	{
		String fileName = initialize("testHeight");
		int imageHeight = GxImageUtil.getImageHeight(fileName);
		Assert.assertEquals(IMAGE_HEIGHT, imageHeight);
	}

	@Test
	public void testImageScale()
	{
		String fileName = initialize("scaled");
		short scale = 50;

		String imagePath = GxImageUtil.scale(fileName, scale);

		int imageHeight = GxImageUtil.getImageHeight(imagePath);
		Assert.assertEquals(IMAGE_HEIGHT * scale / 100, imageHeight);

		int imageWidth = GxImageUtil.getImageWidth(imagePath);
		Assert.assertEquals(IMAGE_WIDTH * scale / 100, imageWidth);
	}

	@Test
	public void testImageCrop()
	{
		String fileName = initialize("croped");
		String imagePath = GxImageUtil.crop(fileName, 10, 10, 300, 400);

		int imageHeight = GxImageUtil.getImageHeight(imagePath);
		Assert.assertEquals(400, imageHeight);

		int imageWidth = GxImageUtil.getImageWidth(imagePath);
		Assert.assertEquals(300, imageWidth);
	}


	@Test
	public void testImageResize()
	{
		String fileName = initialize("resized");
		String imagePath = GxImageUtil.resize(fileName, 300, 400, false);

		int imageHeight = GxImageUtil.getImageHeight(imagePath);
		Assert.assertEquals(400, imageHeight);

		int imageWidth = GxImageUtil.getImageWidth(imagePath);
		Assert.assertEquals(300, imageWidth);
	}


	@Test
	public void testImageFlipHorizontally()
	{
		String fileName = initialize("flippedHorizontally");
		String imagePath = GxImageUtil.flipHorizontally(fileName);

		int imageHeight = GxImageUtil.getImageHeight(imagePath);
		Assert.assertEquals(IMAGE_HEIGHT, imageHeight);

		int imageWidth = GxImageUtil.getImageWidth(imagePath);
		Assert.assertEquals(IMAGE_WIDTH, imageWidth);
	}

	@Test
	public void testImageFlipVertically()
	{
		String fileName = initialize("flippedVertically");
		String imagePath = GxImageUtil.flipVertically(fileName);

		int imageHeight = GxImageUtil.getImageHeight(imagePath);
		Assert.assertEquals(IMAGE_HEIGHT, imageHeight);

		int imageWidth = GxImageUtil.getImageWidth(imagePath);
		Assert.assertEquals(IMAGE_WIDTH, imageWidth);
	}

	@Test
	public void testImageFileSize()
	{
		String fileName = initialize("imageSize");
		long fileSize = GxImageUtil.getFileSize(fileName);
		Assert.assertEquals(113974, fileSize);

	}
}
