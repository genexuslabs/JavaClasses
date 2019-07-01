package com.genexus.common.classes;

import java.util.Vector;

public abstract class AbstractGXFile {

	public abstract String readAllText(String string);

	public abstract String getAbsoluteName();

	public abstract String getNameNoExt() ;

	public abstract long getLength();

	public abstract String getName();

	public abstract java.util.Date getLastModified();

	public abstract String getPath();

	public abstract String getURI();

	public abstract String htmlClean();

	public abstract void open(String string);

	public abstract void openRead(String string);

	public abstract Vector readAllLines();

	public abstract String readLine();

	public abstract void rename(String string);

	public abstract String getSeparator();

	public abstract void setSource(String string);

	public abstract void writeAllLines(Vector vector, String string);

	public abstract void writeAllText(String string, String string1);

	public abstract void writeLine(String string);

	public abstract void appendAllLines(Vector vector, String string);

	public abstract void appendAllText(String string, String string1);
}
