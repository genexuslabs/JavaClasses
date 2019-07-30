package com.genexus.webpanels.gridstate ;
import com.genexus.*;
import com.genexus.internet.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.webpanels.GXWebObjectBase;
import com.genexus.webpanels.WebSession;

public final class GXGridStateHandler {
	public static final ILogger logger = LogManager.getLogger(SdtGridState.class);
	private String gridName;
	private Runnable varsFromState;
	private Runnable varsToState;
	private String varsFromStateMethod;
	private String varsToStateMethod;
	private Object parent;
	private ModelContext context;
	private SdtGridState state;

	private GXGridStateHandler(ModelContext context, String gridName, String programName) {
		this.context = context;
		this.gridName = programName + "_" + gridName + "_GridState";
		state = new SdtGridState(context);
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
		return state.getgxTv_SdtGridState_Inputvalues().elementAt(idx - 1).getgxTv_SdtGridState_InputValuesItem_Value();
	}

	public void clearFilterValues() {
		state.getgxTv_SdtGridState_Inputvalues().clear();
	}

	public void addFilterValue(String name, String value) {
		SdtGridState_InputValuesItem GridStateFilterValue = new SdtGridState_InputValuesItem(context);
		GridStateFilterValue.setgxTv_SdtGridState_InputValuesItem_Name(name);
		GridStateFilterValue.setgxTv_SdtGridState_InputValuesItem_Value(value);
		state.getgxTv_SdtGridState_Inputvalues().add(GridStateFilterValue, 0);
	}

	public void saveGridState() {
		WebSession session = ((HttpContext) context.getHttpContext()).getWebSession();
		state.fromJSonString(session.getValue(gridName));
		runVarsToState();
		session.setValue(gridName, state.toJSonString());
	}

	public void loadGridState() {
		HttpContext httpContext = (HttpContext) context.getHttpContext();
		HttpRequest httpRequest = httpContext.getHttpRequest();
		WebSession session = httpContext.getWebSession();
		if (GXutil.strcmp(httpRequest.getMethod(), "GET") == 0) {
			state.fromJSonString(session.getValue(gridName));
			runVarsFromState();
		}
	}

	public int getFiltercount() {
		return state.getgxTv_SdtGridState_Inputvalues().size();
	}

	public int getCurrentpage() {
		return state.getgxTv_SdtGridState_Currentpage();
	}

	public void setCurrentpage(int value) {
		state.setgxTv_SdtGridState_Currentpage(value);
	}

	public SdtGridState getState() {
		return this.state;
	}

	public void setState(SdtGridState state) {
		this.state = state;
	}
}

