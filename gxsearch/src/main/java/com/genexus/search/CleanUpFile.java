package com.genexus.search;

import com.genexus.util.ICleanupFile;

public class CleanUpFile implements ICleanupFile {

	public String htmlCleanFile(String absoluteName) {
		return DocumentHandler.htmlCleanFile(absoluteName);
	}

}
