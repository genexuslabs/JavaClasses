package com.genexus.JWT.utils;

import java.util.UUID;

import com.genexus.commons.GUIDObject;

/***** DEPRECATED OBJECT SINCE GeneXus 16 upgrade 11 ******/

public final class GUID extends GUIDObject {

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	/**
	 * @deprecated GUID object is deprecated. USe Genexus GUID data type instead
	 *             https://wiki.genexus.com/commwiki/servlet/wiki?31772,GUID+data+type
	 */
	@Deprecated
	public String generate() {
		UUID uuid = UUID.randomUUID();

		return uuid.toString();// .replaceAll("-", "").toUpperCase();
	}
	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
}
