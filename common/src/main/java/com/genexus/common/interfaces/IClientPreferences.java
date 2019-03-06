package com.genexus.common.interfaces;

import com.genexus.HTMLDocType;

public interface IClientPreferences {

	public byte getYEAR_LIMIT();

	public char getDECIMAL_POINT() ;

	public String getDATE_FMT() ;

	public String getTIME_FMT() ;

	public String getLANGUAGE() ;

	public String getWEB_IMAGE_DIR();

	public boolean getMDI_FORMS();

	public boolean getSDI_CLOSING_FIX();

	public boolean getCS_REORGJAVA();

	public String getREORG_TIME_STAMP();

	public String getPACKAGE();

	public String getBUILD_NUMBER(int buildN);

	public HTMLDocType getDOCTYPE();

	public boolean getDOCTYPE_DTD();

	public String getSMTP_HOST();

}
