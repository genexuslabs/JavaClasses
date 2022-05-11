package com.genexus.diagnostics.core.provider;

import com.genexus.diagnostics.core.ILogger;

public class Log4J2Logger implements ILogger {
	
	//Java implementation
	public Log4J2Logger (final Class<?> clazz) {
		log = org.apache.logging.log4j.LogManager.getLogger(clazz);
	}
	
	public Log4J2Logger (String clazz) {
		log = org.apache.logging.log4j.LogManager.getLogger(clazz);
	}
	
	org.apache.logging.log4j.Logger log = null;
	
	
	@Override
	public void fatal(String msg) {
		if (log.isFatalEnabled()) {
			log.fatal(msg);
		}
	}
	
	public void fatal(String msg, Throwable ex) {
		if (log.isFatalEnabled()) {
			log.fatal(msg, ex);
		}
	}

	public void fatal(String msg1, String msg2, Throwable ex) {
		if (log.isFatalEnabled()) {
			log.fatal(msg1 + msg2, ex);
		}
	}

	public void fatal(Throwable ex, String[] list) {
		if (log.isFatalEnabled()) {
			for (String parm : list) {
				log.fatal(parm);
			}
		}
	}

	public void fatal(String[] list) {
		if (log.isFatalEnabled()) {
			fatal(null, list);
		}
	}
	
	public void error(String msg, Throwable ex) {
		if (log.isErrorEnabled()) {
			log.error(msg, ex);
		}
	}

	public void error(String msg1, String msg2, Throwable ex) {
		if (log.isErrorEnabled()) {
			log.error(msg1 + msg2, ex);
		}
	}

	public void error(Throwable ex, String[] list) {
		if (log.isErrorEnabled()) {
			for (String parm : list) {
				log.error(parm);
			}
		}
	}

	public void error(String[] list) {
		error(null, list);
	}
	
	public void error(String msg) {
		if (log.isErrorEnabled()) {
			log.error(msg);
		}
	}

	public void warn(Throwable ex, String[] list) {
		if (log.isWarnEnabled()) {
			StringBuilder msg = new StringBuilder();
			for (String parm : list) {
				msg.append(parm);
			}
			if (ex != null)
				log.warn(msg, ex);
			else
				log.warn(msg);
		}
	}

	public void warn(String[] list) {
		warn(null, list);
	}

	public void warn(String msg, Throwable ex) {
		if (log.isWarnEnabled()) {
			log.warn(msg, ex);
		}
	}

	public void debug(String msg) {
		if (log.isDebugEnabled()) {
			log.debug(msg);
		}
	}
	
	
	public void debug(Throwable ex, String[] list) {
		if (log.isDebugEnabled()) {
			StringBuilder msg = new StringBuilder();
			for (String parm : list) {
				msg.append(parm);
			}
			if (ex != null)
				log.debug(msg, ex);
			else
				log.debug(msg);
		}
	}

	public void debug(String[] list) {
		if (log.isDebugEnabled()) {
			debug(null, list);
		}
	}

	// Lambda Functions not supported JAVA 7. Only Java 8.
	/*
	 * public static void debug(Logger log, String startMsg, Func<String> buildMsg)
	 * { if (log.isDebugEnabled()) { String msg = buildMsg(); log.debug(startMsg +
	 * msg); } }
	 */

	public void debug(String msg1, String msg2, Throwable ex) {
		if (log.isDebugEnabled()) {
			log.debug(msg1 + msg2, ex);
		}
	}

	public void debug(String msg, Throwable ex) {
		if (log.isDebugEnabled()) {
			log.debug(msg, ex);
		}
	}

	public void info(String[] list) {
		if (log.isInfoEnabled()) {
			StringBuilder msg = new StringBuilder();
			for (String parm : list) {
				msg.append(parm);
			}
			log.info(msg);
		}
	}
	
	public void info(String msg) {
		if (log.isInfoEnabled()) {
			log.info(msg);
		}
	}
	
	public void warn(String msg) {
		if (log.isWarnEnabled()) {
			log.warn(msg);
		}
	}
	

	
	public void trace(String msg) {
		if (log.isTraceEnabled()) {
			log.trace(msg);
		}
	}
	
	
	public void trace(Throwable ex, String[] list) {
		if (log.isTraceEnabled()) {
			StringBuilder msg = new StringBuilder();
			for (String parm : list) {
				msg.append(parm);
			}
			if (ex != null)
				log.trace(msg, ex);
			else
				log.trace(msg);
		}
	}

	public void trace(String[] list) {
		if (log.isTraceEnabled()) {
			trace(null, list);
		}
	}

	// Lambda Functions not supported JAVA 7. Only Java 8.
	/*
	 * public static void debug(Logger log, String startMsg, Func<String> buildMsg)
	 * { if (log.isDebugEnabled()) { String msg = buildMsg(); log.debug(startMsg +
	 * msg); } }
	 */

	public void trace(String msg1, String msg2, Throwable ex) {
		if (log.isTraceEnabled()) {
			log.trace(msg1 + msg2, ex);
		}
	}

	public void trace(String msg, Throwable ex) {
		if (log.isTraceEnabled()) {
			log.trace(msg, ex);
		}
	}
	
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}
	
}
