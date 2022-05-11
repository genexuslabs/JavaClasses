package com.genexus.diagnostics.core.provider;

import java.util.Arrays;

import com.genexus.AndroidLog;
import com.genexus.diagnostics.core.ILogger;

public class AndroidLogger implements ILogger {

	public AndroidLogger() {
		
	}
	
	@Override
	public void error(String msg, Throwable ex) {
		
		AndroidLog.error(msg);
	}

	@Override
	public void error(String msg1, String msg2, Throwable ex) {
		AndroidLog.error(msg1);
	}

	@Override
	public void error(Throwable ex, String[] list) {
		AndroidLog.error(ex.getMessage());
	}

	@Override
	public void error(String[] list) {
		AndroidLog.error(Arrays.toString(list));
	}

	@Override
	public void error(String msg) {
		AndroidLog.error(msg);
	}

	@Override
	public void warn(String msg) {
		AndroidLog.warning(msg);
		
	}

	@Override
	public void warn(Throwable ex, String[] list) {
		AndroidLog.warning(ex.getMessage());
		
	}

	@Override
	public void warn(String[] list) {
		AndroidLog.warning(Arrays.toString(list));
		
	}

	@Override
	public void warn(String msg, Throwable ex) {
		AndroidLog.warning(msg);
		
	}

	@Override
	public void debug(String msg) {
		AndroidLog.debug(msg);
		
	}

	@Override
	public void debug(Throwable ex, String[] list) {
		AndroidLog.debug(ex.getMessage());
		
	}

	@Override
	public void debug(String[] list) {
		AndroidLog.debug(Arrays.toString(list));
		
	}

	@Override
	public void debug(String msg1, String msg2, Throwable ex) {
		AndroidLog.debug(msg1);
		
	}

	@Override
	public void debug(String msg, Throwable ex) {
		AndroidLog.debug(msg);
		
	}

	@Override
	public void info(String[] list) {
		AndroidLog.info(Arrays.toString(list));
		
	}

	@Override
	public void info(String msg) {
		AndroidLog.info(msg);
		
	}
	
		@Override
	public void fatal(String msg, Throwable ex) {
		AndroidLog.error(msg);		
	}

	@Override
	public void fatal(String msg1, String msg2, Throwable ex) {
		AndroidLog.error(msg1);
		
	}

	@Override
	public void fatal(Throwable ex, String[] list) {
		AndroidLog.error(Arrays.toString(list));		
	}

	@Override
	public void fatal(String[] list) {
		AndroidLog.error(Arrays.toString(list));		
	}

	@Override
	public void fatal(String msg) {
		AndroidLog.error(msg);
		
	}
	
	@Override
	public void trace(String msg) {
		AndroidLog.debug(msg);
		
	}

	@Override
	public void trace(Throwable ex, String[] list) {
		AndroidLog.debug(Arrays.toString(list));	
		
	}

	@Override
	public void trace(String[] list) {
		AndroidLog.debug(Arrays.toString(list));	
		
	}

	@Override
	public void trace(String msg1, String msg2, Throwable ex) {
		AndroidLog.debug(msg1);	
		
	}

	@Override
	public void trace(String msg, Throwable ex) {
		AndroidLog.debug(msg);	
	}

	public boolean isDebugEnabled() {
		return true;
	}

	boolean isErrorEnabled()  {
		return true;
	};
}
