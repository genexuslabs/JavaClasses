package com.genexus.diagnostics.core.provider;

import com.genexus.diagnostics.LogLevel;
import com.genexus.diagnostics.core.ILogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;
import org.apache.logging.log4j.message.MapMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	public boolean isFatalEnabled() {
		return log.isFatalEnabled();
	}

	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	public boolean isEnabled(int logLevel) {
		return log.isEnabled(getLogLevel(logLevel));
	}

	public boolean isEnabled(int logLevel, String marker) {
		return log.isEnabled(getLogLevel(logLevel), MarkerManager.getMarker(marker));
	}

	public void setContext(String key, Object value) {
		// Add entry to the MDC (only works for JSON log format)
		ThreadContext.put(key, fromObjectToString(value));
	}

	public void write(String message, int logLevel, Object data, boolean stackTrace) {
		if (isJsonLogFormat())
			writeJsonFormat(message, logLevel, data, stackTrace);
		else
			writeTextFormat(message, logLevel, data, stackTrace);
	}

	private static final String STACKTRACE_KEY = "stackTrace";
	private static final String MESSAGE_KEY = "message";

	private void writeTextFormat(String message, int logLevel, Object data, boolean stackTrace) {
		String dataKey = "data";
		Map<String, Object> mapMessage = new LinkedHashMap<>();

		if (data == null || (data instanceof String && "null".equals(data.toString()))) {
			mapMessage.put(dataKey, JSONObject.NULL);
		} else if (data instanceof String && isJson((String) data)) { // JSON Strings
			mapMessage.put(dataKey, jsonStringToMap((String)data));
		} else {
			mapMessage.put(dataKey, data);
		}

		if (stackTrace) {
			mapMessage.put(STACKTRACE_KEY, getStackTraceAsList());
		}

		String json = new JSONObject(mapMessage).toString();
		String format = "{} - {}";
		log.log(getLogLevel(logLevel), format, message, json);
	}

	private void writeJsonFormat(String message, int logLevel, Object data, boolean stackTrace) {
		String dataKey = "data";
		MapMessage<?, ?> mapMessage = new MapMessage<>().with(MESSAGE_KEY, message);

		if (data == null || (data instanceof String && "null".equals(data.toString()))) {
			mapMessage.with(dataKey, (Object) null);
		} else if (data instanceof String && isJson((String) data)) { // JSON Strings
			mapMessage.with(dataKey, jsonStringToMap((String)data));
		} else {
			mapMessage.with(dataKey, data);
		}

		if (stackTrace) {
			mapMessage.with(STACKTRACE_KEY, getStackTraceAsList());
		}

		log.log(getLogLevel(logLevel), mapMessage);
	}

	private Level getLogLevel(int logLevel) {
		LogLevel level = LogLevel.fromInt(logLevel);
		switch (level) {
			case OFF: return Level.OFF;
			case TRACE: return Level.TRACE;
			case INFO: return Level.INFO;
			case WARNING: return Level.WARN;
			case ERROR: return Level.ERROR;
			case FATAL: return Level.FATAL;
			default: return Level.DEBUG;
		}
	}

	private static String fromObjectToString(Object value) {
		String res;
		if (value == null) {
			res = "null";
		} else if (value instanceof String && isJson((String) value)) {
			// Avoid double serialization
			res = (String) value;
		} else if (value instanceof String) {
			res = (String) value;
		} else if (value instanceof Number || value instanceof Boolean) {
			res = value.toString();
		} else if (value instanceof Map) {
			res = new JSONObject((Map<?, ?>) value).toString();
		} else if (value instanceof List) {
			res = new JSONArray((List<?>) value).toString();
		} else {
			// Any other object → serialize as JSON
			res = JSONObject.quote(value.toString());
		}
		return res;
	}

	private static boolean isJson(String str) {
		try {
			new JSONObject(str);
			return true;
		} catch (Exception e1) {
			try {
				new JSONArray(str);
				return true;
			} catch (Exception e2) {
				return false;
			}
		}
	}

	private static List<String> getStackTraceAsList() {
		List<String> stackTraceLines = new ArrayList<>();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			stackTraceLines.add(ste.toString());
		}
		return stackTraceLines;
	}

	private static boolean isJsonLogFormat() {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();

		for (Appender appender : config.getAppenders().values()) {
			if (appender instanceof AbstractAppender) {
				Object layout = appender.getLayout();
				if (layout instanceof JsonTemplateLayout) {
					return true;
				}
			}
		}
		return false;
	}

	public static Map<String, Object> jsonStringToMap(String jsonString) {
		JSONObject jsonObject = new JSONObject(jsonString);
		return toMap(jsonObject);
	}

	private static Map<String, Object> toMap(JSONObject jsonObject) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (String key : jsonObject.keySet()) {
			Object value = jsonObject.get(key);
			map.put(key, convert(value));
		}
		return map;
	}

	private static List<Object> toList(JSONArray array) {
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			list.add(convert(value));
		}
		return list;
	}

	private static Object convert(Object value) {
		if (value instanceof JSONObject) {
			return toMap((JSONObject) value);
		} else if (value instanceof JSONArray) {
			return toList((JSONArray) value);
		} else if (value.equals(JSONObject.NULL)) {
			return null;
		} else {
			return value;
		}
	}
}
