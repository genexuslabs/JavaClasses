package com.genexus.webpanels;
import com.genexus.CommonUtil;
import com.genexus.internet.HttpContext;
import com.genexus.util.CacheAPI;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class HttpUtils
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(HttpUtils.class);

  public static Hashtable<String, String[]> parseMultipartPostData(FileItemCollection fileItemCollection)
  {
    Hashtable<String, String[]> ht = new Hashtable<>();
    for (int i=0; i<fileItemCollection.getCount(); i++)
    {
      FileItem item = fileItemCollection.item(i);
      if (item.isFormField())
      {
		try
		{
			pushValue( ht, item.getFieldName(), item.getString("UTF-8"));
		}
		catch( java.io.UnsupportedEncodingException e)
		{
			pushValue( ht, item.getFieldName(), "");
		}
	  }
      else
      {
        String itemFilePath;
        if (item.getSize() == 0)
        {
          itemFilePath = "";
        }
        else
        {
			String keyId = HttpUtils.getUploadFileKey();
			itemFilePath = HttpUtils.getUploadFileId(keyId);
			HttpUtils.CacheUploadFile(keyId, item.getPath(), item.getName(), CommonUtil.getFileType(item.getName()));
        }
        pushValue(ht, item.getFieldName(), itemFilePath);
      }
    }
    return ht;
  }

  public static Hashtable<String, String[]> parseQueryString(String s)
    {
	if(s == null)
	    throw new IllegalArgumentException();
	Hashtable<String, String[]> ht = new Hashtable<>();
	StringBuffer sb = new StringBuffer();
	String key;
	for(StringTokenizer st = new StringTokenizer(s, "&"); st.hasMoreTokens();)
	{
	    String pair = st.nextToken();
	    int pos = pair.indexOf('=');
	    if(pos == -1)
		throw new IllegalArgumentException();
	    key = parseName(pair.substring(0, pos), sb);
	    String val = parseName(pair.substring(pos + 1, pair.length()), sb);
        //Remove known malicious chars
	    pushValue( ht, key, val.replace("\0",""));
	}
	return ht;
    }

    public static void pushValue( Hashtable<String, String[]> ht, String key, String val)
    {
      String valArray[] = null;
      if(ht.containsKey(key))
      {
	  String oldVals[] = (String[])ht.get(key);
	  valArray = new String[oldVals.length + 1];
	  for(int i = 0; i < oldVals.length; i++)
	      valArray[i] = oldVals[i];
	  valArray[oldVals.length] = val;
      }
      else
      {
	  valArray = new String[1];
	  valArray[0] = val;
      }
      ht.put(key, valArray);
    }

    public static Hashtable<String, String[]> parsePostData(HttpServletRequest request)
    {
    	String paramName = null;
    	String paramValues[] = null;
    	Hashtable<String, String[]> ht = new Hashtable<>();
    	String value;
    	for(Enumeration params = request.getParameterNames(); params.hasMoreElements();)
    	{
    		paramName = (String)params.nextElement();
    		paramValues = request.getParameterValues(paramName);
    		value = "";
    		for(int i = 0; i < paramValues.length; i++)
    			value = (new StringBuilder(String.valueOf(value))).append(paramValues[i].toString()).toString();
    		pushValue(ht, paramName, value);
    	}
    	return ht;
    }

    public static Hashtable<String, String[]> parsePostData(ServletInputStream in)
    {
		if(in == null)
			throw new IllegalArgumentException();
		try
		{
			return parseQueryString(IOUtils.toString(in, "8859_1"));
		}
		catch(IOException e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}
    }

    private static String parseName(String s, StringBuffer sb)
    {
	sb.setLength(0);
		java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
		try
		{
			for(int i = 0; i < s.length(); i++)
			{
				char c = s.charAt(i);
				switch(c)
				{
				case 43: // '+'
					stream.write(' ');
					//                sb.append(' ');
					break;

				case 37: // '%'
					try
					{
						stream.write(Integer.parseInt(s.substring(i + 1, i + 3), 16));
						//                    sb.append((char)Integer.parseInt(s.substring(i + 1, i + 3), 16));
						i += 2;
						break;
					}
					catch(NumberFormatException numberformatexception)
					{
						throw new IllegalArgumentException();
					}
					catch(StringIndexOutOfBoundsException stringindexoutofboundsexception)
					{
						String rest = s.substring(i);
						stream.write(s.substring(i).getBytes("UTF8"));
						//                    sb.append(rest);
						if(rest.length() == 2)
							i++;
					}
					break;

				default:
					stream.write(c);
					//                sb.append(c);
					break;
				}
			}
		}catch(java.io.IOException e)
		{ // Si por alguna raz�n falla el decode con UTF-8, utilizo el encoding de la VM
			return parseNameVMEncoding(s, sb);
		}
		try
		{
			sb.append(new String(stream.toByteArray(), "UTF8"));
		}catch(UnsupportedEncodingException e)
		{
			sb.append(stream.toByteArray());
		}
	return sb.toString();
    }

	private static String parseNameVMEncoding(String s, StringBuffer sb)
    {
	sb.setLength(0);
	for(int i = 0; i < s.length(); i++)
	{
	    char c = s.charAt(i);
	    switch(c)
	    {
	    case 43: // '+'
		sb.append(' ');
		break;

	    case 37: // '%'
		try
		{
		    sb.append((char)Integer.parseInt(s.substring(i + 1, i + 3), 16));
		    i += 2;
		    break;
		}
		catch(NumberFormatException numberformatexception)
		{
		    throw new IllegalArgumentException();
		}
		catch(StringIndexOutOfBoundsException stringindexoutofboundsexception)
		{
		    String rest = s.substring(i);
		    sb.append(rest);
		    if(rest.length() == 2)
			i++;
		}
		break;

	    default:
		sb.append(c);
		break;
	    }
	}

	return sb.toString();
    }


    public static StringBuffer getRequestURL(HttpServletRequest req)
    {
	StringBuffer url = new StringBuffer();
	String scheme = req.getScheme();
	int port = req.getServerPort();
	String urlPath = req.getRequestURI();
	url.append(scheme);
	url.append("://");
	url.append(req.getServerName());
	if(scheme.equals("http") && port != 80 || scheme.equals("https") && port != 443)
	{
	    url.append(':');
	    url.append(req.getServerPort());
	}
	url.append(urlPath);
	return url;
    }

    static Hashtable nullHashtable = new Hashtable();

	public static String getUploadFileKey(){
		return UUID.randomUUID().toString().replace("-","");
	}

	public static String getUploadFileId(String keyId) {
		return CommonUtil.UPLOADPREFIX + keyId;
	}

	public static void CacheUploadFile(String keyId, String uploadFilePath, String fileName, String fileExtension) {
		try {
			JSONObject jObj = new JSONObject();
			jObj.put("path", uploadFilePath);
			jObj.put("fileName", fileName);
			jObj.put("fileExtension", fileExtension);
			CacheAPI.files().set(keyId, jObj.toString(), CommonUtil.UPLOAD_TIMEOUT);
		}
		catch (JSONException e) {
			log.debug("Error Caching Upload File", e);
		}
		BlobsCleaner.getInstance().addBlobFile(uploadFilePath);
	}

	public static boolean isUploadRequest(HttpContext context) {
		return context.getRequest().getRequestURI().endsWith("/gxobject");
	}

}
