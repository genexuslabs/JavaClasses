package com.genexus.webpanels.gridstate ;
import com.genexus.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.webpanels.GXWebObjectBase;
import com.genexus.xml.GXXMLSerializable;

public final class GXGridStateHandler {
	public static final ILogger logger = LogManager.getLogger(GXGridStateHandler.class);
	private String gridName;
	private Runnable varsFromState;
	private Runnable varsToState;
	private String varsFromStateMethod;
	private String varsToStateMethod;
	private Object parent;
	private ModelContext context;
	private GXXMLSerializable state;

	private GXGridStateHandler(ModelContext context, String gridName, String programName) {
		this.context = context;
		this.gridName = programName + "_" + gridName + "_GridState";
	}

	public GXGridStateHandler(ModelContext context, String gridName, String programName, Runnable varsFromState, Runnable varsToState) {
		this(context, gridName, programName);
		this.varsFromState = varsFromState;
		this.varsToState = varsToState;
	}

	//Cosntructor por java <= 1.7
	public GXGridStateHandler(ModelContext context, String gridName, String programName, GXWebObjectBase parent, String varsFromStateMethod, String varsToStateMethod) {
		this(context, gridName, programName);
		this.varsFromStateMethod = varsFromStateMethod;
		this.varsToStateMethod = varsToStateMethod;
		this.parent = parent;
	}

	private void runVarsToState() {
		if (this.varsToState != null)
			varsToState.run();
		else {
			try {
				parent.getClass().getMethod(varsToStateMethod).invoke(parent, (Object[]) null);
			} catch (Exception ex) {
				logger.error("Error invoking method " + varsToStateMethod + " for grid state " + gridName, ex);
			}
		}
	}

	private void runVarsFromState() {
		if (this.varsFromState != null)
			varsFromState.run();
		else {
			try {
				parent.getClass().getMethod(varsFromStateMethod).invoke(parent, (Object[]) null);
			} catch (Exception ex) {
				logger.error("Error invoking method " + varsFromStateMethod + " for grid state " + gridName, ex);
			}
		}
	}

	public String filterValues(int idx) {
		return "";
	}

	public void clearFilterValues() {

	}

	public void addFilterValue(String name, String value) {
	}

	public void saveGridState() {
	}

	public void loadGridState() {
	}

	public int getFiltercount() {
		return 0;
	}

	public int getCurrentpage() {
		return 0;
	}

	public void setCurrentpage(int value) {

	}

	public GXXMLSerializable getState() {
		return this.state;
	}

	public void setState(GXXMLSerializable state) {
		this.state = state;
	}
}

