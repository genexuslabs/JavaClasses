package com.genexus.db.driver;

import com.mockdb.*;
import com.genexus.Application;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class TestMockDataAccess {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
	}

	@Test
	public void testMockDataAccess(){
		Application.init(GXcfg.class);
		ausemockdataaccess pgm = new ausemockdataaccess (-1);
		Application.realMainProgram = pgm;
		pgm.execute();

		assertEquals("Executing Update sentence\r\n" +
			"Executing Update sentence\r\n" +
			"Executing SQL sentence\r\n" +
			"Executing Update sentence\r\n", outContent.toString());
	}
}
