package com.genexus.webpanels;

import java.io.File;
import java.util.UUID;

import com.genexus.*;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpResponse;
import com.genexus.util.CacheAPI;
import com.genexus.util.GXServices;

import json.org.json.JSONArray;
import json.org.json.JSONObject;



public class GXObjectUploadServices extends GXWebObjectStub
{   
    protected void doExecute(HttpContext context) throws Exception
    {
        WebApplicationStartup.init(Application.gxCfg, context);
        context.setStream();
        
		try
		{
			if (context.isMultipartContent())
			{
				ModelContext modelContext =  new ModelContext(Application.gxCfg);
				modelContext.setHttpContext(context);
				ModelContext.getModelContext().setHttpContext(context);
				context.setContext(modelContext);

				context.setContentType("text/plain");
				FileItemCollection postedFiles = context.getHttpRequest().getPostedparts();
				JSONArray jsonArray = new JSONArray();
				for (int i = 0, len = postedFiles.getCount(); i < len; i++)
				{
					FileItem file = postedFiles.item(i);
					if (!file. isFormField())
					{
						String fileName = "";
						String[] files = file.getName().split("\\\\");
						if (files.length > 0)
							fileName = files[files.length - 1];
						else
							fileName = file.getName();

						long fileSize = file.getSize(); 

						String ext = CommonUtil.getFileType(fileName);
						String savedFileName = "";
						String url = "";
						if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
						{
							String fileDirPath = context.getDefaultPath() + File.separator + "WEB-INF" + File.separatorChar + Application.getClientPreferences().getTMPMEDIA_DIR();
							savedFileName = PrivateUtilities.getTempFileName(fileDirPath, CommonUtil.getFileName(fileName), ext == null || ext.length() == 0 ? "tmp" : ext);;
							file.write(savedFileName);
							url = GXDbFile.pathToUrl(savedFileName, context);
							BlobsCleaner.getInstance().addBlobFile(savedFileName);
						}
						else
						{
							savedFileName = file.getPath();
							url = file.getAbsolutePath();
							BlobsCleaner.getInstance().addBlobFile(fileName);
						}

						JSONObject jObj = new JSONObject();
						jObj.put("name", fileName);
						jObj.put("size", fileSize);
						jObj.put("url", url);
						jObj.put("type", HttpResponse.getContentType(fileName));
						jObj.put("extension", ext);
						jObj.put("thumbnailUrl", url);
						jObj.put("path", savedFileName);
						jsonArray.put(jObj);
					}
				}
				JSONObject jObjResponse = new JSONObject();
				jObjResponse.put("files", jsonArray);
				context.writeText(jObjResponse.toString());
				context.getResponse().flushBuffer();
			}
			else
			{
				String contentType = context.getHeader("Content-Type");
				String ext = getExtension(contentType);
				String tempFileName = com.genexus.PrivateUtilities.getTempFileName(ext);
				String filePath = tempFileName;		
				if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null)
				{
					filePath = Preferences.getDefaultPreferences().getBLOB_PATH() + tempFileName;		
				}
				else
				{
					filePath = Preferences.getDefaultPreferences().getBLOB_PATH().replace(java.io.File.separator, "/") + 	tempFileName;
				}				
				FileItem fileItem = new FileItem(filePath, false, "", context.getRequest().getInputStream());
				filePath = fileItem.getPath();
				String keyId = UUID.randomUUID().toString().replace("-","");
				CacheAPI.files().set(keyId, filePath, CommonUtil.UPLOAD_TIMEOUT );
				context.getResponse().setContentType("application/json");
				context.getResponse().setStatus(201);
				context.getResponse().setHeader("GeneXus-Object-Id", keyId);
				JSONObject jObj = new JSONObject();
				jObj.put("object_id", CommonUtil.UPLOADPREFIX + keyId);
				context.writeText(jObj.toString());
				context.getResponse().flushBuffer();
				return;
			}
		}
		catch (Throwable e)
		{
			context.sendResponseStatus(404, e.getMessage());
		}
    }
	
	protected boolean IntegratedSecurityEnabled( )
	{
	return com.genexus.Application.getClientPreferences().getProperty("EnableIntegratedSecurity", "0").equals("1");
	}

	protected int IntegratedSecurityLevel( )
	{
	return SECURITY_GXOBJECT;
	}

	protected String IntegratedSecurityPermissionPrefix( )
	{
	return "";
	}   

	private String getExtension(String contentType)
	{
		if (contentType.startsWith("image/jpeg"))
		{
			return "jpg";
		}
		if (contentType.startsWith("image/tiff"))
		{
			return "tif";
		}
		if (contentType.startsWith("image/png"))
		{
			return "png";
		}
		if (contentType.startsWith("audio/x-wav"))
		{
			return "wav";
		}
		if (contentType.startsWith("video/mpeg"))
		{
			return "mpg";
		}
		if (contentType.startsWith("video/quicktime"))
		{
			return "mov";
		}	   
		if (contentType.startsWith("audio/x-caf"))
		{
			return "caf";
		}
		if (contentType.startsWith("audio/mpeg"))
		{
			return "mp3";
		}
		if (contentType.startsWith("audio/x-pn-realaudio"))
		{
			return "ram";
		}
		if (contentType.startsWith("application/pdf"))
		{
			return "pdf";
		}	   
		if (contentType.startsWith("text/rtf"))
		{
			return "rtf";
		}	   
		if (contentType.startsWith("text/plain"))
		{
			return "txt";
		}
		if (contentType.startsWith("image/bmp"))
		{
			return "bmp";
		}	   
		if (contentType.startsWith("image/gif"))
		{
			return "gif";
		}	   
		if (contentType.startsWith("audio/mp4"))
		{
			return "m4a";
		}	   
		if (contentType.startsWith("audio/3gpp"))
		{
			return "a3gpp";
		}	   
		if (contentType.startsWith("video/3gpp2"))
		{
			return "3g2";
		}	   
		if (contentType.startsWith("video/3gpp"))
		{
			return "3gp";
		}	   
		if (contentType.startsWith("video/mp4"))
		{
			return "mp4";
		}	   
		if (contentType.startsWith("video/x-msvideo"))
		{
			return "avi";
		}
		if (contentType.startsWith("video/x-divx"))
		{
			return "divx";
		}	   
		if (contentType.startsWith("application/zip"))
		{
			return "zip";
		}	   
		if (contentType.startsWith("application/x-gzip"))
		{
			return "gz";
		}	   
		if (contentType.startsWith("application/x-tar"))
		{
			return "tar";
		}	   
		if (contentType.startsWith("application/x-rar-compressed"))
		{
			return "rar";
		}
		return "tmp";
	}
   protected void init(HttpContext context )
   {
   }	   
}
