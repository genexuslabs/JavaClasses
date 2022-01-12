package com.artech.base.services;

public interface IAndroidImageUtil
{
	//properties
	public  long getFileSize(String imageFile);
	public  int getImageHeight(String imageFile);
	public  int getImageWidth(String imageFile);

	//methods
	public  String crop(String imageFile, int x, int y, int width, int height);
	public  String flipHorizontally(String imageFile);
	public  String flipVertically(String imageFile);
	public  String resize(String imageFile, int width, int height, boolean keepAspectRatio);
	public  String scale(String imageFile, short percent);
	public  String rotate(String imageFile, short angle);


	
}
