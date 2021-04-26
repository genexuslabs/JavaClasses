package com.genexus.fileupload;

import com.genexus.fileupload.IFileItemStream;
import java.io.IOException;
import java.io.InputStream;

public class FileItemStream implements IFileItemStream{

	public String getName() {
		//TODO
		return null;
	}

	public boolean isFormField() {
		//TODO
		return false;
	}

	public InputStream openStream() throws IOException {
		//TODO
		return null;
	}

	public String getFieldName() {
		//TODO
		return null;
	}
}
