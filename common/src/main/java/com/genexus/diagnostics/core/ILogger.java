package com.genexus.diagnostics.core;

public interface ILogger {
	
	void fatal(String msg, Throwable ex);

	void fatal(String msg1, String msg2, Throwable ex);

	void fatal(Throwable ex, String[] list);

	void fatal(String[] list);
	
	void fatal(String msg);
	
	void error(String msg, Throwable ex);

	void error(String msg1, String msg2, Throwable ex);

	void error(Throwable ex, String[] list);

	void error(String[] list);
	
	void error(String msg);

	void warn(String msg);
	
	void warn(Throwable ex, String[] list);

	void warn(String[] list);

	void warn(String msg, Throwable ex);

	void debug(String msg);
	
	void debug(Throwable ex, String[] list);

	void debug(String[] list);

	void debug(String msg1, String msg2, Throwable ex);

	void debug(String msg, Throwable ex);

	void info(String[] list);
	
	void info(String msg);

	void trace(String msg);
	
	void trace(Throwable ex, String[] list);

	void trace(String[] list);

	void trace(String msg1, String msg2, Throwable ex);

	void trace(String msg, Throwable ex);
	
	boolean isDebugEnabled();

	boolean isErrorEnabled();

	// Lambda Functions not supported JAVA 7. Only Java 8.
	/*
	 * public static void debug(Logger log, String startMsg, Func<String> buildMsg)
	 * { if (log.isDebugEnabled()) { String msg = buildMsg(); log.debug(startMsg +
	 * msg); } }
	 */

	
}
