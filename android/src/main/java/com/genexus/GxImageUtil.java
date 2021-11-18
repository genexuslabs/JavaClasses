package com.genexus;


import com.artech.base.services.AndroidContext;

public class GxImageUtil {

	public static long getFileSize(String imageFile){
		return AndroidContext.ApplicationContext.getAndroidImageUtil().getFileSize(imageFile);
	}

	public static int getImageHeight(String imageFile) {
		return AndroidContext.ApplicationContext.getAndroidImageUtil().getImageHeight(imageFile);
	}

	public static int getImageWidth(String imageFile) {
		return AndroidContext.ApplicationContext.getAndroidImageUtil().getImageWidth(imageFile);
	}

	public static String crop(String imageFile, int x, int y, int width, int height){
		return AndroidContext.ApplicationContext.getAndroidImageUtil().crop(imageFile, x, y, width, height);
	}

	public static String flipHorizontally(String imageFile){
		return AndroidContext.ApplicationContext.getAndroidImageUtil().flipHorizontally(imageFile);
	}

	public static String flipVertically(String imageFile){
		return AndroidContext.ApplicationContext.getAndroidImageUtil().flipVertically(imageFile);
	}

	public static String resize(String imageFile, int width, int height, boolean keepAspectRatio){
		return AndroidContext.ApplicationContext.getAndroidImageUtil().resize(imageFile, width, height, keepAspectRatio);
	}

	public static String scale(String imageFile, short percent){
		return AndroidContext.ApplicationContext.getAndroidImageUtil().scale(imageFile, percent);
	}

	public static String rotate(String imageFile, short angle){
		return AndroidContext.ApplicationContext.getAndroidImageUtil().rotate(imageFile, angle);
	}


}

