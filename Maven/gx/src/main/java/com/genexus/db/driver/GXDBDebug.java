// $Log: GXDBDebug.java,v $
// Revision 1.1  2001/10/12 22:03:42  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/10/12 22:03:42  gusbro
// GeneXus Java Olimar
//

package com.genexus.db.driver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.genexus.CommonUtil;
import com.genexus.GXutil;
import com.genexus.ICleanedup;
//import com.genexus.DebugFlag;

public final class GXDBDebug implements ICleanedup
{
	private static final boolean DEBUG       = com.genexus.DebugFlag.DEBUG;
	public static final byte LOG_MAX = 0;
	public static final byte LOG_MED = 1;
	public static final byte LOG_MIN = 2;
	public static final int POOL_ACTIVITY = 1;

	private PrintWriter log;
	private int logLineCount = 0;
	private GregorianCalendar cal = new GregorianCalendar();
	private String fileName = "";

	boolean logEnabled = false;
	JDBCLogConfig cfg;

	public GXDBDebug(JDBCLogConfig cfg)
	{
		this.cfg = cfg;
		this.logLineCount = 0;
		if	(DEBUG)
		{
			this.logEnabled = cfg.enabled;

			if	(com.genexus.ApplicationContext.getInstance().isGXUtility())
				logEnabled = false;

			if	(logEnabled)
			{
				fileName = cfg.getFileName();

				try
				{
					Writer baseWriter = new FileWriter(fileName);
					if	(cfg.buffered)
					{
						baseWriter = new BufferedWriter(baseWriter);
					}

				 	log = new PrintWriter(baseWriter);
				}
				catch (IOException e)
				{	
					logEnabled = false;
					/*System.err.println("Can't create log file " + fileName + " " + e.toString()); Do not write to output console*/
				}

				if	(logEnabled)
				{
					/*System.err.println("Opening log file: " + fileName); Do not write to output console*/
					
					log.println("Java vendor       : " + System.getProperty("java.vendor"));
					log.println("Java version      : " + System.getProperty("java.version"));
					log.println("Generator version : " + com.genexus.Version.version);
					log.println("Java classpath    : " + System.getProperty("java.class.path"));
					log.println("Operating System  : " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "/" + System.getProperty("os.arch"));
					log.println("Current DateTime  : " + new java.util.Date());
					log.println("");
					flushLog();
				}
			}
		}
	}

	public void closeLog()
	{
		if	(logEnabled && log != null)
		{
			log.close();
			logEnabled = false;
		}
	}

	public void cleanup()
	{
		closeLog();
	}

	public void flushLog()
	{
		if	(logEnabled && log != null)
		{
			log.flush();
		}
	}

	public void logException(int handle, Exception e, Object object)
	{
		if	(e instanceof SQLException)
			logSQLException(handle, (SQLException) e, object);
	}

	public void logSQLException(int handle, SQLException e, Object obj)
	{
		if	(!logEnabled)
			return;

		e.printStackTrace(log);

		while (e != null)
		{
			log(handle, "Exception: SQLState: " + e.getSQLState () + " ErrorCode: " + e.getErrorCode() + " in " + obj.hashCode());
			log(handle, "           Message : " + e.getMessage ());
			log(handle, "           Message : " + e.getMessage ());
			e = e.getNextException();
		}

		if	(!cfg.buffered)
			flushLog();
	}

	void logWarnings(SQLWarning warn) throws SQLException  
	{
		if (warn != null && logEnabled && log != null)
		{
			while (warn != null) {
				log.println ("Warning: SQLState: " + warn.getSQLState () + " ErrorCode: " + warn.getErrorCode());
				log.println ("         Message : " + warn.getMessage ());
				warn = warn.getNextWarning ();
			}
		}
		if	(!cfg.buffered)
			flushLog();

	}

        public void logComment(int level, Object source, int handle, String text)
        {
		if(level >= cfg.detail)
                    logComment(source, handle, text);
        }
    
	public final void logComment(int level, int handle, String text)
	{	
		logComment(handle, text);
	}

	public final void logComment(int handle, String text)
	{	
		if	(logEnabled)
			log(handle, "# " + text);
	}

	public final void logComment(Object source, String text)
	{
		logComment(source, 0, text);
	}

	public final void logComment(Object source, int handle, String text)
	{
		if	(logEnabled)
			log(source, handle, "# " + text);
	}

	final void log(int level, Object source, int handle, String text)
	{
		if	(level >= cfg.detail)
		{
			log(source, handle, text);
		}
	}

	final void log(Object source, int handle, String text)
	{
		if	(logEnabled)
		{
			String name = source.getClass().getName();
				
			log(handle, name.substring(name.lastIndexOf('.', name.length()) + 1) + "/" + source.hashCode() + ": " + text);
		}
	}

	public final void log(int handle, String text)
	{
		if	(logEnabled && log != null)
		{
			cal.setTime(new java.util.Date(System.currentTimeMillis()));

			String time = padstr(cal.get(Calendar.HOUR_OF_DAY), 2) + ":" + 
						  padstr(cal.get(Calendar.MINUTE     ), 2) + ":" + 
						  padstr(cal.get(Calendar.SECOND     ), 2) + "." + 
						  padstr(cal.get(Calendar.MILLISECOND), 3);

			log.println(GXutil.str(logLineCount++, 8, 0) + "(" + handle + ")" + "(" + time + ")" + text );

			if	(!cfg.buffered)
				flushLog();
		}
	}

	static int getJDBCObjectId(Object conString)
	{
		return  conString.hashCode();
	}

	private static String padstr(int number, int length)
	{
		return CommonUtil.padl(CommonUtil.ltrim(GXutil.str(number, length, 0)), length, "0");
	}

	public boolean isLogEnabled()
	{
		return logEnabled;
	}

	public void close(int level)
	{
		if	(logEnabled && level == cfg.level && log != null)
		{
			System.err.println("Closing " + fileName + "...");
			log.close();
		}
	}

}
