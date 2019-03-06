package com.genexus.specific.java;

import com.genexus.CommonUtil;
import com.genexus.GXPictureFix;
import com.genexus.common.interfaces.IExtensionPictureFormatter;

public class PictureFormatter implements IExtensionPictureFormatter {

	@Override
	public String format(String value, String picture) {
		if	(picture.startsWith("@") && picture.indexOf('!') > 0)
			picture = CommonUtil.replicate("!", value.length());

		GXPictureFix gx = new GXPictureFix(picture, picture.length());
		  
		return gx.formatValid(value);
	}

}
