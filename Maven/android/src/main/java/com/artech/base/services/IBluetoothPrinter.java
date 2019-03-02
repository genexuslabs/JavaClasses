package com.artech.base.services;

import java.io.FileInputStream;

public interface IBluetoothPrinter
{
	public boolean print( FileInputStream in);
	  
	public void cleanUp();
	   
	
}
