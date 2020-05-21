package com.genexus.gxoffice;

import java.math.BigDecimal;
import java.util.Date;

import com.genexus.Application;
import com.genexus.ICleanedup;
import com.genexus.LoadLibrary;

public class GxOffice implements ICleanedup {
	private static GxOffice office;
	private GxExcel excel;
	private GxWord word;
	// private GxMail mail;

	static {
		LoadLibrary.load("gxoffice2");
	}

	private GxOffice() {
		Application.addCleanup(this);
		excel = new GxExcel();
		word = new GxWord();
		// mail = new GxMail ();
	}

	public static GxOffice getGXOffice() {
		if (office == null) {
			office = new GxOffice();
		}

		return office;
	}

	public int gxxopen(String xlName) {
		short[] p = new short[] { 0 };

		return excel.Open(xlName, p);
	}

	public void gxxopen(String xlName, int[] handle) {
		handle[0] = gxxopen(xlName);
	}

	public int gxxshow(double handle) {
		return excel.Show((short) handle);
	}

	public void gxxshow(double handle, int[] out) {
		out[0] = gxxshow(handle);
	}

	public int gxxclose(double handle) {
		return excel.Close((short) handle);
	}

	public void gxxclose(double handle, int[] out) {
		out[0] = gxxclose(handle);
	}

	public int gxxsave(double handle) {
		return excel.Save((short) handle);
	}

	public void gxxsave(double handle, int[] out) {
		out[0] = gxxsave(handle);
	}

	public int gxxhide(double handle) {
		return excel.Hide((short) handle);
	}

	public void gxxhide(double handle, int[] out) {
		out[0] = gxxhide(handle);
	}

	public int gxxclear(double handle) {
		return excel.Clear((short) handle);
	}

	public void gxxclear(double handle, int[] out) {
		out[0] = gxxclear(handle);
	}

	public int gxxget(double handle, int row, int col, long[] ret) {
		return excel.GetLong((short) handle, row, col, ret);
	}

	public int gxxget(double handle, int row, int col, int[] ret) {
		long[] l = new long[1];
		int out = gxxget(handle, row, col, l);
		ret[0] = (int) l[0];
		return out;
	}

	public int gxxget(double handle, int row, int col, short[] ret) {
		long[] l = new long[1];
		int out = gxxget(handle, row, col, l);
		ret[0] = (short) l[0];
		return out;
	}

	public int gxxget(double handle, int row, int col, byte[] ret) {
		long[] l = new long[1];
		int out = gxxget(handle, row, col, l);
		ret[0] = (byte) l[0];
		return out;
	}

	public void gxxget(double handle, int row, int col, long[] value, int[] out) {
		out[0] = gxxget(handle, row, col, value);
	}

	public void gxxget(double handle, int row, int col, int[] value, int[] out) {
		long[] l = new long[1];
		out[0] = gxxget(handle, row, col, l);
		value[0] = (int) l[0];
	}

	public void gxxget(double handle, int row, int col, short[] value, int[] out) {
		long[] l = new long[1];
		out[0] = gxxget(handle, row, col, l);
		value[0] = (short) l[0];
	}

	public void gxxget(double handle, int row, int col, byte[] value, int[] out) {
		long[] l = new long[1];
		out[0] = gxxget(handle, row, col, l);
		value[0] = (byte) l[0];
	}

	public int gxxget(double handle, int row, int col, double[] ret) {
		return excel.GetDouble((short) handle, row, col, ret);
	}

	public void gxxget(double handle, int row, int col, double[] ret, int[] out) {
		out[0] = gxxget(handle, row, col, ret);
	}

	public int gxxget(double handle, int row, int col, java.util.Date[] ret) {
		return excel.GetDate((short) handle, row, col, ret);
	}

	public void gxxget(double handle, int row, int col, java.util.Date[] ret, int[] out) {
		out[0] = gxxget(handle, row, col, ret);
	}

	public int gxxget(double handle, int row, int col, BigDecimal[] ret) {
		double[] retd = new double[1];

		int out = excel.GetDouble((short) handle, row, col, retd);
		ret[0] = new BigDecimal(retd[0]);
		return out;
	}

	public void gxxget(double handle, int row, int col, BigDecimal[] ret, int[] out) {
		out[0] = gxxget(handle, row, col, ret);
	}

	public int gxxget(double handle, int row, int col, String[] ret) {
		return excel.GetString((short) handle, row, col, ret);
	}

	// ------------------------------------ xlsGets
	// -----------------------------------------
	public int gxxget(double handle, String[] value, double row, double col, String[] font, double[] size,
			long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	public int gxxget(double handle, byte[] value, double row, double col, String[] font, double[] size, long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	public int gxxget(double handle, short[] value, double row, double col, String[] font, double[] size,
			long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	public int gxxget(double handle, int[] value, double row, double col, String[] font, double[] size, long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	public int gxxget(double handle, double[] value, double row, double col, String[] font, double[] size,
			long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	public int gxxget(double handle, long[] value, double row, double col, String[] font, double[] size, long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	public int gxxget(double handle, java.util.Date[] value, double row, double col, String[] font, double[] size,
			long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	public int gxxget(double handle, BigDecimal[] value, double row, double col, String[] font, double[] size,
			long[] color) {
		gxxgetformat((long) handle, (int) row, (int) col, color, font, size);
		return gxxget((long) handle, (int) row, (int) col, value);
	}

	// -
	// ------------------------------------ xlsGets BigDecimal
	// -----------------------------------------
	public int gxxget(double handle, String[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	public int gxxget(double handle, byte[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	public int gxxget(double handle, short[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	public int gxxget(double handle, int[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	public int gxxget(double handle, double[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	public int gxxget(double handle, long[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	public int gxxget(double handle, java.util.Date[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	public int gxxget(double handle, BigDecimal[] value, double row, double col, String[] font, BigDecimal[] size,
			long[] color) {
		double[] dsize = new double[1];
		int ret = gxxget(handle, value, row, col, font, dsize, color);
		size[0] = new BigDecimal(dsize[0]);
		return ret;
	}

	// -

	public int gxxput(double handle, String value, double row, double col, String font, double size, double color) {
		gxxputformat((int) handle, (int) row, (int) col, 1, 1, (int) color, font, (int) size);
		return gxxput((int) handle, (int) row, (int) col, value);
	}

	public int gxxput(double handle, long value, double row, double col, String font, double size, double color) {
		gxxputformat((int) handle, (int) row, (int) col, 1, 1, (int) color, font, (int) size);
		return gxxput((int) handle, (int) row, (int) col, (int) value);
	}

	public int gxxput(double handle, double value, double row, double col, String font, double size, double color) {
		gxxputformat((int) handle, (int) row, (int) col, 1, 1, (int) color, font, (int) size);
		return gxxput((int) handle, (int) row, (int) col, (int) value);
	}

	public int gxxput(double handle, Date value, double row, double col, String font, double size, double color) {
		gxxputformat((int) handle, (int) row, (int) col, 1, 1, (int) color, font, (int) size);
		return gxxput((int) handle, (int) row, (int) col, value);
	}

	public int gxxput(double handle, BigDecimal value, double row, double col, String font, double size, double color) {
		gxxputformat((int) handle, (int) row, (int) col, 1, 1, (int) color, font, (int) size);
		return gxxput((int) handle, (int) row, (int) col, value.toString());
	}

	public void gxxget(double handle, int row, int col, String[] ret, int[] out) {
		out[0] = gxxget(handle, row, col, ret);
	}

	public int gxxput(double handle, int row, int col, long value) {
		return excel.PutLong((short) handle, row, col, value);
	}

	public void gxxput(double handle, int row, int col, long value, int[] out) {
		out[0] = gxxput(handle, row, col, value);
	}

	public int gxxput(double handle, int row, int col, double value) {
		return excel.PutDouble((short) handle, row, col, value);
	}

	public void gxxput(double handle, int row, int col, double value, int[] out) {
		out[0] = gxxput(handle, row, col, value);
	}

	public int gxxput(double handle, int row, int col, Date value) {
		return excel.PutDate((short) handle, row, col, value);
	}

	public void gxxput(double handle, int row, int col, Date value, int[] out) {
		out[0] = gxxput(handle, row, col, value);
	}

	public void gxxput(double handle, int row, int col, BigDecimal value, int[] out) {
		out[0] = gxxput(handle, row, col, value);
	}

	public int gxxput(double handle, int row, int col, BigDecimal value) {
		return excel.PutDouble((short) handle, row, col, value.doubleValue());
	}

	public int gxxput(double handle, int row, int col, String value) {
		return excel.PutString((short) handle, row, col, value);
	}

	public void gxxput(double handle, int row, int col, String value, int[] out) {
		out[0] = gxxput(handle, row, col, value);
	}

	public int gxxtype(double handle, double row, double col, String[] ret) {
		return excel.Type((short) handle, (int) row, (int) col, ret);
	}

	public void gxxtype(double handle, double row, double col, String[] ret, int[] out) {
		out[0] = gxxtype(handle, row, col, ret);
	}

	public int gxxerror() {
		return excel.getError();
	}

	public void gxxerror(int[] out) {
		out[0] = gxxerror();
	}

	public int gxxdspmsg(int value) {
		excel.setDisplayMessages((short) value);
		return gxxerror();
	}

	public void gxxdspmsg(int value, int[] out) {
		out[0] = gxxdspmsg(value);
	}

	public int gxdefaultpath(String path) {
		excel.setDefaultPath(path);
		return gxxerror();
	}

	public void gxdefaultpath(String path, int[] out) {
		out[0] = gxdefaultpath(path);
	}

	public int gxxgetformat(long handle, int row, int col, long[] color, String[] font, double[] size) {
		int[] _color = new int[] { 0 };
		int[] _size = new int[] { 0 };

		int ret = excel.GetFormat((short) handle, row, col, _color, font, _size);

		color[0] = _color[0];
		size[0] = _size[0];

		return ret;
	}

	public void gxxgetformat(long handle, int row, int col, long[] color, String[] font, double[] size, int[] out) {
		out[0] = gxxgetformat(handle, row, col, color, font, size);
	}

	public int gxxputformat(long handle, long row, long col, long height, long width, long color, String font,
			double size) {
		return excel.PutFormat((short) handle, (int) row, (int) col, (int) height, (int) width, (int) color, font,
				size);
	}

	public void gxxputformat(long handle, long row, long col, long height, long width, long color, String font,
			double size, int[] out) {
		out[0] = gxxputformat(handle, row, col, height, width, color, font, size);
	}

	public int gxxprint(int handle, int preview) {
		return excel.PrintOut((short) handle, (short) preview);
	}

	public void gxxprint(int handle, int preview, int[] out) {
		out[0] = gxxprint(handle, preview);
	}

	public int gxxselectsheet(int handle, String sheet) {
		return excel.SelectSheet((short) handle, sheet);
	}

	public void gxxselectsheet(int handle, String sheet, int[] out) {
		out[0] = gxxselectsheet(handle, sheet);
	}

	public int gxxrenamesheet(int handle, String sheet) {
		return excel.RenameSheet((short) handle, sheet);
	}

	public void gxxrenamesheet(int handle, String sheet, int[] out) {
		out[0] = gxxrenamesheet(handle, sheet);
	}

	public int gxxautofit(int value) {
		excel.setAutoFit((short) value);
		return excel.getError();
	}

	public void gxxautofit(int value, int[] out) {
		out[0] = gxxautofit(value);
	}

	public int gxxdelimiter(String value) {
		excel.setDelimiter(value);
		return excel.getError();
	}

	public void gxxdelimiter(String value, int[] out) {
		out[0] = gxxdelimiter(value);
	}

	public int gxxreadonly(int value) {
		excel.setReadOnly((short) value);
		return excel.getError();
	}

	public void gxxreadonly(int value, int[] out) {
		out[0] = gxxreadonly(value);
	}

	public int gxxtemplate(String template) {
		excel.setTemplate(template);
		return excel.getError();
	}

	public void gxxtemplate(String template, int[] out) {
		out[0] = gxxtemplate(template);
	}

	// GxWord
	public int gxwopen(String fileName, int[] handle) {
		short[] l = new short[] { 0 };

		int ret = word.Open(fileName, l);

		handle[0] = l[0];

		return ret;
	}

	public void gxwopen(String fileName, int[] handle, int[] out) {
		out[0] = gxwopen(fileName, handle);
	}

	public int gxwclose(int handle) {
		word.Close((short) handle);
		return word.getError();
	}

	public void gxwclose(int handle, int[] out) {
		out[0] = gxwclose(handle);
	}

	public int gxwsave(int handle) {
		word.Save((short) handle);
		return word.getError();
	}

	public void gxwsave(int handle, int[] out) {
		out[0] = gxwsave(handle);
	}

	public int gxwsaveas(int handle, String FileName, String FileType, int DosText, int LineBreaks) {
		return word.SaveAs((short) handle, FileName, FileType, (short) DosText, (short) LineBreaks);
	}

	public void gxwsaveas(int handle, String FileName, String FileType, int DosText, int LineBreaks, int[] out) {
		out[0] = gxwsaveas(handle, FileName, FileType, DosText, LineBreaks);
	}

	public int gxwshow(int handle) {
		return word.Show((short) handle);
	}

	public void gxwshow(int handle, int[] out) {
		out[0] = word.Show((short) handle);
	}

	public int gxwhide(int handle) {
		return word.Hide((short) handle);
	}

	public void gxwhide(int handle, int[] out) {
		out[0] = gxwhide(handle);
	}

	public int gxwreadonly(int value) {
		word.setReadOnly((short) value);
		return word.getError();
	}

	public void gxwreadonly(int value, int[] out) {
		out[0] = gxwreadonly(value);
	}

	public int gxwget(int handle, String[] text) {
		return word.Get((short) handle, text);
	}

	public void gxwget(int handle, String[] text, int[] out) {
		out[0] = gxwget(handle, text);
	}

	public int gxwput(int handle, String text) {
		return gxwput(handle, text, 0);
	}

	public int gxwput(int handle, String text, int append) {
		return word.Put((short) handle, text, (short) append);
	}

	public void gxwput(int handle, String text, int append, int[] out) {
		out[0] = gxwput(handle, text, append);
	}

	public int gxwprint(int handle) {
		return word.PrintOut((short) handle, (short) 0, (short) 0);
	}

	public int gxwprint(int handle, int preview) {
		return word.PrintOut((short) handle, (short) preview, (short) 0);
	}

	public int gxwprint(int handle, int preview, int background) {
		return word.PrintOut((short) handle, (short) preview, (short) background);
	}

	public void gxwprint(int handle, int preview, int background, int[] out) {
		out[0] = gxwprint(handle, preview, background);
	}

	public int gxwtemplate(String template) {
		word.setTemplate(template);
		return word.getError();
	}

	public void gxwtemplate(String template, int[] out) {
		out[0] = gxwtemplate(template);
	}

	public int gxwreplace(int handle, String oldValue, String newValue, int MatchCase, int MatchWholeWord) {
		return word.Replace((short) handle, oldValue, newValue, (short) MatchCase, (short) MatchWholeWord);
	}

	public void gxwreplace(int handle, String oldValue, String newValue, int MatchCase, int MatchWholeWord, int[] out) {
		out[0] = gxwreplace(handle, oldValue, newValue, MatchCase, MatchWholeWord);
	}

	public int gxwspellcheck(int handle) {
		return word.SpellCheck((short) handle);
	}

	public void gxwspellcheck(int handle, int[] out) {
		out[0] = gxwspellcheck(handle);
	}

	public int gxwerror() {
		return word.getError();
	}

	public void gxwerror(int[] out) {
		out[0] = gxwerror();
	}

	public int gxwdspmsg(int value) {
		word.setDisplayMessages((short) value);
		return gxwerror();
	}

	public void gxwdspmsg(int value, int[] out) {
		out[0] = gxwdspmsg(value);
	}

	public void cleanup() {
		excel.cleanup();
	}
}
