package com.genexus.cloud.serverless.aws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.genexus.Application;
import com.genexus.GXutil;
import com.genexus.Preferences;
import com.genexus.util.GXFile;
import com.genexus.util.GXServices;

import json.org.json.JSONException;
import json.org.json.JSONObject;

public class StreamLambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest arg0, Context arg1) {
        try {
            LambdaHandler.initialize();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return processFile(arg0);
    }


    protected AwsProxyResponse processFile(AwsProxyRequest arg0) {
        AwsProxyResponse response = new AwsProxyResponse();
        String contentType = arg0.getHeaders().get("Content-Type");
        String ext = getExtension(contentType);
        String tempFileName = com.genexus.PrivateUtilities.getTempFileName(ext);
        String filePath;
        if (Application.getGXServices().get(GXServices.STORAGE_SERVICE) == null) {
            filePath = Preferences.getDefaultPreferences().getBLOB_PATH() + tempFileName;
        } else {
            filePath = Preferences.getDefaultPreferences().getBLOB_PATH().replace(java.io.File.separator, "/") + tempFileName;
        }
        byte[] data = arg0.getBody().getBytes();
        InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(data));

        GXFile gxFile = new GXFile(filePath, true);
        gxFile.create(stream);

        filePath = gxFile.getAbsolutePath();
        String keyId = UUID.randomUUID().toString().replace("-", "");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("GeneXus-Object-Id", keyId);
        response.setHeaders(headers);
        response.setStatusCode(201);


        JSONObject jObj = new JSONObject();
        try {
            jObj.put("object_id", GXutil.UPLOADPREFIX + keyId);
            jObj.put("object_path", filePath);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        response.setBody(jObj.toString());
        return response;

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


    public static String readFullyAsString(InputStream inputStream, String encoding)
            throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    public static byte[] readFullyAsBytes(InputStream inputStream)
            throws IOException {
        return readFully(inputStream).toByteArray();
    }

    private static ByteArrayOutputStream readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }


}