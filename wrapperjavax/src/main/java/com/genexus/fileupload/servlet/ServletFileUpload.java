package com.genexus.fileupload.servlet;

import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.fileupload.FileItemIterator;
import com.genexus.fileupload.IFileItemIterator;

import java.io.IOException;

public class ServletFileUpload extends org.apache.commons.fileupload.servlet.ServletFileUpload implements IServletFileUpload{

	public ServletFileUpload() {
		super();
	}

	public void setUploadHeaderEncoding(String encoding) {
		setHeaderEncoding(encoding);
	}

	public IFileItemIterator getItemIterator(IHttpServletRequest request) throws org.apache.commons.fileupload.FileUploadException, IOException {
		return new FileItemIterator(getItemIterator(((HttpServletRequest)request).getWrappedClass()));
	}

	public static boolean isMultipartContent(IHttpServletRequest request) {
		return org.apache.commons.fileupload.servlet.ServletFileUpload.isMultipartContent(((HttpServletRequest)request).getWrappedClass());
	}
}
