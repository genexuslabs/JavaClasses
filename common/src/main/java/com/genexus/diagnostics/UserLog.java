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
			String loggerName = topic.startsWith("$") ? topic.substring(1) : String.format("%s.%s", defaultUserLogNamespace, topic.trim());
			log = LogManager.getLogger(loggerName);
		} else {
			log = getMainLogger();
		}
		return log;
	}

	public static void write(int logLevel, String message, String topic) {
		ILogger log = getLogger(topic);
		LogLevel level = LogLevel.fromInt(logLevel);

		switch (level) {
			case OFF: //LogLevel off
				break;
			case TRACE:
				log.trace(message);
				break;
			case INFO:
				log.info(message);
				break;
			case WARN:
				log.warn(message);
				break;
			case ERROR:
				log.error(message);
				break;
			case FATAL:
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

	public static void setContext(String key, Object value) {
		// Topic is ignored, also if you put something
		getLogger("$").setContext(key, value);
	}

	public static void write(String message, String topic, int logLevel, Object data, boolean stackTrace) {
		getLogger(topic).write(message, logLevel, data, stackTrace);
	}

	public static void write(String message, String topic, int logLevel, Object data) {
		write(message, topic, logLevel, data, false);
	}

	public static boolean isDebugEnabled() {
		return getLogger().isDebugEnabled();
	}

	public static boolean isErrorEnabled() {
		return getLogger().isErrorEnabled();
	}

	public static boolean isFatalEnabled() {
		return getLogger().isFatalEnabled();
	}

	public static boolean isInfoEnabled() {
		return getLogger().isInfoEnabled();
	}

	public static boolean isWarnEnabled() {
		return getLogger().isWarnEnabled();
	}

	public static boolean isTraceEnabled() {
		return getLogger().isTraceEnabled();
	}

	public static boolean isEnabled(int logLevel) {
		return getLogger().isEnabled(logLevel);
	}

	public static boolean isEnabled(int logLevel, String topic) {
		return getLogger(topic).isEnabled(logLevel);
	}


}
