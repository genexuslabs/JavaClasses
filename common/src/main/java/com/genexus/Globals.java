
package com.genexus;

public class Globals
{
	public String 	Gx_ope = "";
	public int 		Gx_dbe =  0;
	public String 	Gx_dbt = "";
	public String 	Gx_etb = "";
	public byte 	Gx_eop =  0;
	public String 	Gx_usr = "";
	public String 	Gx_pwd = "";
	public String 	Gx_dbn = "";
	public String 	Gx_srv = "";
	public short	Gx_err = 0;
	public String 	Gx_emsg = "";
	public String   Gx_dbsqlstate = "";

	public byte nLocRead = 0;
	public com.genexus.internet.LocationCollection colLocations;
	public String sSOAPErrMsg = "";
	public int nSOAPErr = 0;

	public short getErr() { return Gx_err; }
	public String getErrMsg() { return Gx_emsg; }
	public int getSoapErr() {return nSOAPErr; }
	public String getSoapErrMsg() {return sSOAPErrMsg; }
}