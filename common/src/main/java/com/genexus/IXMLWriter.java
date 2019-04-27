package com.genexus;

import java.io.IOException;
import java.math.BigDecimal;

public interface IXMLWriter {
    byte xmlEnd();
    byte xmlStartWeb(String sFileName);
    byte xmlStart(String sFileName);
    byte xmlBeginElement(String sLevelName);
    byte xmlEndElement();
    byte xmlText(String sTag);
    byte xmlRaw(String sTag);
    byte xmlValue(String sTag, java.util.Date sValue);
    byte xmlValue(String sTag, String sValue);
    byte xmlValue(String sTag, long nValue);
    byte xmlValue(String sTag, double dValue);
    byte xmlValue(String sTag, BigDecimal dValue);
    byte xmlAtt(String sName, String sValue);
    byte xmlAtt(String sName, long nValue);
    byte xmlAtt(String sName, double nValue);
    byte xmlAtt(String sName, BigDecimal dValue);

}
