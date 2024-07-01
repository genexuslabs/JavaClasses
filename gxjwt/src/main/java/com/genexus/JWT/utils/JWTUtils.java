package com.genexus.JWT.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JWTUtils {


	public static final InputStream inputFileToStream(String path) throws IOException {
		final File initialFile = new File(path);
		final InputStream targetStream = new DataInputStream(new FileInputStream(initialFile));
		return targetStream;
	}

}
