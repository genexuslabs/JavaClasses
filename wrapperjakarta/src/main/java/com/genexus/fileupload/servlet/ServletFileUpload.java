package com.genexus.fileupload.servlet;

import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.fileupload.FileItemIterator;
import com.genexus.fileupload.IFileItemIterator;
import java.util.Collection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;

import java.io.IOException;

public class ServletFileUpload implements IServletFileUpload{

	public ServletFileUpload() {
	}

	public void setUploadHeaderEncoding(String encoding) {
	}

	public IFileItemIterator getItemIterator(IHttpServletRequest request) throws Exception {
		return new FileItemIterator(((HttpServletRequest)request).getWrappedClass());
	}

	public static boolean isMultipartContent(IHttpServletRequest request) {
		try {
			Collection<Part> parts = ((HttpServletRequest) request).getWrappedClass().getParts();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
