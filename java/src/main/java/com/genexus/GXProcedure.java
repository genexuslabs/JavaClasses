
package com.genexus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.GXDebugInfo;
import com.genexus.diagnostics.GXDebugManager;
import com.genexus.internet.HttpClient;
import com.genexus.internet.HttpContext;
import com.genexus.mock.GXMockProvider;
import com.genexus.performance.ProcedureInfo;
import com.genexus.performance.ProceduresInfo;
import com.genexus.util.*;
import com.genexus.util.saia.OpenAIRequest;
import com.genexus.util.saia.OpenAIResponse;
import com.genexus.util.saia.SaiaService;
import org.json.JSONObject;

public abstract class GXProcedure implements IErrorHandler, ISubmitteable {
	public abstract void initialize();

	protected transient int remoteHandle;
	public transient ModelContext context;
	protected transient LocalUtil localUtil;
	protected transient String location = "";
	protected com.genexus.internet.HttpContext httpContext;
	protected boolean isRemote;
	protected boolean disconnectUserAtCleanup = false;
	protected boolean ejbMessageCall = false;
    UserInformation ui=null;
	
	private Date beginExecute; 
	private HttpClient client;

	public static final int IN_NEW_UTL = -2;

	public GXProcedure(int remoteHandle, ModelContext context, String location) {
		this(false, remoteHandle, context, location);
	}

	public GXProcedure(boolean inNewUTL, int remoteHandle, ModelContext context, String location) {
		//JMX Counter
		if (Application.isJMXEnabled()) {
			beginExecute = new Date();
			ProcedureInfo pInfo = ProceduresInfo.addProcedureInfo(this.getClass().getName());
			pInfo.incCount();
		}

		this.remoteHandle = remoteHandle;
		this.context	  = context;
		this.location	  = location;
		int parentHandle = remoteHandle;
		
		if(inNewUTL) {
			remoteHandle = IN_NEW_UTL;
		}
		if (context != null && context.getSessionContext() != null) {
			ApplicationContext.getInstance().setEJB(true);
			ApplicationContext.getInstance().setPoolConnections(true);
		}

		switch(remoteHandle) {
			case -1:
				ui = Application.getConnectionManager().createUserInformation(Namespace.getNamespace(context.getNAME_SPACE()));
				this.remoteHandle = ui.getHandle();
				ApplicationContext.getInstance().setMsgsToUI(false);
				
				// I have to set the 'autoDisconnect' to true, because it indicates
				// that I have to 'keep alive' the connection in case that no requests
				// are made, because the only requests that are accounted for this are
				// the 'create object', but not the methods in the objects, so if the
				// procedure spends a lot of time in processing without creating objects
				// it could be disconnected.
				ui.setAutoDisconnect(true);
				break;
			case IN_NEW_UTL:
				if(context.prefs == null && ApplicationContext.getInstance().isApplicationServer()) {
					if(parentHandle == IN_NEW_UTL) {
						context.prefs = ClientPreferences.getInstance(context.packageClass);
					}
					else {
						context.prefs = ServerPreferences.getInstance(context.packageClass);
						Preferences specificPrefs = (Preferences) context.getPreferences();
						specificPrefs.iniFile.setProperty(specificPrefs.defaultSection, "NAME_SPACE", Application.getConnectionManager().getUserInformation(parentHandle).getNamespace().getName());
					}
				}
				if (ApplicationContext.getInstance().isApplicationServer()) {
					if(context.prefs.getProperty("NAME_SPACE", "").equals("")) {
						Preferences specificPrefs = (Preferences) context.getPreferences();
						specificPrefs.iniFile.setProperty(specificPrefs.defaultSection, "NAME_SPACE", Application.getConnectionManager().getUserInformation(parentHandle).getNamespace().getName());
					}
				}
				ui = Application.getConnectionManager().createUserInformation(Namespace.getNamespace(context.getNAME_SPACE()));
				this.remoteHandle = ui.getHandle();
				disconnectUserAtCleanup = true;
				break;
			default:
				ui = Application.getConnectionManager().getUserInformation(remoteHandle);
		}

		localUtil    	  = ui.getLocalUtil();
		if (context != null) {
			httpContext = (HttpContext) context.getHttpContext();
			httpContext.initClientId();
		}
	}

	public Object me() {
		return this;
	}
	public void handleError() {
		new DefaultErrorHandler().handleError(context, remoteHandle);
	}

	public int getHandle() {
		return remoteHandle;
	}

	public ModelContext getContext() {
		return context;
	}

	protected boolean isRemoteProcedure() {
		return Application.isRemoteProcedure(context, remoteHandle, location);
	}
	protected boolean batchCursorHolder(){
		return false;
	}
	protected void exitApp() {
		exitApplication(batchCursorHolder());
	}
	/**
	 * @deprecated use exitApp()
	 * */
	protected void exitApplication() {
		exitApplication(true);
	}
	private void exitApplication(boolean flushBuffers) {
		if(dbgInfo != null && Application.realMainProgram == this)
			dbgInfo.onExit();

		if (flushBuffers) {
			try {
				Application.getConnectionManager().flushBuffers(remoteHandle, this);
			} catch (Exception exception) { ; }
		}
		if(disconnectUserAtCleanup) {
			try {
				Application.getConnectionManager().disconnect(remoteHandle);
			} catch(Exception disconnectException){ ; }
		}
		Application.cleanup(context, this, remoteHandle);
	}

	public void endExecute(String name) {
		if (Application.isJMXEnabled()) {
			ProcedureInfo pInfo = ProceduresInfo.getProcedureInfo(name);
			pInfo.setTimeExecute(System.currentTimeMillis() - beginExecute.getTime());
		}
		
		if (context != null && context.getSessionContext() != null) {
			ApplicationContext.getInstance().setEJB(false);
			ApplicationContext.getInstance().setPoolConnections(false);
		}
	}
	
	public void release() {
	}

	protected String formatLink(String jumpURL) {
		return formatLink(jumpURL, new String[]{}, new String[]{});
	}

	protected String formatLink(String jumpURL, String[] parms, String[] parmsName) {
		String contextPath = (httpContext.getRequest() == null)? "" : httpContext.getRequest().getContextPath();
		return URLRouter.getURLRoute(jumpURL, parms, parmsName, contextPath, context.getPackageName());
	}
	
	public void callSubmit(final int id, Object [] submitParms) {
		SubmitThreadPool.submit(this, id, submitParms, context.submitCopy());
	}
	
	public void callSubmit(String blockName, String message, final int id, Object [] submitParms) {
		ReorgSubmitThreadPool.submitReorg(blockName, message, this, id);
	}	
	
	/** This method is overridden by subclass in the generated code
	 */
	public void submit(int id, Object [] submitParms, ModelContext ctx){  }
	public void submit(int id, Object [] submitParms){  }
	public void submitReorg(int id, Object [] submitParms) throws SQLException{  }
	
	
	public void setejbMessageCall() {
		ejbMessageCall = true;
	}
	
	public void SetCreateDataBase() {
		GXReorganization.setCreateDataBase();
	}

    public int setLanguage(String language) {
		int res = GXutil.setLanguage(language, context, ui);
		this.localUtil = ui.getLocalUtil();
		return res;
	}

	protected void callWebObject(String url) {
		httpContext.wjLoc = url;
	}

	protected void cleanup() {
	}

	public void handleException(String gxExceptionType, String gxExceptionDetails, String gxExceptionStack) {
	}

	private GXDebugInfo dbgInfo = null;
	protected void trkCleanup() {
		if(dbgInfo != null)
			dbgInfo.onCleanup();
	}

	protected void initialize(int objClass, int objId, int dbgLines, long hash) {
		dbgInfo = GXDebugManager.getInstance().getDbgInfo(context, objClass, objId, dbgLines, hash);
	}

	protected void trk(int lineNro) {
		if(dbgInfo != null)
			dbgInfo.trk(lineNro);
	}

	protected void trk(int lineNro, int lineNro2) {
		if(dbgInfo != null)
			dbgInfo.trk(lineNro, lineNro2);
	}

	protected void trkrng(int lineNro, int lineNro2) {
		trkrng(lineNro, 0, lineNro2, 0);
	}

	protected void trkrng(int lineNro, int colNro, int lineNro2, int colNro2) {
		if(dbgInfo != null)
			dbgInfo.trkRng(lineNro, colNro, lineNro2, colNro2);
	}

	protected void privateExecute() {
	}

	protected String[] getParametersInternalNames( ) {
		return null ;
	}

	protected void mockExecute() {
		if (GXMockProvider.getProvier() != null) {
			if (GXMockProvider.getProvier().handle(remoteHandle, context, this, getParametersInternalNames())) {
				cleanup();
				return;
			}
		}
		privateExecute( );
	}

	protected String callTool(String name, String arguments) {
		return "";
	}

	protected String callAssistant(String agent, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result) {
		return callAgent(agent, properties, messages, result);
	}

	protected ChatResult chatAgent(String agent, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result) {
		callAgent(agent, true, properties, messages, result);
		return new ChatResult(this, agent, properties, messages, result, client);
	}

	protected String callAgent(String agent, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result) {
		return callAgent(agent, false, properties, messages, result);
	}

	protected String callAgent(String agent, boolean stream, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result) {
		OpenAIRequest aiRequest = new OpenAIRequest();
		aiRequest.setModel(String.format("saia:agent:%s", agent));
		if (!messages.isEmpty())
			aiRequest.setMessages(messages);
		aiRequest.setVariables(properties.getList());
		if (stream)
			aiRequest.setStream(true);
		client = new HttpClient();
		OpenAIResponse aiResponse = SaiaService.call(aiRequest, client, result);
		if (aiResponse != null && aiResponse.getChoices() != null) {
			for (OpenAIResponse.Choice element : aiResponse.getChoices()) {
				String finishReason = element.getFinishReason();
				if (finishReason.equals("stop"))
					return element.getMessage().getStringContent();
				if (finishReason.equals("tool_calls")) {
					messages.add(element.getMessage());
					return processNotChunkedResponse(agent, stream, properties, messages, result, element.getMessage().getToolCalls());
				}
			}
		} else if (client.getStatusCode() == 200) {
			return "";
		}
		return "";
	}

	public String processNotChunkedResponse(String agent, boolean stream, GXProperties properties, ArrayList<OpenAIResponse.Message> messages, CallResult result, ArrayList<OpenAIResponse.ToolCall> toolCalls) {
		for (OpenAIResponse.ToolCall tollCall : toolCalls) {
			processToolCall(tollCall, messages);
		}
		return callAgent(agent, stream, properties, messages, result);
	}

	private void processToolCall(OpenAIResponse.ToolCall toolCall, ArrayList<OpenAIResponse.Message> messages) {
		String result;
		String functionName = toolCall.getFunction().getName();
		try {
			result = callTool(functionName, toolCall.getFunction().getArguments());
		}
		catch (Throwable e) {
			result = String.format("Error calling tool %s", functionName);
		}
		OpenAIResponse.Message toolCallMessage = new OpenAIResponse.Message();
		toolCallMessage.setRole("tool");
		toolCallMessage.setStringContent(result);
		toolCallMessage.setToolCallId(toolCall.getId());
		messages.add(toolCallMessage);
	}
}
