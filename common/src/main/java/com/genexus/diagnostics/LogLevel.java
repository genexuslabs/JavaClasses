package com.genexus.diagnostics;

public enum LogLevel {
	OFF(0),
	TRACE(1),
	DEBUG(5),
	INFO(10),
	WARN(15),
	ERROR(20),
	FATAL(30);

	private final int lvl;
	LogLevel(int lvl) { this.lvl = lvl; }
	public int intValue() { return lvl; }

	public static LogLevel fromInt(int lvl) {
		for (LogLevel level : LogLevel.values()) {
			if (level.intValue() == lvl) {
				return level;
			}
		}
		return LogLevel.OFF;
	}
}
