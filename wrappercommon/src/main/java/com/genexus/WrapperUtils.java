package com.genexus;

import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.logging.log4j.Logger;

public class WrapperUtils {
    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(WrapperUtils.class);

    public static String getJsonFromRestExcpetion(int[] statusCode, String reasonPhrase, boolean applicationException, Throwable ex) {
        int localStatusCode = statusCode[0];
        if (!applicationException) {
            localStatusCode = 500;
            reasonPhrase = "Internal Server Error";

            if (ex instanceof com.fasterxml.jackson.core.JsonProcessingException)
            {
                localStatusCode = 400;
                reasonPhrase = "Bad Request";
            }
        }

        log.error("Error executing REST service", ex);
        JSONObject errorJson = new JSONObject();
        try
        {
            JSONObject obj = new JSONObject();
            obj.put("code", localStatusCode);
            obj.put("message", reasonPhrase);
            errorJson.put("error", obj);
        }
        catch(JSONException e)
        {
            log.error("Invalid JSON", e);
        }

        statusCode[0] = localStatusCode;
        return errorJson.toString();
    }
}