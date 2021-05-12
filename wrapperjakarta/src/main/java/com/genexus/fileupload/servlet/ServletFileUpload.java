package com.genexus.fileupload.servlet;

import com.genexus.fileupload.servlet.IServletFileUpload;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.fileupload.FileItemIterator;

import java.io.IOException;

public class ServletFileUpload implements IServletFileUpload{

	public ServletFileUpload()
	{
		//TODO
	}

	public void setUploadHeaderEncoding(String encoding) {
		//TODO
	}

	public FileItemIterator getItemIterator(IHttpServletRequest request) throws Exception {
		//TODO
		return null;
	}

	public static boolean isMultipartContent(IHttpServletRequest request) {
		//TODO
		return false;
	}
}
