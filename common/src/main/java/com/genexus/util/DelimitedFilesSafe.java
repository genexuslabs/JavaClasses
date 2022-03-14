package com.genexus.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.Date;
import java.io.*;

import com.genexus.*;
import com.genexus.platform.INativeFunctions;
import com.genexus.common.interfaces.SpecificImplementation;

import java.math.BigDecimal;

public class DelimitedFilesSafe implements IDelimitedFilesSafe
{
	// variables

	private static final byte GX_ASCDEL_SUCCESS			=  0;
	private static final byte GX_ASCDEL_INVALIDSEQUENCE	= -1;
	private static final byte GX_ASCDEL_OPENERROR		= -2;
	private static final byte GX_ASCDEL_READERROR		= -3;
	private static final byte GX_ASCDEL_ENDOFDATA		= -4;
	private static final byte GX_ASCDEL_INVALIDFORMAT	= -5;
	private static final byte GX_ASCDEL_OVERFLOW			= -6;
	private static final byte GX_ASCDEL_INVALIDDATE		= -7;
	private static final byte GX_ASCDEL_NOTENOUGHMEMORY	= -8;
	private static final byte GX_ASCDEL_WRITEERROR		= -9;
	private static final byte GX_ASCDEL_BADFMTSTR		= -10;
	
	public static final String CRLF = "\r\n";

	protected String filename;
	private int len;
	private String fdel;
	private String sdel;
	protected boolean dfropen_in_use = false;
	protected boolean dfwopen_in_use = false;
	protected boolean trace_on = DebugFlag.DEBUG;
	protected BufferedReader bufread;
	protected BufferedWriter bufwrite;
	private StringTokenizer actline;
	private String toWrite;
	private String encoding = "";
	private String newLineBehavior = null;
	
	private String currString;
	private boolean readingString = false;
	protected boolean lengthInBytes = false;
	
	private String lastLineRead;
	private boolean lastFieldRead;
	private boolean isCsv;

	public String getEncoding()
	{
		return encoding;
	}

	public byte dftrace(int trace)
	{
		byte lastTrace = (byte)(trace_on ? 1 : 0);
		this.trace_on = (trace != 0);
		return lastTrace;
	}	
	
// dfropen

	public byte dfropen(String filename)
	{
		return dfropen(filename, 1024);
	}

	public byte dfropen(String filename, int len)
	{
		return dfropen(filename, len, ",");
	}

	public byte dfropen(String filename, int len, String fdel)
	{
		return dfropen(filename, len, fdel, "\"");
	}

	public byte dfropen(String filename, int len, String fdel, String psdel)
	{
		return dfropen(filename, len, fdel, psdel, "");
	}
	
	public byte dfropen(String pfilename, int plen, String pfdel, String psdel, String enc)
	{
		byte retval = GX_ASCDEL_SUCCESS;
		
		if	(pfdel.equals("\\t"))
		{
			pfdel = "\t";
		}

		if (!dfropen_in_use)
		{
			this.encoding = enc;
			dfropen_in_use = true;
			filename=pfilename;
			len=plen;
			fdel=pfdel;

			sdel = psdel;
			this.isCsv = pfilename.toUpperCase().endsWith(".CSV");

			try
			{
				SpecificImplementation.NativeFunctions.getInstance().executeWithPermissions(
					new RunnableThrows() {
						public Object run() throws Exception
						{
							if(encoding != null)
							{
								encoding = encoding.trim();
								if(encoding.equals(""))
								{
									encoding = "UTF8";
								}
								
								try
								{
									lengthInBytes = true;
									String bom = getByteOrderMark(encoding);
									encoding = CommonUtil.normalizeEncodingName(encoding);
									bufread = new BufferedReader( new InputStreamReader( new FileInputStream( new File(filename)), encoding ));
									if (bom!=null)
									{
										char bombuffer[] = new char[bom.length()];
										bufread.read(bombuffer);
									}
									return null;
								}catch(UnsupportedEncodingException e){ System.err.println(e.toString()); }
							}
							bufread = new BufferedReader( (new FileReader( (new File(filename)) )) );
							return null;
						}
					}, INativeFunctions.FILE_ALL);
			}
			catch (Exception e)
			{
				retval = GX_ASCDEL_OPENERROR;
				dfropen_in_use = false;
				if (trace_on)
					System.err.println("Error ADF0001: " + e);
			}

		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if (trace_on)
				System.err.println("Error ADF0005: open function in use");
		}

		return retval;
	}

// dfrnext

	public byte dfrnext()
	{
		byte retval = GX_ASCDEL_SUCCESS;
		readingString = false;
		currString = "";

		if (dfropen_in_use)
		{
			try
			{
				String line = bufread.readLine();
				lastLineRead = line;
				lastFieldRead = false;
				if (line == null)
					retval = GX_ASCDEL_ENDOFDATA;
				else
					actline = new StringTokenizer(line, fdel, true);
			}
			catch (IOException e)
			{
				retval = GX_ASCDEL_READERROR;
				if (trace_on)
					System.err.println("Error ADF0002: " + e);
			}

		}else{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
		}
		
		return retval;
	}

	// dfrgnum

	public byte dfrgnum(double[] num)
	{
		byte retval = GX_ASCDEL_SUCCESS;

		Double retnum = new Double(0);

		if (dfropen_in_use)
		{
			try
            {
				String stringDelimitedField = actline.nextToken(fdel);
				if(fdel.equals(stringDelimitedField) || stringDelimitedField.equals(""))
				{ // Si el token debe estar vac�o...
				    stringDelimitedField = "";
				}
				retnum = new Double(stringDelimitedField);
			}
			catch (Exception e)
			{
				retval = GX_ASCDEL_INVALIDFORMAT;
				if 	(trace_on)
					System.err.println("Error ADF0008: " + e);
			}
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if 	(trace_on)
				System.err.println("Error ADF0004 o ADF0006");
		}

		num[0] = retnum.doubleValue();
		try 
		{
			String stringDelimitedField = actline.nextToken(fdel);
		}
		catch(Exception e)
		{//Se sabe que se puede leer un token que no existe al final de la linea
		}		
		return retval;
	}

	public byte dfrgnum(BigDecimal[] num)
	{
		byte retval = GX_ASCDEL_SUCCESS;

		BigDecimal retnum = new BigDecimal(0);

		if (dfropen_in_use)
		{
			try
			{
                String stringDelimitedField = actline.nextToken(fdel);
                if(fdel.equals(stringDelimitedField) || stringDelimitedField.equals(""))
                { // Si el token debe estar vac�o...
                    stringDelimitedField = "";
                }
                if (!stringDelimitedField.equals(""))
                	new BigDecimal(stringDelimitedField);                
                retnum = DecimalUtil.stringToDec(stringDelimitedField);
			}
			catch (Exception e)
			{
				retval = GX_ASCDEL_INVALIDFORMAT;
				if 	(trace_on)
					System.err.println("Error ADF0008: " + e);
			}
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if 	(trace_on)
				System.err.println("Error ADF0004 o ADF0006");
		}

		num[0] = retnum;
		try 
		{
			String stringDelimitedField = actline.nextToken(fdel);
		}
		catch(Exception e)
		{//Se sabe que se puede leer un token que no existe al final de la linea
		}
		return retval;
	}


// dfrgtxt

	public byte dfrgtxt(String[] str)
	{
		return dfrgtxt(str, 20000);
	}

        public byte dfrgtxt(String[] str, int len)
        {
                byte retval = GX_ASCDEL_SUCCESS;
                
            	if (!fdel.equals("") && !actline.hasMoreTokens() && lastLineRead.endsWith(fdel) && !lastFieldRead)
            	{
            		lastFieldRead = true;
            		str[0] = "";
            		return retval;
            	}                

                String retstr = "";

                if (dfropen_in_use)
                {
                        try
                        {
							if(!readingString)
							{//Si no estoy leyendo una linea sin delimitador de campo
							    String stringDelimitedField = actline.nextToken(fdel);
                                if (fdel.equals(sdel))
                                {
                                        if(fdel.equals(stringDelimitedField))
                                          retstr = new String(actline.nextToken(fdel));
                                        else
                                          retstr = stringDelimitedField;
                                }
                                else
                                {
                                        if(fdel.equals(stringDelimitedField) || stringDelimitedField.equals(""))
                                        { // Si es un field vacio.
                                            stringDelimitedField = "";
                                            str[0] = retstr;
                                            return retval;
                                        }
										
                                        int lIndex = 0;
                                        int rIndex = stringDelimitedField.length();
										
                                        if(sdel.length() != 0 )
                                        {
                                                if(stringDelimitedField.trim().startsWith(sdel))
                                                {
                                                        lIndex = stringDelimitedField.indexOf(sdel) + 1;
														
														//pongo eso para considerar el caso ",texto"
														if (stringDelimitedField.trim().endsWith(sdel) && stringDelimitedField.length()==1)
															stringDelimitedField = stringDelimitedField + actline.nextToken(fdel);
														
                                                        while (!stringDelimitedField.trim().endsWith(sdel))
                                                        {
                                                           stringDelimitedField = stringDelimitedField + actline.nextToken(fdel);
                                                        }
                                                        rIndex = stringDelimitedField.lastIndexOf(sdel);
                                                }
                                        }
                                        retstr = stringDelimitedField.substring(lIndex, rIndex);
                                }
							}
							else
							{//Estoy leyendo una linea sin delimitador de campo y no se ha finalizado de leer la misma
							 //Lo que resta por leer esta en currString
								retstr = currString;
							}
							String[] strParm = new String[1];
							strParm[0] = retstr;
							retval = processStringToRead(strParm, len);
							retstr = strParm[0];
                        }
                        catch (Exception e)
                        {
                                retval = GX_ASCDEL_INVALIDFORMAT;
                                if (trace_on)
                                        System.err.println("Error ADF0009: " + e);
                        }
                }
                else
                {
                        retval = GX_ASCDEL_INVALIDSEQUENCE;
                        if (trace_on)
                                System.err.println("Error ADF0004 o ADF0006");
                }

								if (isCsv && ((sdel.equals("") || sdel.equals("\"")) && (retstr.contains("\"\"") || (!fdel.equals("") && retstr.contains(fdel)))))
								{
									retstr = retstr.replace("\"\"", "\"");
									if (sdel.equals(""))
									{
										retstr = retstr.substring(1, retstr.length() -1);
									}
								}

                str[0] = retstr;
				try 
				{
					String stringDelimitedField = actline.nextToken(fdel);
				}
				catch(Exception e)
				{//Se sabe que se puede leer un token que no existe al final de la linea
				}
                return retval;
        }
		
	private byte processStringToRead(String[] currToken, int len)
	{
		byte retval = GX_ASCDEL_SUCCESS;
		if(!fdel.equals(""))
		{//Si hay un delimitador de campos, leo la cantidad de caracteres o bytes especificados
		 //truncando el string en caso que sea mas largo
			readingString = false;
			if (!lengthInBytes)
			{
				int strLength = currToken[0].length();
				if(strLength > len)
				{
					retval = GX_ASCDEL_OVERFLOW;
				    currToken[0] = currToken[0].substring(0, len);
				}
			}
			else
			{
				try
				{
					int bytesLength = currToken[0].getBytes(encoding).length;
					if(bytesLength > len)
					{
						retval = GX_ASCDEL_OVERFLOW;
						byte[] strBytes = currToken[0].getBytes(encoding);
						byte[] tmpBytes = new byte[len];
						for(int i=0; i< len; i++)
						{
							tmpBytes[i] = strBytes[i];
						}
						currToken[0] = new String(tmpBytes, 0, len, encoding);
					}
				}catch(UnsupportedEncodingException e){ System.err.println(e.toString()); }
			}
        }
		else
		{//Si no hay un delimitador de campos, leo la cantidad de caracteres o bytes especificados y guardo en
		 //currString el resto de los caracteres o bytes que quedan por leer de la linea
			if(!lengthInBytes)
			{
				int strLength = currToken[0].length();
				if(strLength > len)
				{
					readingString = true;
					currString = currToken[0].substring(len, strLength);
				    currToken[0] = currToken[0].substring(0, len);
				}
				else
				{//Se finalizo de leer la linea
					readingString = false;
					currString = "";
				}
			}
			else
			{
				try
				{
					int bytesLength = currToken[0].getBytes(encoding).length;
					if(bytesLength > len)
					{
						readingString = true;
						byte[] strBytes = currToken[0].getBytes(encoding);
						byte[] tmpBytes = new byte[len];
						byte[] newBytes = new byte[bytesLength - len];
						for(int i=0; i< bytesLength; i++)
						{
							if(i < len) tmpBytes[i] = strBytes[i];
							else newBytes[i - len] = strBytes[i];
						}
						currToken[0] = new String(tmpBytes, 0, len, encoding);
						currString = new String(newBytes, 0, bytesLength - len, encoding);
					}
					else
					{//Se finalizo de leer la linea
						readingString = false;
						currString = "";
					}
				}catch(UnsupportedEncodingException e){ System.err.println(e.toString()); }
			}
		}
		return retval;
	}

// dfrgdate

	public byte dfrgdate(java.util.Date[] date)
	{
		return dfrgdate(date, "ymd");
	}

	public byte dfrgdate(java.util.Date[] date, String fmt)
	{
		return dfrgdate(date, fmt, "-");
	}

	public byte dfrgdate(java.util.Date[] date, String fmt, String sep)
	{
		byte retval = GX_ASCDEL_SUCCESS;

		String retstr;
		Date retdate = CommonUtil.nullDate();
		int year = 0, month = 0, day = 0;

		if (dfropen_in_use)
		{
			try
			{
                          String stringDelimitedField = actline.nextToken(fdel);
                          if(fdel.equals(stringDelimitedField) || stringDelimitedField.equals(""))
                          { // Si el token debe estar vac�o...
                              stringDelimitedField = "";
                          }

				retstr = stringDelimitedField;

				StringTokenizer stok = new StringTokenizer(retstr, sep);

				for (int i = 0; i < 3; i++)
				{
					int value = (new Integer(stok.nextToken())).intValue();
					switch (fmt.charAt(i))
					{
						case 'y':
							   year  = value;
								break;
						case 'm':
							   	month = value;
								break;
						case 'd':
							   	day   = value;
								break;
						default:
							return GX_ASCDEL_BADFMTSTR;
					}
				}

				if	(month == 0 && day == 0 && year == 0)
				{
					retdate = CommonUtil.nullDate();
				}
				else if (month < 1 || month > 12 || day < 1 || day > 31)
				{
					retval = GX_ASCDEL_INVALIDDATE;
					if (trace_on)
						System.err.println("Error ADF0010");
				}
				else
				{
					Calendar calendar = GregorianCalendar.getInstance();
					calendar.set(year-1900, month - 1, day);
					retdate = calendar.getTime();
				}

			}
			catch (Exception e)
			{
				retval = GX_ASCDEL_INVALIDFORMAT;
			}
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if (trace_on)
				System.err.println("Error ADF0004 o ADF0006");
		}

		date[0] = retdate;
		try 
		{
			String stringDelimitedField = actline.nextToken(fdel);
		}
		catch(Exception e)
		{//Se sabe que se puede leer un token que no existe al final de la linea
		}		
		return retval;
	}

// dfrclose

	public byte dfrclose()
	{
		byte retval = GX_ASCDEL_SUCCESS;

		if (dfropen_in_use)
		{
			try
			{
				bufread.close();
				dfropen_in_use = false;
				encoding = "";
			}
			catch(Exception e)
			{
			}
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
		}
		return retval;
	}

// dfwopen

	public byte dfwopen(String filename)
	{
		return dfwopen(filename, ",");
	}

	public byte dfwopen(String filename, String fdel)
	{
		return dfwopen(filename, fdel, "\"");
	}

	public byte dfwopen(String pfilename, String pfdel, String psdel)
	{
		return dfwopen(pfilename, pfdel, psdel, 0);
	}

	public byte dfwopen(String pfilename, String pfdel, String psdel, int append)
	{
		return dfwopen(pfilename, pfdel, psdel, append, "");
	}

	public byte dfwopen(final String filename, String fdel, final String sdel, final int append, final String enc)
	{
		byte retval = GX_ASCDEL_SUCCESS;

		if	(fdel.equals("\\t"))
		{
			fdel = "\t";
		}

		if (!dfwopen_in_use)
		{
			dfwopen_in_use = true;

			this.sdel = sdel;
			this.fdel = fdel;
			this.filename = filename;
			this.encoding = enc;
			this.isCsv = filename.toUpperCase().endsWith(".CSV");

			try
			{
				SpecificImplementation.NativeFunctions.getInstance().executeWithPermissions(
					new RunnableThrows() {
						public Object run() throws Exception
						{
							if(encoding != null)
							{
								encoding = encoding.trim();
								if(!encoding.equals(""))
								{
									try
									{
										lengthInBytes = true;
										String bom = getByteOrderMark(encoding);
										encoding = CommonUtil.normalizeEncodingName(encoding);
										bufwrite = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( filename, append != 0), encoding ));
										if (bom != null)
											bufwrite.write(bom);
										return null;
									}catch(UnsupportedEncodingException e){ System.err.println(e.toString()); }
								}
							}
							bufwrite = new BufferedWriter( new FileWriter( filename, append != 0)  );
							return null;
						}
					}, INativeFunctions.FILE_ALL);
			}
			catch(Exception e)
			{
				retval = GX_ASCDEL_OPENERROR;
				dfwopen_in_use = false;
				if (trace_on)
					System.err.println("Error ADF0001: " + e);
			}

		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if (trace_on)
				System.err.println("Error ADF0005: open function in use");
		}

		return retval;
	}
	
	private String getByteOrderMark(String enc) throws UnsupportedEncodingException 
	{  
		if (enc!=null && enc.toUpperCase().equals("UTF-8 BOM")) { 
			byte[] bom = new byte[3];  
			bom[0] = (byte) 0xEF;  
			bom[1] = (byte) 0xBB;  
			bom[2] = (byte) 0xBF;  
			return new String(bom, "UTF8");  
		}else if (enc!=null && enc.toUpperCase().equals("UTF-16LE BOM")) { 
			byte[] bom = new byte[2];  
			bom[0] = (byte) 0xFF;  
			bom[1] = (byte) 0xFE;  
			return new String(bom, "UTF-16LE");  
		}else if (enc!=null && enc.toUpperCase().equals("UTF-16BE BOM")) { 
			byte[] bom = new byte[2];  
			bom[0] = (byte) 0xFE;  
			bom[1] = (byte) 0xFF;  
			return new String(bom, "UTF-16BE");  
		} else {  
			return null;  
		}
	}

	public void setNewLineBehavior(String newLine)
	{
		newLineBehavior = newLine;
	}

	// dfwnext
	public byte dfwnext()
	{
		byte retval = GX_ASCDEL_SUCCESS;

		if (dfwopen_in_use)
		{
			try
			{
				if	(toWrite != null)
				{
					bufwrite.write(toWrite);
				}

				if (newLineBehavior == null)
				{
					bufwrite.newLine();
				}
				else
				{
					bufwrite.write(newLineBehavior);
				}
				toWrite = null;

			}
			catch (IOException e)
			{
				retval = GX_ASCDEL_WRITEERROR;
				if (trace_on)
					System.err.println("Error ADF0003: " + e);
			}
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
		}

		return retval;
	}

	// dfwpnum
	public byte dfwpnum(int num)
	{
		dfwpnum(num, 0);
		return 0;
	}

	public byte dfwpnum(double num, int dec)
	{
		byte retval = GX_ASCDEL_SUCCESS;
		if (dfwopen_in_use)
		{
			String doubnum = CommonUtil.ltrim(CommonUtil.str(num, 20 + dec, dec));
			int k = doubnum.indexOf(".");
			if (dec == 0)
			{
				if	(k >= 0)
					doubnum = doubnum.substring(0, k);
			}
			else
			{
				String left;
				String right;

				if	(k != -1)
				{
					left  = doubnum.substring(0, k);
					right = doubnum.substring(k, doubnum.length());
				}
				else
				{
					left  = doubnum;
					right = ".";
				}

				doubnum = left + CommonUtil.padr(right, dec + 1, "0");
			}

			if (toWrite == null)
				toWrite = doubnum;
			else
				toWrite = toWrite + fdel + doubnum;
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if (trace_on)
				System.err.println("ADF0004");
		}
		return retval;
	}

	// dfwptxt
	public byte dfwptxt(String txt, int len)
	{
		byte retval = GX_ASCDEL_SUCCESS;
		
		if (dfwopen_in_use)
		{
			//txt = txt.trim();
			
			if (len == 0)
			{
				int strlen = txt.length();
				try
				{
					if(lengthInBytes) strlen = txt.getBytes(encoding).length;
				}catch(UnsupportedEncodingException e){ System.err.println(e.toString()); }
				
				len = strlen;
			}
			
			if (toWrite == null)
				toWrite = sdel + processStringToWrite(txt, len) + sdel ;
			else
				toWrite = toWrite + fdel + sdel + processStringToWrite(txt, len) + sdel ;
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if (trace_on)
				System.err.println("ADF0004");
		}
		return retval;
	}


	// dfwpdate
	public byte dfwpdate(Date date)
	{
		return dfwpdate( date , "ymd");
	}

	public byte dfwpdate(Date date, String fmt)
	{
		return dfwpdate( date, fmt, "-");
	}

	public byte dfwpdate(Date date, String fmt, String sep)
	{
		byte retval = GX_ASCDEL_SUCCESS;
		if (dfwopen_in_use)
		{
			String day, month, year;

			if	(date.equals(CommonUtil.nullDate()))
			{
				day   = "00";
				month = "00";
				year  = "0000";
			}
			else
			{
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(date);
				day   =  CommonUtil.padl(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)), 2, "0");
				month =  CommonUtil.padl(Integer.toString(calendar.get(Calendar.MONTH) + 1), 2, "0");
				year  =  CommonUtil.padl(Integer.toString(calendar.get(Calendar.YEAR) + 1900), 4, "0");
			}

			if	(toWrite == null)
				toWrite = "";
			else
			 	toWrite += fdel;

			for (int i = 0; i < 3; i++)
			{
				switch (fmt.charAt(i))
				{
					case 'y':
						toWrite += year + (i == 2?"":sep);
						break;
					case 'm':
						toWrite += month + (i == 2?"":sep);
						break;
					case 'd':
						toWrite += day   + (i == 2?"":sep);
						break;
					default:
						 if (trace_on)
							System.err.println("ADF0012");

						 return GX_ASCDEL_BADFMTSTR;
				}
			}
		}
		else
		{
			retval = GX_ASCDEL_INVALIDSEQUENCE;
			if (trace_on)
				System.err.println("ADF0004");
		}
		return retval;
	}

	// dfwclose
	public byte dfwclose()
	{
		byte retval = GX_ASCDEL_SUCCESS;
		if (dfwopen_in_use)
		{
			try
			{
				bufwrite.close();
				dfwopen_in_use = false;
				encoding = "";
			}
			catch(IOException e)
			{
				System.err.println("Error closing");
			}

		}else{
			retval = GX_ASCDEL_INVALIDSEQUENCE;

		}
		return retval;
	}
	
	private String processStringToWrite(String currToken, int len)
	{//En el caso que el string sea mas corto que el largo especificado, agrego los caracteres o bytes que falten
		int lenDiff = 0;
		int tokenLength;
		if (!lengthInBytes)
        {
        	tokenLength = currToken.length();
        	if (tokenLength > len)
        		currToken = currToken.substring(0, len);
        	else
        		lenDiff = len - tokenLength;
        }
		else
		{
			try
			{
				tokenLength = currToken.getBytes(encoding).length;
				if (tokenLength > len)
				{
					byte[] b1 = currToken.getBytes(encoding);
					byte[] b2 = new byte[len];
					System.arraycopy(b1, 0, b2, 0, len);
					currToken = new String(b2, encoding);
				}
				else
					lenDiff = len - tokenLength;
					
			}catch(UnsupportedEncodingException e){ System.err.println(e.toString()); }
		}
		for(int i = 0; i < lenDiff; i++)
			currToken = currToken.concat(" ");

		if (isCsv && ((sdel.equals("") || sdel.equals("\"")) && (currToken.contains("\"") || (!fdel.equals("") && currToken.contains(fdel)) || currToken.contains(CRLF))))
		{
			currToken = currToken.replace("\"", "\"\"");
			if (sdel.equals(""))
			{
				currToken = '"' + currToken + '"'; 
			}
		}	
			
		return currToken;
	}
	
/*
// main
	static public void main(String[] args)
	{
		   int i;
		   int CliCod = 0,CliCId = 0;
		   String CliNom = null;
		   Date CliFecNac = null;

//		   ascii.dfropen( "clients.txt", 80, ",");
//		   i=ascii.dfrnext( );
//		   while (i == 0)
//		   {
//		       ascii.dfrgnum( CliCod );
//		       ascii.dfrgtxt( CliNom );
//			   ascii.dfrgnum( CliCId );
//			   ascii.dfrgdate( CliFecNac, "ymd", "-" );
//		       i=ascii.dfrnext( );
//		   }
//		   ascii.dfrclose( );

//////////////////////////////

		   ascii.dfwopen( "prueba.txt");

	       ascii.dfwpnum( 10 );
	       ascii.dfwptxt( "Pepe");
	       ascii.dfwpnum( 1020 );
	       ascii.dfwpdate( new Date(1999-1900,10-1,15), "ymd", "-" );
		   i=ascii.dfwnext( );

	       ascii.dfwpnum( 20 );
	       ascii.dfwptxt( "Luis");
	       ascii.dfwpnum( 1030.567 , 0 );
	       ascii.dfwpdate( new Date(1998-1900,11-1,16), "ymd", "-" );
		   i=ascii.dfwnext( );

	       ascii.dfwpnum( 30 );
	       ascii.dfwptxt( "Pedro");
	       ascii.dfwpnum( 1040);
	       ascii.dfwpdate( new Date(1999-1900,12-1,31), "ymd", "-" );
	       i=ascii.dfwnext( );
		   ascii.dfwclose( );

//////////////////////////////

		   ascii.dfropen( "prueba.txt");
		   i=ascii.dfrnext( );
		   while (i == 0)
		   {
		       ascii.dfrgnum( CliCod );
		       ascii.dfrgtxt( CliNom );
			   ascii.dfrgnum( CliCId );
			   ascii.dfrgdate( CliFecNac, "ymd", "-" );
		       i=ascii.dfrnext( );
		   }
		   ascii.dfrclose( );
	}
*/
}
