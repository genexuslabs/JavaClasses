package com.genexus.fileupload;


public interface IFileItemIterator {

	boolean hasNext() throws Exception;

	IFileItemStream next() throws Exception;
}
