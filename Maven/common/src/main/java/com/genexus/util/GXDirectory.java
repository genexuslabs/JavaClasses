package com.genexus.util;

import java.io.*;

import com.genexus.common.interfaces.SpecificImplementation;

public class GXDirectory {
	private IGXFileInfo DirSource;
	private int ErrCode;
	private String ErrDescription;
	private boolean ret;

	public GXDirectory() {
	}

	public GXDirectory(String DirName) {
		DirSource = new GXFileInfo(new File(DirName));
	}

	public GXDirectory(IGXFileInfo Dir) {
		DirSource = Dir;
	}
	public void setDirectoryInfo(IGXFileInfo dirInfo){
		DirSource = dirInfo;
	}
	public void setSource(String DirName)
	{
		DirSource = new GXFileInfo(new File(DirName));
	}

	public String getSource()
	{
		if (DirSource == null)
		{
			return "";
		}
		return DirSource.getPath();
	}

	public void create()
	{
		if (sourceSeted())
		{
			resetErrors();
			if (DirSource.isDirectory() && DirSource.exists()) {
				ErrCode = 3;
				ErrDescription = "Directory already exists";
			}
			else {
				try {
					ret = DirSource.mkdir();
					if (!ret)
						setUnknownError();
				}
				catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
				}
			}
		}
	}

	public void delete()
	{
		if (sourceSeted())
		{
			resetErrors();
			if (!DirSource.isDirectory() && !DirSource.exists()) {
				ErrCode = 2;
				ErrDescription =
						"The directory couldn't be deleted; directory does not exist";
			}
			else if (DirSource.list().length!=0)
			{
				ErrCode = 4;
				ErrDescription ="The directory couldn't be deleted, directory not empty";
			}
			else {
				try {
					ret = DirSource.delete();
					if (!ret)
						setUnknownError();
				}
				catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
				}
			}
		}
	}

	public boolean exists()
	{
		if (sourceSeted())
		{
			try {
				resetErrors();
				return DirSource.exists();
			}
			catch (SecurityException e) {
				ErrCode = 100;
				ErrDescription = e.getMessage();
				return false;
			}
		}
		else
			return false;
	}

	public void rename(String FileName)
	{
		if (sourceSeted())
		{
			resetErrors();
			if (!DirSource.isDirectory() && !DirSource.exists()) {
				ErrCode = 2;
				ErrDescription =
						"The directory couldn't be renamed; directory does not exist";
			}
			else if (new File(FileName).exists())
			{
				ErrCode = 3;
				ErrDescription = "Directory already exists";
			}
			else {
				try {
					ret = DirSource.renameTo(FileName);
					if (!ret)
						setUnknownError();
				}
				catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
				}
			}
		}
	}

	public String getName()
	{
		if (sourceSeted())
		{
			resetErrors();
			if (!DirSource.isDirectory() && !DirSource.exists()) {
				ErrCode = 2;
				ErrDescription = "Directory does not exist";
				return "";
			}
			else
				return DirSource.getName();
		}
		else
			return "";
	}

	public String getAbsoluteName()
	{
		if (sourceSeted())
		{
			resetErrors();
			if (!DirSource.isDirectory() && !DirSource.exists())
			{
				ErrCode = 2;
				ErrDescription = "Directory does not exist";
				return "";
			}
			else
			{
				try {
					return DirSource.getAbsolutePath();
				}
				catch (SecurityException e) {
					ErrCode = 100;
					ErrDescription = e.getMessage();
					return "";
				}
			}
		}
		else
			return "";
	}

	public GXFileCollection getFiles()
	{
		return getFiles("");
	}


	public GXFileCollection getFiles(String strFilter)
	{
		if (sourceSeted())
		{
			resetErrors();
			GXFileCollection gxfiles = DirSource.listFiles(strFilter);
			if (gxfiles == null)
			{
				ErrCode = 2;
				ErrDescription = "Directory does not exist";
			}
			return gxfiles;
		}
		else
			return new GXFileCollection();
	}

	public GXDirectoryCollection getDirectories()
	{
		if (sourceSeted())
		{
			resetErrors();
			GXDirectoryCollection DirCollection = DirSource.listDirectories();

			if (DirCollection == null)
			{
				ErrCode = 2;
				ErrDescription = "Directory does not exist";
			}
			return DirCollection;
		}
		else
			return new GXDirectoryCollection();
	}

	public int getErrCode()
	{
		return ErrCode;
	}

	public String getErrDescription()
	{
		return ErrDescription;
	}

	private void resetErrors()
	{
		ErrCode = 0;
		ErrDescription = "";
	}

	private boolean sourceSeted()
	{
		if (DirSource==null)
		{
			ErrCode = 1;
			ErrDescription = "Invalid Directory instance";
			return false;
		}
		else
			return true;
	}

	private void setUnknownError()
	{
		ErrCode = -1;
		ErrDescription = "Unknown error";
	}

	public static String applicationdatapath()
	{
		return SpecificImplementation.GXDirectory.getApplicationDataPath();
	}

	public static String temporaryfilespath()
	{
		return SpecificImplementation.GXDirectory.getTemporaryFilesPath();
	}

	public static String externalfilespath()
	{
		return SpecificImplementation.GXDirectory.getExternalFilesPath();
	}

}


class Filter implements FilenameFilter {
	protected String pattern;
	public Filter (String str) {
		if (str!=null && str.length() >=1 && str.charAt(0)=='*')
		{
			pattern = str.substring(1);
		}else
		{
			pattern = str;
		}
	}
	public boolean accept (File dir, String name) {
		return name.toLowerCase().endsWith(pattern.toLowerCase());
	}
}
