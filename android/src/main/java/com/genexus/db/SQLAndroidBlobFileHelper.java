package com.genexus.db;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.artech.base.services.AndroidContext;
import com.genexus.CommonUtil;

public class SQLAndroidBlobFileHelper 
{

    private static List<String> mInsertedBLOBs = new ArrayList<String>();
    private static List<String> mDeletedBLOBs = new ArrayList<String>();
  
    private static List<String> mInsertedBLOBsNames = new ArrayList<String>();
    private static List<String> mDeletedBLOBsNames = new ArrayList<String>();
  
    
    public static void addInsertedBlobPath(String blobInserted)
    {
    	// remove file:// prefix
    	if (blobInserted!=null && blobInserted.length()!=0)
    	{
    		if (blobInserted.toLowerCase().startsWith("file://"))
    		{
    			URI myURI = URI.create(blobInserted);
    			if ("file".equalsIgnoreCase(myURI.getScheme()))
    			{
    				blobInserted = myURI.getPath();
    			}
    			//blobInserted = blobInserted.substring(7);
    		}
    		mInsertedBLOBs.add(fixPathToKbImages(blobInserted));
    	}
    }
       
    public static void addDeletedBlobPath(String blobDeleted)
    {
    	if (blobDeleted!=null && blobDeleted.length()!=0)
    	{
    		// 	remove file:// prefix
    		if (blobDeleted.toLowerCase().startsWith("file://"))
    		{
    			URI myURI = URI.create(blobDeleted);
    			if ("file".equalsIgnoreCase(myURI.getScheme()))
    			{
    				blobDeleted = myURI.getPath();
    			}
    		}
    		mDeletedBLOBs.add(fixPathToKbImages(blobDeleted));
    	}
    }
       
    public static List<String> getInsertedBlobs()
    {
        return mInsertedBLOBs;
    }
    
    public static List<String> getDeletedBlobs()
    {
        return mDeletedBLOBs;
    }
    
      
    private static String fixPathToKbImages(String imagePath)
    {
    	// if file from resources copy to an actual file and keep reference.
		if ( !(imagePath.toLowerCase().startsWith("/") )
				&& imagePath.toLowerCase().contains("resources") )
		{
			//InputStream is = null;
			int id = AndroidContext.ApplicationContext.getDataImageResourceId(imagePath); //$NON-NLS-1$
			if (id != 0)
			{
				String blobBasePath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
				String fileResourceNameNew = "kbfile_" + imagePath.replace("/", "_");
				fileResourceNameNew = blobBasePath + "/" + CommonUtil.getFileName(fileResourceNameNew)+ "." + CommonUtil.getFileType(fileResourceNameNew);

				imagePath = fileResourceNameNew;
			}
		}
		return imagePath;
    }
    
    
    public static void removeDeletedBlobsOnCommit()
    {
    	getBlobsNames();
    	for (String blobToDelete : mDeletedBLOBs)
    	{
    	    if (blobToDelete!=null && blobToDelete.length()>0)
    	    {
    	    	//only delete file if is was not added also
    	    	if (!mInsertedBLOBs.contains(blobToDelete)
    	    			&& !mInsertedBLOBsNames.contains(getBlobsName(blobToDelete)) )
    	    	{
    	    		File blobToDeleteFile = new File(blobToDelete);
    	    		blobToDeleteFile.delete();
    	    	}
    	    	
    	    }
    	}
    	mInsertedBLOBs = new ArrayList<String>();
    	mDeletedBLOBs = new ArrayList<String>();
    	mInsertedBLOBsNames = new ArrayList<String>();
    	mDeletedBLOBsNames = new ArrayList<String>();
    }
    
    public static void removeInsertedBlobsOnRollback()
    {
    	getBlobsNames();
    	for (String blobInsertedToDelete : mInsertedBLOBs)
    	{
    	    if (blobInsertedToDelete!=null && blobInsertedToDelete.length()>0)
    	    {
    	    	//only delete file if is was not deleted also
    	    	if (!mDeletedBLOBs.contains(blobInsertedToDelete)
    	    			&& !mDeletedBLOBs.contains(getBlobsName(blobInsertedToDelete)) )
    	    	{
    	    		File blobInsertedToDeleteFile = new File(blobInsertedToDelete);
    	    		blobInsertedToDeleteFile.delete();
    	    	}
    	    	
    	    }
    	}
    	mInsertedBLOBs = new ArrayList<String>();
    	mDeletedBLOBs = new ArrayList<String>();
    	mInsertedBLOBsNames = new ArrayList<String>();
    	mDeletedBLOBsNames = new ArrayList<String>();
    }
    
    
    private static void getBlobsNames()
    {
    	for (String blobInserted : mInsertedBLOBs)
    	{
    		mInsertedBLOBsNames.add(getBlobsName(blobInserted) );
    	}
    	for (String blobDeleted : mDeletedBLOBs)
    	{
    		mDeletedBLOBsNames.add(getBlobsName(blobDeleted));
    	}
    }
    
    private static String getBlobsName(String path)
    {
    	return CommonUtil.getFileName(path)+ "." + CommonUtil.getFileType(path);
    }
    
}
