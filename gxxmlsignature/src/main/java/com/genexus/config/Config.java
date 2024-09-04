package com.genexus.config;

public final class Config {

	private static boolean GLOBAL_USELINEBREAKS;

	public static void setUseLineBreaks(boolean value) {
		GLOBAL_USELINEBREAKS = value;
	}

	public static boolean getUseLineBreaks() {
		return GLOBAL_USELINEBREAKS;
	}
}
