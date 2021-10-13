package com.genexus.servlet;

import java.io.IOException;
import java.io.OutputStream;

public interface IServletOutputStream {
	OutputStream getOutputStream();
	void println() throws IOException;
	void print(boolean b) throws IOException;
	void print(char c) throws IOException;
	void print(double d) throws IOException ;
	void print(float f) throws IOException;
	void print(int i) throws IOException;
	void print(long l) throws IOException ;
	void print(String s) throws IOException;
	void println(boolean b) throws IOException;
	void println(int i) throws IOException;
	void println(char c) throws IOException;
	void println(double d) throws IOException;
	void println(float f) throws IOException;
	void println(long l) throws IOException;
	void println(String s) throws IOException;
	void write(byte[] b) throws IOException;
	void write(int i) throws IOException;
	void write(byte[] b, int i, int j) throws IOException;
	void close() throws IOException;
	void flush() throws IOException;
}
