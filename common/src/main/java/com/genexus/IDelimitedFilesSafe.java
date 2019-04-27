package com.genexus;

import java.util.Date;

public interface IDelimitedFilesSafe {
    byte dfrgtxt(String[] str);
	byte dfrgtxt(String[] str, int len);
    byte dfrgnum(double[] num);
    byte dfrgdate(java.util.Date[] date, String fmt, String sep);
    byte dfropen(String filename);

    byte dfropen(String filename, int len);

    byte dfropen(String filename, int len, String fdel);

    byte dfropen(String filename, int len, String fdel, String psdel);

    byte dfropen(String pfilename, int plen, String pfdel, String psdel, String enc);
    byte dfrnext();
    byte dfrclose();
    byte dfwopen(String filename);
    byte dfwopen(String filename, String fdel);
    byte dfwopen(String pfilename, String pfdel, String psdel);
    byte dfwopen(String pfilename, String pfdel, String psdel, int append);
    byte dfwopen(final String filename, String fdel, final String sdel, final int append, final String enc);

    byte dfwnext();
    byte dfwpnum(double num, int dec);
    byte dfwptxt(String txt, int len);
    byte dfwpdate(Date date, String fmt, String sep);
    byte dfwclose();
    byte dftrace(int trace);

}
