package com.genexus.fileupload;

import com.genexus.fileupload.IFileItemStream;
import java.io.IOException;
import java.io.InputStream;

public class FileItemStream implements IFileItemStream{
	private org.apache.commons.fileupload.FileItemStream fis;

	public FileItemStream(org.apache.commons.fileupload.FileItemStream fis) {
		this.fis = fis;
	}

	public String getName() {
		return fis.getName();
	}

	public boolean isFormField() {
		return fis.isFormField();
	}

	public InputStream openStream() throws IOException {
		return fis.openStream();
	}

	public String getFieldName() {
		return fis.getFieldName();
	}
}
