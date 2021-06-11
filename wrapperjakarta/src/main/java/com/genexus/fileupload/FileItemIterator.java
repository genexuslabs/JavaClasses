package com.genexus.fileupload;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.util.Iterator;

public class FileItemIterator implements IFileItemIterator{

	Iterator<Part> parts = null;

	public FileItemIterator(HttpServletRequest request) throws Exception {
		parts = request.getParts().iterator();
	}

	public boolean hasNext() throws Exception {
		return parts.hasNext();
	}

	public FileItemStream next() throws Exception{
		return new FileItemStream(parts.next());
	}
}
