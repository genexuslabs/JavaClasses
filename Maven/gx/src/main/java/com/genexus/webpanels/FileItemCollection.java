// $Log: FileItemCollection.java,v $
// Revision 1.1.2.3  2008-05-26 15:03:53  alevin
// - Arreglo memory leak con los FileItems cuando se tenian inputs de tipo file en la pantalla.
// - Arreglo para que no se hagan redirects luego de que el response fue comiteado.
//
// Revision 1.1.2.2  2004/12/16 20:53:05  dmendez
// Soporte de confirmacion con upload controls
//
// Revision 1.1.2.1  2004/10/28 13:56:05  dmendez
// Revision inicial
//

package com.genexus.webpanels;

import java.util.Vector;
import java.io.InputStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import com.genexus.Application;
import com.genexus.GXDbFile;
import com.genexus.util.GXServices;

public class FileItemCollection
{
  protected Vector<FileItem> vector;

  public FileItemCollection( FileItemIterator lstParts, String rootPath)
  {	  
    vector = new Vector<FileItem>();
    if (lstParts != null)
    {
        try
        {
            while (lstParts.hasNext()) 
            {
                FileItemStream item = lstParts.next();
                String completeFileName = item.getName();
                String name = completeFileName;
                if (!item.isFormField())
                {
                    String type = GXDbFile.getFileType(completeFileName);
                    name = GXDbFile.getFileName(completeFileName) + (type.trim().length() == 0 ? "" : ".") + type;
                    if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
                    {
                        name = rootPath + name;
                    }
                    else
                    {
                        name = rootPath.replace(java.io.File.separator, "/") + GXDbFile.generateUri(name, true, false);
                    }
                }
                InputStream stream = item.openStream();
                FileItem fileItem = new FileItem(name, item.isFormField(), item.getFieldName(), stream);
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