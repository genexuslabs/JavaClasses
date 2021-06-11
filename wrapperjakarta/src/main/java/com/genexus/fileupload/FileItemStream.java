package com.genexus.fileupload;

import java.io.IOException;
import java.io.InputStream;
import jakarta.servlet.http.Part;

public class FileItemStream implements IFileItemStream{

	Part part;

	public FileItemStream(Part part) {
		this.part = part;
	}

	public String getName() {
		return part.getSubmittedFileName();
	}

	public boolean isFormField() {
		return part.getSubmittedFileName() == null;
	}

	public InputStream openStream() throws IOException {
		return part.getInputStream();
	}

	public String getFieldName() {
		return part.getName();
	}
}
