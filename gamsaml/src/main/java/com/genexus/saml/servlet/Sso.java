package com.genexus.saml.servlet;

import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import com.genexus.saml.*;
import com.genexus.webpanels.HttpContextWeb;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;


public class Sso extends HttpServlet {
	public static final ILogger logger = LogManager.getLogger(Sso.class);
	private static final long serialVersionUID = 6515092992727846423L;
	private static final String SAMLPARAMETER = "SAMLResponse";
	private static String SESSION_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
	private String errorServlet;


	@Override
	public void init(ServletConfig config) throws ServletException {
		logger.debug("[init]1130");
		super.init(config);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException {

		String samlParameter = request.getParameter(SAMLPARAMETER);
		String stateParm = request.getParameter("RelayState");

		logger.debug("[doPost] state == " + stateParm);
		logger.debug("[doPost] samlParameter " + samlParameter);
		GamSamlProperties props = GamSamlProperties.getInstance();
		props.init(stateParm);
		if (stateParm != null && !stateParm.isEmpty()) {
			errorServlet = GamSamlProperties.getPathErrorServletSSO();
		}
		try {
			if (samlParameter == null) {
				logger.debug("[doPost] samlParameter == null");
				createSAMLAuthn(response);
				try {
					logYError(request, response);
				} catch (Exception e1) {
					logger.error("[doPost] ", e1);
				}

			} else {
				Assertion assertion = null;
				String status = "";
				SamlReceiver receiver = new SamlReceiver();
				try {
					assertion = receiver.getSAMLAssertion(samlParameter);
					logger.debug("[doPost] getInResponseTo(): " + receiver.getSAMLResponse(samlParameter).getInResponseTo());
					//get status
					status = receiver.getStatusAssertion(samlParameter);
				} catch (Exception e) {
					logger.error("[doPost] ", e);
				}
				if (status.equalsIgnoreCase(SESSION_SUCCESS)) {
					logger.debug("[doPost] status == " + SESSION_SUCCESS);
					SamlAssertion samlAssertion = receiver.getDataFromAssertion(assertion);

					if (samlAssertion != null) {
						logger.debug("[doPost] samlAssertion != null");

						login(request, receiver, response);
					} else {
						logger.debug("[doPost] samlAssertion == null");
						try {
							logYError(request, response);
						} catch (Exception e1) {
							logger.error("[doPost]", e1);
						}
					}
				} else {


					try {
						String parameters = "saml=auth&state=" + GamSamlProperties.getState().trim() + "&error_code=" + status + "&error_message=" + receiver.getStatusAssertionMessage(samlParameter);
						response.sendRedirect(request.getContextPath() + "/oauth/gam/callback?" + parameters);
					} catch (Exception e) {
						logger.error("[doPost] ", e);
					}

				}
			}
		} catch (Exception e) {
			logger.error("[doPost]" , e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException {

		if (request.getParameter(SAMLPARAMETER) == null) {
			logger.debug("[doGet] samlParameter == null");
			String stateParm = request.getParameter("state");
			logger.debug("[doGet] state == " + stateParm);
			GamSamlProperties props = GamSamlProperties.getInstance();
			props.init(stateParm);
			if (stateParm != null && !stateParm.isEmpty()) {
				errorServlet = GamSamlProperties.getPathErrorServletSSO();
			}
			createSAMLAuthn(response);
			logger.debug("[doGet] run");

		} else {
			try {
				logYError(request, response);
			} catch (Exception e2) {
				logger.error("[doGet]", e2);
			}
		}
	}


	private void logYError(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, IllegalStateException {

		try {
			RequestDispatcher rd = request.getRequestDispatcher(errorServlet);
			rd.forward(request, response);
		} catch (IOException e) {
			logger.error("[logYError] catch IOException ", e);
			throw new IOException(e);
		} catch (ServletException e) {
			logger.error("[logYError] catch ServletException ", e);
			throw new ServletException(e);
		} catch (IllegalStateException e) {
			logger.error("[logYError] catch IllegalStateException ", e);
			throw new IllegalStateException(e);
		}
	}

	private void login(HttpServletRequest request, SamlReceiver receiver, HttpServletResponse response) {
		logger.debug("[login]");
		String samlParameter = receiver.getSAMLString();
		Response SAMLResponse;
		Assertion Assertion;
		SamlAssertion SAMLAssertion;
		String tokenId = "";
		String tokenInResponseTo = "";
		NameID nameId = null;
		String fullAttributesJSON = "";
		String sessionIndex = "";
		try {
			SAMLResponse = receiver.getSAMLResponse(request.getParameter(SAMLPARAMETER));
			tokenId = SAMLResponse.getID();
			tokenInResponseTo = SAMLResponse.getInResponseTo();

			Assertion = receiver.getSAMLAssertion(samlParameter);
			SAMLAssertion = receiver.getDataFromAssertion(Assertion);
			fullAttributesJSON = SAMLAssertion.getFullAttributesJson();
			nameId = Assertion.getSubject().getNameID();
			logger.debug("[login] fullAttributesJSON: " + fullAttributesJSON);

			List<AuthnStatement> authnStatements = Assertion.getAuthnStatements();
			if (authnStatements != null && authnStatements.size() > 0) {
				//it has to be just one uthentication stmt inside SAML assertion of SAML Response
				AuthnStatement authStmt = authnStatements.get(0);
				sessionIndex = authStmt.getSessionIndex();
			}
		} catch (Exception e) {
			logger.error("[login] ", e);
		}

		String token = nameId.getFormat() + "," + nameId.getValue() + "::" + sessionIndex.trim();
		logger.debug("[login] Token: " + token);
		String parameters = "saml=auth&state=" + GamSamlProperties.getState().trim() + "&token=" + token;
		logger.debug("[login] Parameters: " + parameters);

		try {
			String gxcfg = getServletContext().getInitParameter("gxcfg");
			logger.debug("[login] Parameters 2: " + gxcfg);
			Class contextClass;
			String url = "";
			contextClass = Class.forName(gxcfg);
			com.genexus.servlet.http.IHttpServletRequest myhttpservReq = new com.genexus.servlet.http.HttpServletRequest(request);
			com.genexus.servlet.http.IHttpServletResponse myhttpservRes = new com.genexus.servlet.http.HttpServletResponse(response);
			com.genexus.servlet.IServletContext myhttpservCon = new com.genexus.servlet.ServletContext(getServletContext());
			HttpContext httpContext = new HttpContextWeb("POST", myhttpservReq, myhttpservRes, myhttpservCon);
			ModelContext context = new ModelContext(contextClass);
			context.setHttpContext(httpContext);
			Class<?> gamClass = Class.forName("genexus.security.api.gamexternalauthenticationinputsaml20");
			Object gamObj = gamClass.getDeclaredConstructor(int.class, ModelContext.class).newInstance(-1, context);
			Class<?>[] paramTypes = {String.class, String.class, String.class, String.class};
			Method method = gamClass.getMethod("executeUdp", paramTypes);

			if (GamSamlProperties.getState() == null || GamSamlProperties.getState().trim().isEmpty()) {
				logger.debug("[login]  EmptyState");
				String entityID = "EntityID=" + GamSamlProperties.getServiceProviderEntityId();
				url = (String) method.invoke(gamObj, "saml=auth", entityID, token, fullAttributesJSON);
				logger.debug("[login] url: ");
				response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
				response.setHeader("Location", url);
			} else {
				url = (String) method.invoke(gamObj, "saml=auth", GamSamlProperties.getState().trim(), token, fullAttributesJSON);
				logger.debug("[login] url: ");
				response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
				response.setHeader("Location", url);
			}

		} catch (Exception e) {
			logger.error("[login] Error exception: ", e);
		}
	}

	private void createSAMLAuthn(HttpServletResponse response) {
		logger.debug("[createSAMLAuthn]");

		try {

			SamlHelper helper = new SamlHelper();
			helper.doAuthenticationRedirect(response);
		} catch (Exception e) {
			logger.error("[createSAMLAuthn]", e);
		}
	}
}