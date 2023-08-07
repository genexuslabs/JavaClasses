package com.genexus.springboot;

import com.genexus.GxRestService;
import com.genexus.WrapperUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

abstract public class GxSpringBootRestService extends GxRestService {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GxSpringBootRestService.class);

	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<Object> handleException(HttpMessageNotReadableException ex) {
		log.error("Error executing REST service", ex);

		SetError("400", "Bad Request");
		ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.BAD_REQUEST);
		builder.contentType(MediaType.APPLICATION_JSON);
		return builder.body(errorJson.toString()) ;
	}
}
