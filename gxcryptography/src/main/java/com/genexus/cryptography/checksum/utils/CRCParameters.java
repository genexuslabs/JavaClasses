package com.genexus.cryptography.checksum.utils;

public class CRCParameters {

	private int _width;
	private long _polynomial;
	private boolean _reflectIn;
	private boolean _reflectOut;
	private long _init;
	private long _finalXor;

	public CRCParameters(int width, long polynomial, long init, boolean reflectIn, boolean reflectOut, long finalXor) {
		this._width = width;
		this._polynomial = polynomial;
		this._reflectIn = reflectIn;
		this._reflectOut = reflectOut;
		this._init = init;
		this._finalXor = finalXor;
	}

	public int getWidth() {
		return this._width;
	}

	public long getPolynomial() {
		return this._polynomial;
	}

	public boolean getReflectIn() {
		return this._reflectIn;
	}

	public boolean getReflectOut() {
		return this._reflectOut;
	}

	public long getInit() {
		return this._init;
	}

	public long getFinalXor() {
		return this._finalXor;
	}
}
