package com.genexus.fileupload.servlet;

import com.genexus.fileupload.IFileItemIterator;
import com.genexus.servlet.http.IHttpServletRequest;

public interface IServletFileUpload {

	void setUploadHeaderEncoding(String encoding);

	IFileItemIterator getItemIterator(IHttpServletRequest request) throws Exception;
}
