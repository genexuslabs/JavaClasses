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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<Object> handleException(HttpMessageNotReadableException ex) {
		log.error("Error executing REST service", ex);
		JSONObject errorJson = new JSONObject();
		try
		{
			JSONObject obj = new JSONObject();
			obj.put("code", 400);
			obj.put("message", "Bad Request");
			errorJson.put("error", obj);
		}
		catch(JSONException e)
		{
			log.error("Invalid JSON", e);
		}

		ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.BAD_REQUEST);
		builder.contentType(MediaType.APPLICATION_JSON);
		return builder.body(errorJson.toString()) ;
	}
}
