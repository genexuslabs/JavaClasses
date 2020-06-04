package com.genexus.webpanels.gridstate ;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.internet.HttpRequest;
import com.genexus.webpanels.GXWebObjectBase;
import com.genexus.webpanels.WebSession;
import com.genexus.xml.GXXMLSerializable;

import java.io.IOException;
import java.lang.reflect.Constructor;

public final class GXGridStateHandler {
	public static final ILogger logger = LogManager.getLogger(GXGridStateHandler.class);
	private String gridName;
	private Runnable varsFromState;
	private Runnable varsToState;
	private String varsFromStateMethod;
	private String varsToStateMethod;
	private Object parent;
	private ModelContext context;
	private GridState state;
	private GXXMLSerializable exposedSdtGridState;
	private boolean dirty;
	private ObjectMapper objectMapper;

	static final String SDTGridStateClass = "com.genexuscore.genexus.common.SdtGridState";

	private GXGridStateHandler(ModelContext context, String gridName, String programName) {
		this.context = context;
		this.gridName = programName + "_" + gridName + "_GridState";
		objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		dirty =true;
	}

	public GXGridStateHandler(ModelContext context, String gridName, String programName, Runnable varsFromState, Runnable varsToState) {
		this(context, gridName, programName);
		this.varsFromState = varsFromState;
		this.varsToState = varsToState;
		this.state = new GridState();
	}

	//Cosntructor por java <= 1.7
	public GXGridStateHandler(ModelContext context, String gridName, String programName, GXWebObjectBase parent, String varsFromStateMethod, String varsToStateMethod) {
		this(context, gridName, programName);
		this.varsFromStateMethod = varsFromStateMethod;
		this.varsToStateMethod = varsToStateMethod;
		this.parent = parent;
		this.state = new GridState();
	}

	public void saveGridState() {
		WebSession session = ((HttpContext) context.getHttpContext()).getWebSession();
		stateFromJson(session.getValue(gridName));
		runVarsToState();
		session.setValue(gridName, stateToJson());
		dirty = true;
	}

	public void loadGridState() {
		HttpContext httpContext = (HttpContext) context.getHttpContext();
		HttpRequest httpRequest = httpContext.getHttpRequest();
		if (GXutil.strcmp(httpRequest.getMethod(), "GET") == 0) {
			WebSession session = httpContext.getWebSession();
			stateFromJson(session.getValue(gridName));
			runVarsFromState();
			dirty = true;
		}
	}

	private String stateToJson() {
		try {
			return objectMapper.writeValueAsString(state);
		} catch (IOException ex) {
			logger.error("stateToJson error", ex);
			return null;
		}
	}

	private void stateFromJson(String json) {
		try {
			state = objectMapper.readValue(json, GridState.class);
		} catch (IOException ex) {
			logger.error("stateFromJson error", ex);
		}
	}

	public String filterValues(int idx) {
		return state.InputValues.get(idx - 1).Value;
	}

	public int getCurrentpage() {
		return state.CurrentPage;
	}

	public void setCurrentpage(int value) {
		state.CurrentPage = value;
	}

	public short getOrderedby() {
		return state.OrderedBy;
	}

	public void setOrderedby(short value) {
		state.OrderedBy = value;
	}

	public GXXMLSerializable getState() {
		try {
			if (dirty || exposedSdtGridState == null) {
				Class<?> sdtGridStateClass = Class.forName(SDTGridStateClass);
				Class[] parTypes = new Class[]{ModelContext.class};
				Constructor ctr = sdtGridStateClass.getConstructor(parTypes);
				Object[] argList = new Object[]{context};
				HttpContext httpContext = (HttpContext) context.getHttpContext();
				WebSession session = httpContext.getWebSession();
				exposedSdtGridState = (GXXMLSerializable) ctr.newInstance(argList);
				exposedSdtGridState.fromJSonString(session.getValue(gridName));
				dirty = false;
			}
			return exposedSdtGridState;
		} catch (Exception ex) {
			logger.error("Can't create " + SDTGridStateClass, ex);
			return null;
		}
	}

	public void setState(GXXMLSerializable state) {
		this.exposedSdtGridState = state;
		String jsonState = exposedSdtGridState.toJSonString();
		stateFromJson(jsonState);
		HttpContext httpContext = (HttpContext) context.getHttpContext();
		WebSession session = httpContext.getWebSession();
		session.setValue(gridName, jsonState);
	}

	public void clearFilterValues() {
		state.InputValues.clear();
	}

	public void addFilterValue(String name, String value) {
		GridStateInputValuesItem item = new GridStateInputValuesItem();
		item.Value = value;
		item.Name = name;
		state.InputValues.add(item);
	}

	public int getFiltercount() {
		return state.InputValues.size();
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

