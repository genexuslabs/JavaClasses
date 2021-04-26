package com.genexus.webpanels;

import java.io.InputStream;
import java.util.Vector;

import com.genexus.fileupload.IFileItemIterator;
import com.genexus.fileupload.IFileItemStream;


public class FileItemCollection
{
  protected Vector<FileItem> vector;

  public FileItemCollection( IFileItemIterator lstParts, String rootPath)
  {	  
    vector = new Vector<FileItem>();
    if (lstParts != null)
    {
        try
        {
            while (lstParts.hasNext()) 
            {
                IFileItemStream item = lstParts.next();
                String completeFileName = item.getName();
                String name = completeFileName;
                if (!item.isFormField())
                {
					name = rootPath + com.genexus.PrivateUtilities.getTempFileName("tmp");
                }
                InputStream stream = item.openStream();
                FileItem fileItem = new FileItem(completeFileName, name, item.isFormField(), item.getFieldName(), stream);
                vector.add(fileItem);
            }
        }
        catch(Exception ex)
        {
        }
    }
  }

  public int getCount()
  {
    return vector.size();
  }
  
  public void clear()
  {
	  vector.clear();
  }

  public boolean hasitembyname( String name)
  {
    for (int i=0; i<vector.size(); i++)
    {
      FileItem fileItem = (FileItem) vector.get(i);
      if ( name.compareToIgnoreCase(fileItem.getFieldName())==0)
      	return true;
    }
    return false;
  }
  
  public FileItem itembyname( String name)
  {
    for (int i=0; i<vector.size(); i++)
    {
      FileItem fileItem = (FileItem) vector.get(i);
      if ( name.compareToIgnoreCase(fileItem.getFieldName())==0)
      	return fileItem;
    }
    return new FileItem();
  }

  public FileItem item( int i)
  {
    return (FileItem) vector.get( i);
  }

}