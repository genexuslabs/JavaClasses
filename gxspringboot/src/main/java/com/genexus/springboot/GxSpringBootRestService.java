package com.genexus.springboot;

import com.genexus.GxRestService;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

abstract public class GxSpringBootRestService extends GxRestService {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GxSpringBootRestService.class);

	@ExceptionHandler(Exception.class)
	protected ResponseEntity<Object> handleException(Exception ex) {
		log.error("Error executing REST service", ex);
		JSONObject errorJson = new JSONObject();
		int errCode = 500;
		String errMessage = "Internal Server Error";
		HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;

		if (ex instanceof HttpMessageNotReadableException || ex instanceof NullPointerException) {
			errCode = 400;
			errMessage = "Bad Request";
			statusCode = HttpStatus.BAD_REQUEST;
		}

		try
		{
			JSONObject obj = new JSONObject();
			obj.put("code", errCode);
			obj.put("message", errMessage);
			errorJson.put("error", obj);
		}
		catch(JSONException e)
		{
			log.error("Invalid JSON", e);
		}

		ResponseEntity.BodyBuilder builder = ResponseEntity.status(statusCode);
		builder.contentType(MediaType.APPLICATION_JSON);
		return builder.body(errorJson.toString()) ;
	}
}
