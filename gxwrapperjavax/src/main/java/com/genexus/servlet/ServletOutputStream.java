package com.genexus.servlet;

import java.io.IOException;
import java.io.OutputStream;

public class ServletOutputStream implements IServletOutputStream{
	private javax.servlet.ServletOutputStream sos;

	public ServletOutputStream(javax.servlet.ServletOutputStream sos) {
		this.sos = sos;
	}

	public ServletOutputStream(ServletOutputStream servletOutputStream) {
		this.sos = servletOutputStream.getWrappedClass();
	}

	public javax.servlet.ServletOutputStream getWrappedClass() {
		return sos;
	}

	public OutputStream getOutputStream() {
		return sos;
	}

	public void println() throws IOException {
		sos.println();
	}

	public void print(boolean b) throws IOException {
		sos.print(b);
	}

	public void print(char c) throws IOException {
		sos.print(c);
	}

	public void print(double d) throws IOException {
		sos.print(d);
	}

	public void print(float f) throws IOException {
		sos.print(f);
	}

	public void print(int i) throws IOException {
		sos.print(i);
	}

	public void print(long l) throws IOException {
		sos.print(l);
	}

	public void print(String s) throws IOException {
		sos.print(s);
	}

	public void println(boolean b) throws IOException {
		sos.println(b);
	}

	public void println(int i) throws IOException {
		sos.println(i);
	}

	public void println(char c) throws IOException {
		sos.println(c);
	}

	public void println(double d) throws IOException {
		sos.println(d);
	}

	public void println(float f) throws IOException {
		sos.println(f);
	}

	public void println(long l) throws IOException {
		sos.println(l);
	}

	public void println(String s) throws IOException {
		sos.println(s);
	}

	public void write(byte[] b) throws IOException{
		sos.write(b);
	}

	public void write(int i) throws IOException{
		sos.write(i);
	}

	public void write(byte[] b, int i, int j) throws IOException{
		sos.write(b, i, j);
	}

	public void close() throws IOException {
		sos.close();
	}

	public void flush() throws IOException {
		sos.flush();
	}
}
