package com.genexus.diagnostics.core.provider;

import com.genexus.diagnostics.core.ILogger;

public class Log4J2Logger implements ILogger {
	private org.apache.logging.log4j.Logger log;

	public Log4J2Logger(final Class<?> clazz) {
		log = org.apache.logging.log4j.LogManager.getLogger(clazz);
	}

	public Log4J2Logger(String clazz) {
		log = org.apache.logging.log4j.LogManager.getLogger(clazz);
	}

	@Override
	public void fatal(String msg) {
		log.fatal(msg);
	}

	public void fatal(String msg, Throwable ex) {
		log.fatal(msg, ex);
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
		fatal(null, list);
	}

	public void error(String msg, Throwable ex) {
		log.error(msg, ex);
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
		log.error(msg);
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
		log.warn(msg, ex);
	}

	public void debug(String msg) {
		log.debug(msg);
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
		debug(null, list);
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
		log.debug(msg, ex);
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
		log.info(msg);
	}

	public void warn(String msg) {
		log.warn(msg);
	}

	public void trace(String msg) {
		log.trace(msg);
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
		trace(null, list);
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
		log.trace(msg, ex);
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

}
