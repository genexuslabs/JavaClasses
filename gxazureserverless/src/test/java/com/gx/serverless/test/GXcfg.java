package com.gx.serverless.test;

import com.genexus.GXutil;

public final class GXcfg {
	public static int strcmp(String Left,
							 String Right) {
		return GXutil.rtrim(Left).compareTo(GXutil.rtrim(Right));
	}

}

