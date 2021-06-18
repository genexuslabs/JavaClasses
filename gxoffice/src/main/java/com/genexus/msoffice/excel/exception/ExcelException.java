package com.genexus.msoffice.excel.exception;

public class ExcelException extends Exception
{

	private int _errorCode;
	private String _errDsc;

	public ExcelException(int errCode, String errDsc, Exception e)
	{
		super(e);
		_errorCode = errCode;
		_errDsc = errDsc;
		
	}
	
	public ExcelException(int errCode, String errDsc)
	{
		super();
		_errorCode = errCode;
		_errDsc = errDsc;
	}

	public int get_errorCode()
	{
		return _errorCode;
	}

	public String get_errDsc()
	{
		return _errDsc;
	}

}
