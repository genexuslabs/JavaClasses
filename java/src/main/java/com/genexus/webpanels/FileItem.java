package com.genexus.webpanels;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;

import com.genexus.Application;
import com.genexus.util.GXFile;
import com.genexus.util.GXServices;

public class FileItem
{ 
	private GXFile gxFile;
	private String fieldName;
	private byte[] fieldByteString;
	private boolean formField;
	
  public FileItem()
  {
  }

  public FileItem(String name, boolean formField, String fieldName, InputStream stream)
  {
	  this.fieldName = fieldName;
	  this.formField = formField;  	
  	if (formField)
  	{
  		try
  		{
  			fieldByteString = IOUtils.toByteArray(stream);
  		}
		  catch(IOException e)
		  {
		  	System.err.println("Error converting to byte[] in FileItem: " + e);  			
		  }
  	}
  	else
  	{
	  	gxFile = new GXFile(name, true);
	  	gxFile.create(stream);
	  }
  }
  
  public String getFieldName()
  {
	  return fieldName;
  }
  
  public String getName()
  {
  	if (gxFile != null)
  	{
	  	return gxFile.getName();
	  }
	  return "";
  }
  
  public String getPath()
	{
  	if (gxFile != null)
  	{
  		return gxFile.getFilePath();
		}
		return "";
	}
	
	public long getSize()
  {
  	if (gxFile != null)
  	{  	
  		return gxFile.getLength();
  	}
  	return 0;
  }
  
  public String getString(String charset) throws UnsupportedEncodingException
  {
  	if (gxFile != null)
  	{
  		return new String(gxFile.toBytes(), charset);
		}
		else
		{
		  try
		  {
		  	return new String(fieldByteString, charset);
		  }
		  catch(IOException e)
		  {
		  	System.err.println("Error reading FileItem String: " + e);
		  	return "";
		  }			
		}
	}

  public boolean isFormField()
  {
  	return formField;
  }
  
  public void delete()
  {
  	if (gxFile != null)
  	{
	  	gxFile.delete();
	  }
  }
  
  public void write(String wFile)
  {
  	if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
  	{
  		gxFile.copy(wFile);
  		gxFile.delete();
  	}
  }
}
