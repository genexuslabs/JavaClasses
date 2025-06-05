package com.genexus.diagnostics.core.provider;

import com.genexus.diagnostics.LogLevel;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.GxUserType;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;
import org.apache.logging.log4j.message.ObjectMessage;

import java.lang.reflect.Type;
import java.util.*;

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
		fatal((Throwable) null, list);
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
		error((Throwable) null, list);
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
		warn((Throwable) null, list);
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
		debug((Throwable) null, list);
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
		trace((Throwable) null, list);
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







	/******** NEW methods to improve logging ********/

	public void setContext(String key, Object value) {
		// Add entry to the MDC (only works for JSON log format)
		ThreadContext.put(key, fromObjectToString(value));
	}

	private ObjectMessage buildLogMessage(String messageKey, Object messageValue, boolean stackTrace) {
		Map<String, Object> messageMap;
		String stacktraceLabel = "stackTrace";
		String msgLabel = "message";

		if (isNullOrBlank(messageValue)) {
			if (stackTrace) {
				messageMap = new LinkedHashMap<>();
				messageMap.put(msgLabel, messageKey);
				messageMap.put(stacktraceLabel, getStackTraceAsList());
				if(isJsonLogFormat())
					return new ObjectMessage(messageMap);
				else
					return new ObjectMessage(new Gson().toJson(messageMap));
			}
			return new ObjectMessage(messageKey);
		} else {
			messageMap = objectToMap(messageKey, messageValue);
			if (stackTrace) {
				messageMap.put(stacktraceLabel, getStackTraceAsList());
			}
			if(isJsonLogFormat())
				return new ObjectMessage(messageMap);
			else
				return new ObjectMessage(new Gson().toJson(messageMap));
		}
	}

	public void write(String message, int logLevel, Object data, boolean stackTrace) {
		printLog("data", data, stackTrace, Level.DEBUG);
	}

	private void printLog(final String messageKey, final Object messageValue, final boolean stackTrace,
						  final Level logLevel) {

		/* Generate the message JSON in this format:
		 * { "message" :
		 * 	{
		 * 		"messageKey": "USER messageValue",
		 * 	}
		 * }
		 * */
		ObjectMessage om = buildLogMessage(messageKey, messageValue, stackTrace);

		// Log the message received or the crafted msg
		if (logLevel.equals(Level.FATAL))      log.fatal(om);
		else if (logLevel.equals(Level.ERROR)) log.error(om);
		else if (logLevel.equals(Level.WARN))  log.warn(om);
		else if (logLevel.equals(Level.INFO))  log.info(om);
		else if (logLevel.equals(Level.DEBUG)) log.debug(om);
		else if (logLevel.equals(Level.TRACE)) log.trace(om);
	}



	private static String fromObjectToString(Object value) {
		String res = "";
		if (value == null) {
			res = "null";
		} else if (value instanceof String && isJson((String) value)) {
			// Avoid double serialization
			res = (String) value;
		} else if (value instanceof String) {
			res = (String) value;
		} else if (value instanceof Number || value instanceof Boolean) {
			res = value.toString();
		} else if (value instanceof Map || value instanceof List) {
			res = new Gson().toJson(value);
		} else if (value instanceof GxUserType) {
			res = ((GxUserType) value).toJSonString();
		} else {
			// Any other object → serialize as JSON
			res = new Gson().toJson(value);
		}
		return res;
	}

	private static boolean isJson(String input) {
		try {
			JsonElement json = JsonParser.parseString(input);
			return json.isJsonObject() || json.isJsonArray();
		} catch (Exception e) {
			return false;
		}
	}

	private Map<String, Object> objectToMap(String key, Object value) {
		Map<String, Object> result = new LinkedHashMap<>();
		if (value == null) {
			result.put(key, null);
		} else if (value instanceof Number || value instanceof Boolean
				   || value instanceof Map || value instanceof List) {
			result.put(key, value);
		} else if (value instanceof GxUserType) {
			result.put(key, jsonStringToMap(((GxUserType) value).toJSonString()));
		} else if (value instanceof String) {
			String str = (String) value;

			// Try to parse as JSON
			try {
				JsonElement parsed = JsonParser.parseString(str);
				Gson gson = new Gson();
				if (parsed.isJsonObject()) {
					result.put(key, gson.fromJson(parsed, Map.class));
				} else if (parsed.isJsonArray()) {
					result.put(key, gson.fromJson(parsed, List.class));
				} else if (parsed.isJsonPrimitive()) {
					JsonPrimitive primitive = parsed.getAsJsonPrimitive();
					if (primitive.isBoolean()) {
						result.put(key, primitive.getAsBoolean());
					} else if (primitive.isNumber()) {
						result.put(key, primitive.getAsNumber());
					} else if (primitive.isString()) {
						result.put(key, primitive.getAsString());
					}
				}
			} catch (JsonSyntaxException e) {
				// Invalid JSON: it is left as string
				result.put(key, str);
			}
		} else {
			// Any other object: convert to string
			result.put(key, value.toString());
		}
		return result;
	}

	private static String getStackTrace() {
		StringBuilder stackTrace;
		stackTrace = new StringBuilder();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			stackTrace.append(ste).append(System.lineSeparator());
		}
		return stackTrace.toString();
	}

	private static List<String> getStackTraceAsList() {
		List<String> stackTraceLines = new ArrayList<>();
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			stackTraceLines.add(ste.toString());
		}
		return stackTraceLines;
	}

	private static String stackTraceListToString(List<String> stackTraceLines) {
		return String.join(System.lineSeparator(), stackTraceLines);
	}

	// Convert a JSON String to Map<String, Object>
	private static Map<String, Object> jsonStringToMap(String jsonString) {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		return gson.fromJson(jsonString, type);
	}

	private String toJson(String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		return new Gson().toJson(map);
	}

	private static boolean isJsonLogFormat() {
		LoggerContext context = (LoggerContext) LogManager.getContext(false);
		Configuration config = context.getConfiguration();

		for (Appender appender : config.getAppenders().values()) {
			if (appender instanceof AbstractAppender) {
				Object layout = ((AbstractAppender) appender).getLayout();
				if (layout instanceof JsonTemplateLayout) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isNullOrBlank(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof String) {
			return ((String) obj).trim().isEmpty();
		}
		return false; // It is not null, and it isn't an empty string
	}

}
