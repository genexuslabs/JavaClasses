package com.genexus.diagnostics;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class Log {
	private static ILogger getLogger() {
		return getLogger("");
	}

	public static ILogger getMainLogger() {
		return LogManager.getLogger("com.genexus.logging");
	}

	private static ILogger getLogger(String topic) {
		ILogger log;
		if (topic != null && topic.length() > 0) {
			log = LogManager.getLogger(topic);
		} else {
			log = getMainLogger();
		}
		return log;
	}

	public static void write(int logLevel, String message, String topic) {
		write(message, topic, logLevel);
	}

	public static void write(String message, String topic, int logLevel) {
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

	public static void write(String message, String topic) {
		getLogger(topic).debug(message);
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
