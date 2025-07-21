package com.genexus.gam.utils.json;

import java.util.Date;

public class UnixTimestamp {

	public static long create(Date gxdate) {
		return gxdate.toInstant().getEpochSecond();
	}

}
