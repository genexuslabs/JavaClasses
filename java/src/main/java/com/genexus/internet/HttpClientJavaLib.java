package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.apache.http.client.*;
import HTTPClient.*;

public class HttpClientJavaLib extends GXHttpClient {
	private int statusCode;
	private String reasonLine;


	private void resetExecParams() {
		this.statusCode = 0;
		this.reasonLine = "";
		resetErrors();
	}


	public void addAuthentication(int type, String realm, String name, String value) {
	}

	public void addProxyAuthentication(int type, String realm, String name, String value) {
	}

	public void execute(String method, String url) {
		resetExecParams();


		url = getURLValid(url);
		if (getSecure() == 1)
			url = "https://" + getHost();
		else
			url = "http://" + getHost();



		// HACER ACA LA PARTE DEL AGREGADO DE LOS HEADERS DE AUTH


		// Al final de cada ejecucion se setean los atributos

		if (method.equalsIgnoreCase("GET")) {


		} else if (method.equalsIgnoreCase("POST")) {

		} else if (method.equalsIgnoreCase("PUT")) {

		} else if (method.equalsIgnoreCase("DELETE")) {

		} else {
			// VER COMO TRATAR ACA LOS OTROS METODOS QUE PUEDEN VENIR
		}



		resetState();

	}

	public int getStatusCode() {
		return 0;
	}

	public String getReasonLine() {
		return "";
	}

	public void getHeader(String name, long[] value) {
	}

	public String getHeader(String name) {
		return "";
	}

	public void getHeader(String name, String[] value) {
	}

	public void getHeader(String name, java.util.Date[] value) {
	}

	public void getHeader(String name, double[] value) {
	}

	public InputStream getInputStream() throws IOException {
		return new InputStream() {
			@Override
			public int read() throws IOException {

				return 0;
			}
		};
	}

	public String getString() {
		return "";
	}

	public void toFile(String fileName) {
	}


	public void cleanup() {
	}
}