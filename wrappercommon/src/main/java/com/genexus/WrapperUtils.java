package com.genexus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.genexus.opentelemetry.OpenTelemetryHelper;
import org.json.JSONException;
import com.genexus.json.JSONObjectWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

public class WrapperUtils {
    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(WrapperUtils.class);

    public static String getJsonFromRestException(int[] statusCode, String reasonPhrase, boolean applicationException, Throwable ex) {
		OpenTelemetryHelper.recordException(ex);

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
		JSONObjectWrapper errorJson = new JSONObjectWrapper();
        try
        {
			JSONObjectWrapper obj = new JSONObjectWrapper();
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

	public static ThreadLocal<String> requestBodyThreadLocal = new ThreadLocal<String>();

    public static InputStream storeRestRequestBody(InputStream is) throws IOException {
		String body = new BufferedReader(new InputStreamReader(is, "UTF-8")).lines().collect(Collectors.joining("\n"));
		requestBodyThreadLocal.set(body);

		return IOUtils.toInputStream(body, "UTF-8");
	}
}