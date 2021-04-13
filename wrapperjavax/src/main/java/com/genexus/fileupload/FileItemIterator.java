package com.genexus.fileupload;

import com.genexus.fileupload.IFileItemIterator;
import com.genexus.fileupload.IFileItemStream;
import java.io.IOException;

public class FileItemIterator implements IFileItemIterator{

	private org.apache.commons.fileupload.FileItemIterator fii;

	public FileItemIterator(org.apache.commons.fileupload.FileItemIterator fii) {
		this.fii = fii;
	}

	public boolean hasNext() throws org.apache.commons.fileupload.FileUploadException, IOException {
		return fii.hasNext();
	}

	public IFileItemStream next() throws org.apache.commons.fileupload.FileUploadException, IOException{
		return new FileItemStream(fii.next());
	}
}
