package com.genexus.diagnostics;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class UserLog {

	private static ILogger getLogger() {
		return getLogger("");
	}

	public static String defaultUserLogNamespace = SpecificImplementation.UserLog.GetLogName();

	public static ILogger getMainLogger() {
		return LogManager.getLogger(defaultUserLogNamespace);
	}

	private static ILogger getLogger(String topic) {
		ILogger log;
		if (topic != null && topic.length() > 0) {
			log = LogManager.getLogger(String.format("%s.%s", defaultUserLogNamespace, topic.trim()));
		}
		else {
			log = getMainLogger();
		}
		return log;
	}

	public static void write( int logLevel, String message, String topic) {
		ILogger log = getLogger(topic);

		switch (logLevel) {
			case LogLevel.OFF: //LogLevel off
				break;
			case LogLevel.TRACE:
				log.trace(message);
				break;
			case LogLevel.DEBUG:
				log.debug(message);
				break;
			case LogLevel.INFO:
				log.info(message);
				break;
			case LogLevel.WARNING:
				log.warn(message);
				break;
			case LogLevel.ERROR:
				log.error(message);
				break;
			case LogLevel.FATAL:
				log.fatal(message);
				break;
			default:
				log.debug(message);
		}
	}

	public static void write(String message) {
		getLogger().debug(message);
	}

	public static void write(String message, String topic, int logLevel) {
		write(logLevel, message, topic);
	}

	public static void error(String message) {
		getLogger().error(message);
	}

	public static void error(String message, String topic) {
		getLogger(topic).error(message);
	}

	public static void error(String message, String topic, Throwable ex) {
		getLogger(topic).error(message, ex);
	}

	public static void fatal(String message) {
		getLogger().fatal(message);
	}

	public static void fatal(String message, String topic) {
		getLogger(topic).fatal(message);
	}

	public static void fatal(String message, String topic, Throwable ex) {
		getLogger(topic).fatal(message, ex);
	}

	public static void warning(String message) {
		getLogger().warn(message);
	}

	public static void warning(String message, String topic) {
		getLogger(topic).warn(message);
	}

	public static void warning(String message, String topic, Throwable ex) {
		getLogger(topic).warn(message, ex);
	}

	public static void info(String message) {
		getLogger().info(message);
	}

	public static void info(String message, String topic) {
		getLogger(topic).info(message);
	}

	public static void debug(String message) {
		getLogger().debug(message);
	}

	public static void debug(String message, String topic) {
		getLogger(topic).debug(message);
	}

	public static void debug(String message, String topic, Throwable ex) {
		getLogger(topic).debug(message, ex);
	}
}
