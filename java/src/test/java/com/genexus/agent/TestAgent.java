package com.genexus.agent;

import com.genexus.Application;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Before;
import org.junit.Test;

public class TestAgent {

	@Before
	public void setUpStreams() {
		Connect.init();
		LogManager.initialize(".");
		Application.init(GXcfg.class);
	}

	@Test
	public void testAPICallAgent() {
		String[] GXv_char4 = new String[1] ;
		new Agent(-1).execute( "Today", "Tomorrow", GXv_char4) ;
		System.out.println(GXv_char4[0]);

		String[] GXv_char5 = new String[1] ;
		new Agent(-1).execute( "chat", "", GXv_char5) ;
		System.out.println(GXv_char5[0]);

		String[] GXv_char9 = new String[1] ;
		new Agent(-1).execute( "eval_image", "", GXv_char9) ;
		System.out.println(GXv_char9[0]);

		String[] GXv_char8 = new String[1] ;
		new Agent(-1).execute( "chat_stream", "", GXv_char8) ;

		String[] GXv_char6 = new String[1] ;
		new Agent(-1).execute( "toolcall", "", GXv_char6) ;
		System.out.println();
		System.out.println(GXv_char6[0]);

		String[] GXv_char7 = new String[1] ;
		new Agent(-1).execute( "toolcall_stream", "", GXv_char7) ;
	}
}
