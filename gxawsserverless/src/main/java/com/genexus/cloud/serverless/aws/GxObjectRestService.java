package com.genexus.cloud.serverless.aws;

import com.genexus.*;
import com.genexus.util.CacheAPI;

import com.genexus.util.GXServices;
import json.org.json.JSONException;
import json.org.json.JSONObject;

import com.genexus.servlet.IServletContext;
import com.genexus.servlet.ServletContext;
import com.genexus.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.genexus.webpanels.FileItem;

public class GxObjectRestService extends GxRestService {
    @Context
    private javax.servlet.ServletContext myContext;
    @Context
    private javax.servlet.http.HttpServletRequest myServletRequest;
    @Context
    private javax.servlet.http.HttpServletResponse myServletResponse;

    @POST
    @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    public Response execute() {
		IHttpServletRequest myServletRequestWrapper = new HttpServletRequest(myServletRequest);
		IHttpServletResponse myServletResponseWrapper = new HttpServletResponse(myServletResponse);
		IServletContext myContextWrapper = new ServletContext(myContext);
        super.init("POST", myServletRequestWrapper, myServletResponseWrapper, myContextWrapper);
        Response.ResponseBuilder builder = Response.status(201);
        String contentType = myServletRequest.getHeader("Content-Type");
        String ext = getExtension(contentType);
        String tempFileName = com.genexus.PrivateUtilities.getTempFileName(ext);
        String filePath;
        if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null) {
            filePath = Preferences.getDefaultPreferences().getBLOB_PATH() + tempFileName;
        } else {
            filePath = Preferences.getDefaultPreferences().getBLOB_PATH().replace(java.io.File.separator, "/") + tempFileName;
        }

        try (InputStream stream = myServletRequest.getInputStream()) {
            filePath = new FileItem(filePath, false, "", stream).getPath();
            String keyId = UUID.randomUUID().toString().replace("-", "");
            CacheAPI.files().set(keyId, filePath, CommonUtil.UPLOAD_TIMEOUT);

            builder.header("Content-Type", "application/json");
            builder.header("GeneXus-Object-Id", keyId);

            JSONObject jObj = new JSONObject();
            try {
                jObj.put("object_id", CommonUtil.UPLOADPREFIX + keyId);
                builder.entity(jObj.toString());
            } catch (JSONException e) {
                builder = Response.status(500);
                builder.entity(errorJson.toString());
            }
        } catch (IOException e) {
            builder = Response.status(500);
            System.err.println(e);
            builder.entity(e.getMessage());
        }
        return builder.build();
    }

    protected boolean IntegratedSecurityEnabled() {
        return com.genexus.Application.getClientPreferences().getProperty("EnableIntegratedSecurity", "0").equals("1");
    }

    protected int IntegratedSecurityLevel() {
        //return SECURITY_GXOBJECT;
        return 0;
    }

    protected String IntegratedSecurityPermissionPrefix() {
        return "";
    }

    private static String getExtension(String contentType) {
        if (contentType.startsWith("image/jpeg")) {
            return "jpg";
        }
        if (contentType.startsWith("image/tiff")) {
            return "tif";
        }
        if (contentType.startsWith("image/png")) {
            return "png";
        }
        if (contentType.startsWith("audio/x-wav")) {
            return "wav";
        }
        if (contentType.startsWith("video/mpeg")) {
            return "mpg";
        }
        if (contentType.startsWith("video/quicktime")) {
            return "mov";
        }
        if (contentType.startsWith("audio/x-caf")) {
            return "caf";
        }
        if (contentType.startsWith("audio/mpeg")) {
            return "mp3";
        }
        if (contentType.startsWith("audio/x-pn-realaudio")) {
            return "ram";
        }
        if (contentType.startsWith("application/pdf")) {
            return "pdf";
        }
        if (contentType.startsWith("text/rtf")) {
            return "rtf";
        }
        if (contentType.startsWith("text/plain")) {
            return "txt";
        }
        if (contentType.startsWith("image/bmp")) {
            return "bmp";
        }
        if (contentType.startsWith("image/gif")) {
            return "gif";
        }
        if (contentType.startsWith("audio/mp4")) {
            return "m4a";
        }
        if (contentType.startsWith("audio/3gpp")) {
            return "a3gpp";
        }
        if (contentType.startsWith("video/3gpp2")) {
            return "3g2";
        }
        if (contentType.startsWith("video/3gpp")) {
            return "3gp";
        }
        if (contentType.startsWith("video/mp4")) {
            return "mp4";
        }
        if (contentType.startsWith("video/x-msvideo")) {
            return "avi";
        }
        if (contentType.startsWith("video/x-divx")) {
            return "divx";
        }
        if (contentType.startsWith("application/zip")) {
            return "zip";
        }
        if (contentType.startsWith("application/x-gzip")) {
            return "gz";
        }
        if (contentType.startsWith("application/x-tar")) {
            return "tar";
        }
        if (contentType.startsWith("application/x-rar-compressed")) {
            return "rar";
        }
        return "tmp";
    }
}

