package com.genexus.fileupload;

import java.io.IOException;
import java.io.InputStream;

public interface IFileItemStream {

	String getName();

	boolean isFormField();

	InputStream openStream() throws IOException;

	String getFieldName();
}
