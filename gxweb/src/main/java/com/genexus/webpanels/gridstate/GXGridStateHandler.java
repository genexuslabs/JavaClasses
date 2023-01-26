package com.genexus.webpanels.gridstate ;
import com.genexus.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpRequest;
import com.genexus.webpanels.WebSession;
import com.genexuscore.genexus.common.*;

import java.util.Enumeration;

public final class GXGridStateHandler {
	public static final ILogger logger = LogManager.getLogger(GXGridStateHandler.class);
	private String gridName;
	private Runnable varsFromState;
	private Runnable varsToState;
	private String varsFromStateMethod;
	private String varsToStateMethod;
	private Object parent;
	private ModelContext context;
	private SdtGridState state;
	private boolean dirty;

	static final String sdtGridStateClass = "com.genexuscore.genexus.common.SdtGridState";

	private GXGridStateHandler(ModelContext context, String gridName, String programName) {
		this.context = context;
		this.gridName = programName + "_" + gridName + "_GridState";
		dirty =true;
		this.state = new SdtGridState(context);
	}

	public GXGridStateHandler(ModelContext context, String gridName, String programName, Runnable varsFromState, Runnable varsToState) {
		this(context, gridName, programName);
		this.varsFromState = varsFromState;
		this.varsToState = varsToState;
	}

	//Cosntructor por java <= 1.7
	public GXGridStateHandler(ModelContext context, String gridName, String programName, Object parent, String varsFromStateMethod, String varsToStateMethod) {
		this(context, gridName, programName);
		this.varsFromStateMethod = varsFromStateMethod;
		this.varsToStateMethod = varsToStateMethod;
		this.parent = parent;
		this.state = new SdtGridState(context);
	}

	public void saveGridState() {
		WebSession session = ((HttpContext) context.getHttpContext()).getWebSession();
		state.fromJSonString(session.getValue(gridName));
		runVarsToState();
		session.setValue(gridName, state.toJSonString());
		dirty = true;
	}

	public void loadGridState() {
		HttpContext httpContext = (HttpContext) context.getHttpContext();
		HttpRequest httpRequest = httpContext.getHttpRequest();
		if (GXutil.strcmp(httpRequest.getMethod(), "GET") == 0) {
			WebSession session = httpContext.getWebSession();
			state = new SdtGridState(context);
			state.fromJSonString(session.getValue(gridName));
			runVarsFromState();
			dirty = true;
		}
	}


	public String filterValues(int idx) {
		return state.getgxTv_SdtGridState_Inputvalues().get(idx-1).getgxTv_SdtGridState_InputValuesItem_Value();
	}

	public String filterValues(String filter) {
		int idx = containsName(filter);
		if (idx>0)
			return filterValues(idx);
		else
			return "";
	}

	private int containsName(String filter) {
		int idx=1;
		for (Enumeration<SdtGridState_InputValuesItem> values = state.getgxTv_SdtGridState_Inputvalues().elements(); values.hasMoreElements();) {
			SdtGridState_InputValuesItem value = values.nextElement();
			if (value.getgxTv_SdtGridState_InputValuesItem_Name().equalsIgnoreCase(filter))
				return idx;
			idx++;
		}
		return -1;
	}

	public int getCurrentpage() {
		return state.getgxTv_SdtGridState_Currentpage();
	}

	public void setCurrentpage(int value) {
		state.setgxTv_SdtGridState_Currentpage(value);
	}

	public short getOrderedby() {
		return state.getgxTv_SdtGridState_Orderedby();
	}

	public void setOrderedby(short value) {
		state.setgxTv_SdtGridState_Orderedby(value);
	}

	public SdtGridState getState() {
		try {
			if (dirty || state == null) {
				HttpContext httpContext = (HttpContext) context.getHttpContext();
				WebSession session = httpContext.getWebSession();
				state = new SdtGridState(context);
				state.fromJSonString(session.getValue(gridName));
				dirty = false;
			}
			return state;
		} catch (Exception ex) {
			logger.error("Can't create " + sdtGridStateClass, ex);
			return null;
		}
	}

	public void setState(SdtGridState state) {
		this.state = state;
		String jsonState = state.toJSonString();
		HttpContext httpContext = (HttpContext) context.getHttpContext();
		WebSession session = httpContext.getWebSession();
		session.setValue(gridName, jsonState);
	}

	public void clearFilterValues() {
		state.getgxTv_SdtGridState_Inputvalues().clear();
	}

	public void addFilterValue(String name, String value) {
		int idx = containsName(name);
		if (idx > 0)
			state.getgxTv_SdtGridState_Inputvalues().get(idx-1).setgxTv_SdtGridState_InputValuesItem_Value(value);
		else {
			SdtGridState_InputValuesItem item = new SdtGridState_InputValuesItem(context);
			item.setgxTv_SdtGridState_InputValuesItem_Value(value);
			item.setgxTv_SdtGridState_InputValuesItem_Name(name);
			state.getgxTv_SdtGridState_Inputvalues().add(item);
		}
	}

	public int getFiltercount() {
		return state.getgxTv_SdtGridState_Inputvalues().getItemCount();
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
}

