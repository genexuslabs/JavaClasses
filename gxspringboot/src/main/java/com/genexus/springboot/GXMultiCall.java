package com.genexus.springboot;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController("GXMultiCall")
@RequestMapping(value = {"/gxmulticall", "/rest/gxmulticall"})
public class GXMultiCall extends GxSpringBootRestService {
	private final static String METHOD_EXECUTE = "execute";

	@PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Object gxMultiCall(@RequestBody String jsonStr) throws Exception {
		super.init( "POST" );
		ResponseEntity.BodyBuilder builder = null;

		try {
			com.genexus.webpanels.GXMultiCall.callProcRest(context, jsonStr);
		}
		catch(ClassNotFoundException cnf) {
			builder = ResponseEntity.status(HttpStatus.NOT_FOUND);
			cleanup();
			return builder.body(null) ;
		}

		builder = ResponseEntity.ok();
		builder.contentType(MediaType.APPLICATION_JSON);
		cleanup();
		return builder.body("") ;
	}

	protected boolean IntegratedSecurityEnabled( ) {
		return false;
	}

	protected int IntegratedSecurityLevel( ) {
		return 0;
	}

	protected String EncryptURLParameters() {
		return "NO";
	}
}
