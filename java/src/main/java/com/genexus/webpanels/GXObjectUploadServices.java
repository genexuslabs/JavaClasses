package com.genexus.webpanels;


import com.genexus.*;
import com.genexus.internet.HttpContext;

import json.org.json.JSONArray;
import json.org.json.JSONObject;
import com.genexus.ws.rs.core.*;
import org.apache.commons.io.FilenameUtils;


public class GXObjectUploadServices extends GXWebObjectStub
{   
	boolean isRestCall = false;
	IResponseBuilder builder = null;
	public IResponseBuilder doInternalRestExecute(HttpContext context) throws Exception
	{
		isRestCall = true;
		doExecute(context);
		return builder;
	}
	public void doInternalExecute(HttpContext context) throws Exception
	{
		doExecute(context);
	}
    protected void doExecute(HttpContext context) throws Exception
    {
		String savedFileName = "";
		String fileName = "";
		String ext = "";
        WebApplicationStartup.init(com.genexus.Application.gxCfg, context);
        context.setStream();
		String keyId;
		try
		{
			String fileDirPath = Preferences.getDefaultPreferences().getPRIVATE_PATH();
				ModelContext modelContext =  new ModelContext(com.genexus.Application.gxCfg);
				modelContext.setHttpContext(context);
				ModelContext.getModelContext().setHttpContext(context);
				context.setContext(modelContext);

			if (((HttpContextWeb) context).isMultipartContent())
			{
				context.setContentType("text/plain");
				FileItemCollection postedFiles = context.getHttpRequest().getPostedparts();
				JSONArray jsonArray = new JSONArray();
				for (int i = 0, len = postedFiles.getCount(); i < len; i++)
				{
					keyId = HttpUtils.getUploadFileKey();
					FileItem file = postedFiles.item(i);
					if (!file.isFormField())
					{
						ext = CommonUtil.getFileType(file.getName());
						fileName = CommonUtil.getFileName(file.getName()) + "." + ext;
						long fileSize = file.getSize();
						savedFileName = file.getPath();
						JSONObject jObj = new JSONObject();
						jObj.put("name", fileName);
						jObj.put("size", fileSize);
						jObj.put("extension", ext);
						jObj.put("path", HttpUtils.getUploadFileId(keyId));
						jsonArray.put(jObj);
						if (!savedFileName.isEmpty()){
							HttpUtils.CacheUploadFile(keyId, savedFileName, fileName, ext);
						}
					}
				}
				JSONObject jObjResponse = new JSONObject();
				jObjResponse.put("files", jsonArray);
				((HttpContextWeb) context).writeText(jObjResponse.toString());
				context.getResponse().flushBuffer();
			}
			else
			{
				keyId = HttpUtils.getUploadFileKey();
				String contentType = context.getHeader("Content-Type");
				String gxFileName = context.getHeader("x-gx-filename");
				String fName = "";
				if (!gxFileName.isEmpty()) {
					ext = FilenameUtils.getExtension(gxFileName);
					fName = FilenameUtils.getBaseName(gxFileName);
				}
				else {
					ext = getExtension(contentType);
				}
				fileName = com.genexus.PrivateUtilities.getTempFileName("", fName, "tmp");
				String filePath = fileDirPath + fileName;
				fileName = fileName.replaceAll(".tmp", "." + ext);
				FileItem fileItem = new FileItem(filePath, false, "", context.getRequest().getInputStream().getInputStream());
				savedFileName = fileItem.getPath();
				JSONObject jObj = new JSONObject();
				jObj.put("object_id", HttpUtils.getUploadFileId(keyId));
				if (!isRestCall) {
					context.getResponse().setContentType("application/json");
					context.getResponse().setStatus(201);
					context.getResponse().setHeader("GeneXus-Object-Id", keyId);
					((HttpContextWeb) context).writeText(jObj.toString());
					context.getResponse().flushBuffer();
				}
				else {
					String jsonResponse = jObj.toString();
					builder = Response.statusWrapped(201).entityWrapped(jsonResponse);
					builder.header("GeneXus-Object-Id", keyId);
				}
				if (!savedFileName.isEmpty()) {
					HttpUtils.CacheUploadFile(keyId, savedFileName, fileName, ext);
				}
			}
		}
		catch (Throwable e)
		{
			context.sendResponseStatus(404, e.getMessage());
		}
		finally {
			if (!isRestCall)
			ModelContext.deleteThreadContext();
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

	protected String EncryptURLParameters() {return "NO";};

	private String getExtension(String contentType)
	{
		if (contentType.startsWith("image/jpg")||contentType.startsWith("application/jpg"))
		{
			return "jpg";
		}
		if (contentType.startsWith("image/jpeg")||contentType.startsWith("application/jpeg"))
		{
			return "jpeg";
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
		if (contentType.startsWith("audio/vnd.rn-realaudio"))
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
		if (contentType.startsWith("audio/x-m4a"))
		{
			return "m4a";
		}
		if (contentType.startsWith("audio/aiff"))
		{
			return "aif";
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
