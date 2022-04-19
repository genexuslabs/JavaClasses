package com.genexus.util;

import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Test;

public class TestFileMethods {
	private static String FILE_NAME = "testFile.txt";
	private static String LINE1 = "Line1";
	private static String LINE2 = "Line2";

	@Test
	public void testOpenWrite(){
		Connect.init();

		GXFile testWriteFile = new GXFile();
		testWriteFile.setSource(FILE_NAME);
		testWriteFile.openWrite("");
		testWriteFile.writeLine(LINE1);
		testWriteFile.close();

		GXFile testReadFile = new GXFile();
		testReadFile.setSource(FILE_NAME);
		testReadFile.openRead("");
		Assert.assertTrue(testReadFile.readLine().equals(LINE1));
		testReadFile.close();

		testWriteFile.openWrite("");
		testWriteFile.writeLine(LINE2);
		testWriteFile.close();

		testReadFile.openRead("");
		Assert.assertTrue(testReadFile.readLine().equals(LINE1));
		Assert.assertTrue(testReadFile.readLine().equals(LINE2));
		testReadFile.close();

		testReadFile.delete();
	}
}
