package com.genexus.saml.servlet;

import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.saml.GamSamlProperties;
import com.genexus.saml.SamlHelper;
import com.genexus.saml.SamlReceiver;
import com.genexus.webpanels.HttpContextWeb;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.NameID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;


public class Logout extends HttpServlet {
	public static final ILogger logger = LogManager.getLogger(Logout.class);

	private static final long serialVersionUID = -4745341452085760212L;
	private String errorServlet;
	private static final String SAMLPARAMETER = GamSamlProperties.getSAMLPARAMETER();

	@Override
	public void init(ServletConfig config) throws ServletException {
		logger.debug("[init]");
		super.init(config);
		errorServlet = GamSamlProperties.getPathErrorServletSSO();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException {

		String samlParameter = request.getParameter(SAMLPARAMETER);
		String alg = request.getParameter("SigAlg");

		String stateParm = request.getParameter("RelayState");
		logger.debug("[doPost] RelayState == " + stateParm);
		GamSamlProperties props = GamSamlProperties.getInstance();
		props.init(stateParm);
		logger.debug("[doPost] alg =" + alg);

		String signature = request.getParameter("Signature");
		String logoutRequest = request.getParameter("SAMLRequest");

		HttpSession session = request.getSession();

		try {
			if (samlParameter == null && logoutRequest == null){
				logger.debug("[doPost] samlParameter == null");
				try {
					createSamlLogout(request, response);
				} catch (Exception e) {
					logger.error("[doPost] ", e);
					try {
						logYError(request,response);
					} catch (Exception e1) {
						logger.error("[doPost] ", e1);

					}
				}
			} else
			{
				logger.debug("[LogoutResponse]");
				logger.debug("[samlParameter]" + samlParameter);
				//si it is a LogoutResponse
				if (samlParameter != null){
					SamlReceiver receiver = new SamlReceiver();
					//get status
					LogoutResponse resp = receiver.getLogoutResponse(samlParameter,signature,alg);
					String identifier = (String) session.getAttribute("identifier");
					if(resp != null)
					{
						String status = resp.getStatus().getStatusCode().getValue();
						logger.debug("[status]" + status);
						if(status.equalsIgnoreCase("urn:oasis:names:tc:SAML:2.0:status:Success") || status.equalsIgnoreCase("urn:oasis:names:tc:SAML:2.0:status:PartialLogout")){

							//verifies the validity of the LogoutResponse
							if (!identifier.equals("") && validateLogoutResponse(resp,identifier)) {
								logout(request,response);
							} else {
								try {
									logYError(request,response);
								} catch (Exception e1) {
									logger.error("[doPost] ", e1);
								}
							}
						}
						else
						{
							String error = "&error_code="+ status + "&error_message"+ resp.getStatus().getStatusMessage().getMessage();
							logger.error("Logout Error" + error + "  ----  " + resp.getStatus().getStatusDetail());
						}
					}
					else
					{
						logger.error("Logout Error  ----  Response is null");
					}

				}
				//if it is a LogoutRequest
				if (logoutRequest != null){
					//verifies the signature
					SamlReceiver receiver = new SamlReceiver();
					LogoutRequest req = receiver.getLogoutRequest(logoutRequest);

					if (alg == null)
					{
						logger.debug("[getLogoutResponse] alg is empty");
						alg = req.getSignature().getSignatureAlgorithm();
						logger.debug("[getLogoutResponse] Get alg " + alg);
					}

					String sessionIndex = req.getSessionIndexes().get(0).getSessionIndex();
					NameID nameId = req.getNameID();
					String externalToken = nameId.getFormat() + "," + nameId.getValue()+"::" + sessionIndex;;
					stateParm = "Token="+ externalToken;
					props.update(stateParm);

					boolean isValid = receiver.validateSignature(req);

					if (isValid){
						logger.debug("[doPost] LogoutRequest with valid signature");
						SamlHelper helper = new SamlHelper();
						String gxcfg = getServletContext().getInitParameter("gxcfg");
						Class contextClass = Class.forName(gxcfg);
						com.genexus.servlet.http.IHttpServletRequest myhttpservReq  = new com.genexus.servlet.http.HttpServletRequest(request);
						com.genexus.servlet.http.IHttpServletResponse myhttpservRes  = new com.genexus.servlet.http.HttpServletResponse(response);
						com.genexus.servlet.IServletContext myhttpservCon  = new com.genexus.servlet.ServletContext(getServletContext());
						HttpContext httpContext = new HttpContextWeb("POST", myhttpservReq, myhttpservRes, myhttpservCon );
						helper.doLogoutLocalRedirect(req, response, httpContext, contextClass);
					}
					else
						logger.debug("[doPost] NOT valid signature");
				}
			}
		}catch (Exception e) {
			logger.error("[doPost] ", e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException{
		doPost(request,response);
	}


	private void logYError(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException {

		try {
			RequestDispatcher rd = request.getRequestDispatcher(errorServlet);
			rd.forward(request, response);
		} catch (Exception e) {
			logger.error("[logYError] ", e);
		}
	}

	private void logout(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, SecurityException {
		logger.debug("[logout] 200 OK");
		//send 200 ok
		String gxcfg = getServletContext().getInitParameter("gxcfg");
		logger.debug("[GamSamlProperties.getState().trim()] :" + GamSamlProperties.getState().trim());
		String token = "";
		String fullAttributesJSON = "";
		try {

			String url = "";
			Class contextClass = Class.forName(gxcfg);
			com.genexus.servlet.http.IHttpServletRequest myhttpservReq  = new com.genexus.servlet.http.HttpServletRequest(request);
			com.genexus.servlet.http.IHttpServletResponse myhttpservRes  = new com.genexus.servlet.http.HttpServletResponse(response);
			com.genexus.servlet.IServletContext myhttpservCon  = new com.genexus.servlet.ServletContext(getServletContext());
			HttpContext httpContext = new HttpContextWeb("POST", myhttpservReq, myhttpservRes, myhttpservCon );
			ModelContext context = new ModelContext(contextClass);
			context.setHttpContext(httpContext);
			try {
				Class<?> gamClass = Class.forName("genexus.security.api.gamexternalauthenticationinputsaml20");
				Object gamObj = gamClass.getDeclaredConstructor(int.class, ModelContext.class).newInstance(-1, context);
				Class<?>[] paramTypes = {String.class, String.class, String.class, String.class};
				Method method = gamClass.getMethod("executeUdp", paramTypes);
				url = (String) method.invoke("saml=signout",GamSamlProperties.getState().trim(), token, fullAttributesJSON);
			} catch (Exception e) {
				logger.error("[logout] load class with reflection ", e);
			}
			logger.debug("[SSO: logout] url: " + url);
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			response.setHeader("Location", url);
		} catch (Exception e) {
			logger.error("[logout] :", e);
		}
	}

	private void createSamlLogout(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, SecurityException {
		logger.debug("[createSamlLogout]");
		try{
			HttpSession session = request.getSession();
			String sessionIndex = request.getParameter("index");
			String fullNameIdentifier = request.getParameter("nameid");
			String[] splitedNameID = fullNameIdentifier.split(",");
			String nameIdentifier = splitedNameID[1].replace("%7c", "|");
			logger.debug("[index]" + sessionIndex);
			SamlHelper helper = new SamlHelper();
			String identifier = helper.createIdentifier();
			session.setAttribute("identifier", identifier);
			helper.doLogoutGlobalRedirect(response,sessionIndex,identifier, nameIdentifier);
		}
		catch(Exception e){
			logger.error("[createSamlLogout] ", e);
		}
	}

	private boolean validateLogoutResponse(LogoutResponse resp, String identifier) {
		SamlHelper helper = new SamlHelper();
		return helper.validateLogoutResponse(resp,identifier);
	}

}

