package com.genexus.saml;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Properties;


public class GamSamlProperties {
	public static final ILogger logger = LogManager.getLogger(GamSamlProperties.class);
	private static GamSamlProperties instance = null;
	private Properties generalPropsProperties = null;
	private Properties attPropsProperties = null;
	private static String state = "";

	private GamSamlProperties() {
	}

	public static GamSamlProperties getInstance() {
		if (instance == null) {
			instance = new GamSamlProperties();
		}
		return instance;
	}

	public void init(String instate) {
		logger.debug("[init] - state = " + instate);
		generalPropsProperties = null;
		if (instate == null || instate.isEmpty()) {
			logger.debug("[init] - state state == null or empty");
			state = "";
			logger.debug("[init] sale if ");

		} else {
			state = instate;
			logger.debug("[init] - state != null");
			// if state is there inicialize from GAM
			try {
				generalPropsProperties = loadPropsFromGAM(instate);
			} catch (Exception e) {
				logger.error("[init] ", e);
			}
		}
	}

	public void updatePropsFromGAM(String state) {
		logger.debug("[init] - updatePropsFromGAM = " + state);
		generalPropsProperties = null;
		try {
			generalPropsProperties = loadPropsFromGAM(state);
		} catch (Exception e) {
			logger.error("[] updatePropsFromGAM " , e);
		}
	}

	public static String getSingleLogoutendpoint() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"SingleLogoutEndpoint");
		logger.debug("[getSingleLogoutendpoint] " + "Gets SingleLogoutendpoint: " + ret);
		return ret;
	}

	public static String getSingleLogoutLocation() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"SingleLogoutLocation");
		logger.debug("[getSingleLogoutLocation] " + "Gets SingleLogoutLocation: " + ret);
		return ret;
	}

	public static String getSingleLogoutResponseLocation() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"SingleLogoutResponseLocation");
		logger.debug("[getSingleLogoutResponseLocation] " + "Gets SingleLogoutResponseLocation: " + ret);
		return ret;
	}

	public static String getServiceProviderEntityId() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"ServiceProviderEntityId");
		logger.debug("[getServiceProviderEntityId] " + "Gets ServiceProviderEntityId: " + ret);
		return ret;
	}

	public static String getSamlEndpointLocation() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"SamlEndpointLocation");
		logger.debug("[getSamlEndpointLocation] " + "Gets SamlEndpointLocation: " + ret);
		return ret;
	}

	public static String getNameIDPolicyFormat() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"NameIDPolicyFormat");
		logger.debug("[getNameIDPolicyFormat] " + "Gets NameIDPolicyFormat: " + ret);
		return ret;
	}

	public static String getAuthnContext() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"AuthnContext");
		logger.debug("[getAuthnContext] " + "Gets AuthnContext: " + ret);
		return ret;
	}

	public static String getForceAuthn() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"ForceAuthn");
		logger.debug("[getForceAuthn] " + "Gets ForceAuthn: " + ret);
		return ret;
	}

	public static String getKeyStPathCredential() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyStPathCredential");
		logger.debug("[getKeyStPathCredential] " + "Gets KeyStPathCredential: " + ret);
		return ret;
	}

	public static String getKeyStPwdCredential() {
		String cryptPass = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyStPwdCredential");
		String ret = Crypt.Decrypt(cryptPass);
		logger.debug("[getKeyStPwdCredential] " + "Gets getKeyStPwdCredential: " + ret);

		return ret;
	}

	public static String getKeyAliasCredential() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyAliasCredential");
		logger.debug("[getKeyAliasCredential] " + "Gets getKeyAliasCredential: " + ret);
		return ret;
	}

	public static String getKeyStoreCredential() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyStoreCredential");
		logger.debug("[getKeyStoreCredential] " + "Gets getKeyStoreCredential: " + ret);

		return ret;
	}

	public static String getKeyStPathTrustSt() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyStoreFilePathTrustCred");
		logger.debug("[getKeyStPathTrustSt] " + "Gets KeyStoreFilePathTrustCred: " + ret);
		return ret;
	}

	public static String getKeyStPwdTrustSt() {
		String cryptPass = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyStorePwdTrustCred");
		String ret = Crypt.Decrypt(cryptPass);
		logger.debug("[getKeyStPwdTrustSt] " + "Gets KeyStorePwdTrustCred: " + cryptPass);
		return ret;
	}

	public static String getKeyStoreTrustSt() {
		return GamSamlProperties.getInstance()
			.getGeneralProperty("KeyStoreTrustCred");
	}

	public static String getKeyAliasTrustSt() {
		return GamSamlProperties.getInstance().getGeneralProperty("KeyAliasTrustSt");
	}

	public static String getKeyStoreFilePathTrustCred() {
		String ret = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyStoreFilePathTrustCred");
		logger.debug("[getKeyStoreFilePathTrustCred] " + "Gets KeyStoreFilePathTrustCred: " + ret);
		return ret;
	}

	public static String getKeyStorePwdTrustCred() {
		String cryptPass = GamSamlProperties.getInstance().getGeneralProperty(
			"KeyStorePwdTrustCred");
		logger.debug("[getKeyStorePwdTrustCred] " + "cryptPass: " + cryptPass);
		String ret = Crypt.Decrypt(cryptPass);
		logger.debug("[getKeyStorePwdTrustCred] " + "Gets KeyStorePwdTrustCred: " + ret);
		return ret;
	}

	public static String getKeyCrypt() {
		return GamSamlProperties.getInstance().getGeneralProperty("KeyCrypt");
	}

	public static String getKeyAliasTrustCred() {
		return GamSamlProperties.getInstance()
			.getGeneralProperty("KeyAliasTrustCred");
	}

	public static String getKeyStoreTrustCred() {
		return GamSamlProperties.getInstance()
			.getGeneralProperty("KeyStoreTrustCred");
	}

	public static String getPathErrorServletSSO() {
		return GamSamlProperties.getInstance().getGeneralProperty(
			"PathErrorServletSSO");
	}

	public static String getPathLoginServletSSO() {
		return GamSamlProperties.getInstance().getGeneralProperty(
			"PathLoginServletSSO");
	}

	public static String getPathCancelServletSSO() {
		return GamSamlProperties.getInstance().getGeneralProperty(
			"PathCancelServletSSO");
	}

	public static String getCriteriaKey() {
		String cryptPass = GamSamlProperties.getInstance().getGeneralProperty("CriteriaKey");

		String ret = Crypt.Decrypt(cryptPass);
		logger.debug("[getCriteriaKey] " + "Gets getCriteriaKey: " + ret);

		return ret;
	}

	public static String isSmartCardContext() {
		return GamSamlProperties.getInstance()
			.getGeneralProperty("SmartCardAuthContext").trim();
	}

	// it cames always false by now
	public static String isRedirectBinding() {
		return GamSamlProperties.getInstance()
			.getGeneralProperty("isRedirectBinding").trim();
	}

	public static String getAssertionConsumerServiceURL() {
		String ret = GamSamlProperties.getInstance()
			.getGeneralProperty("AssertionConsumerServiceURL").trim();
		logger.debug("[getAssertionConsumerServiceURL] " + "Gets AssertionConsumerServiceURL: " + ret);
		return ret;
	}

	// ATTprops
	public static String getSAMLPARAMETER() {
		return "SAMLResponse";
	}

	public static String getAttSamlParameter() {
		return "SAMLPARAMETER";
	}

	public static String getAttUID() {
		return GamSamlProperties.getInstance().getAttProperty("AttUID");
	}

	public static String getAttDOCUMENTO() {
		return GamSamlProperties.getInstance().getAttProperty("AttDOCUMENTO");
	}

	public static String getAttPAISDOCUMENTO() {
		return GamSamlProperties.getInstance().getAttProperty("AttPAISDOCUMENTO");
	}

	public static String getAttPRESENCIAL() {
		return GamSamlProperties.getInstance().getAttProperty("AttPRESENCIAL");
	}

	public static String getAttTIPODOCUMENTO() {
		return GamSamlProperties.getInstance().getAttProperty("AttTIPODOCUMENTO");
	}

	public static String getAttNOMBRECOMPLETO() {
		return GamSamlProperties.getInstance().getAttProperty("AttNOMBRECOMPLETO");
	}

	public static String getAttCERTIFICADO() {
		return GamSamlProperties.getInstance().getAttProperty("AttCERTIFICADO");
	}

	public static String getAttTRUE() {
		return GamSamlProperties.getInstance().getAttProperty("AttTRUE");
	}

	public static String getAttPRIMERNOMBRE() {
		return GamSamlProperties.getInstance().getAttProperty("AttPRIMERNOMBRE");
	}

	public static String getAttSEGUNDONOMBRE() {
		return GamSamlProperties.getInstance().getAttProperty("AttSEGUNDONOMBRE");
	}

	public static String getAttPRIMERAPELLIDO() {
		return GamSamlProperties.getInstance().getAttProperty("AttPRIMERAPELLIDO");
	}

	public static String getAttSEGUNDOAPELLIDO() {
		return GamSamlProperties.getInstance().getAttProperty("AttSEGUNDOAPELLIDO");
	}

	public static String getState() {
		return state;
	}

	private String getGeneralProperty(String prop) {
		return getPropertyValue(generalPropsProperties, prop);
	}

	private String getAttProperty(String prop) {
		return getPropertyValue(attPropsProperties, prop);
	}

	public String getPropertyValue(Properties config, String prop) {
		if (config == null) {
			return null;
		} else {
			return config.getProperty(prop);
		}
	}

	public Properties loadPropsFromGAM(String state) throws Exception {
		logger.debug("[loadPropsFromGAM] start");
		Properties config = new Properties();
		String[] resultString = new String[1];
		try
		{
			Class<?> gamClass = Class.forName("genexus.security.api.getauthenticationparmsfromstate");
			Object gamObj = gamClass.getDeclaredConstructor(int.class).newInstance(-1);
			Class<?>[] paramTypes = { String.class, java.lang.String[].class };
			Method method = gamClass.getMethod("execute", paramTypes);
			method.invoke(gamObj, state, resultString);
		}catch (Exception e)
		{
			logger.error("[loadPropsFromGAM]  reflection", e);
		}
		String result = resultString[0].trim();
		logger.debug("[loadPropsFromGAM] result: " + result);
		config.load(new StringReader(result));
		logger.debug("[loadPropsFromGAM] config: " + config.toString());
		return config;
	}

	public void saveConfigFile(String result) throws Exception {
		Properties config = new Properties();
		config.load(new StringReader(result));
		generalPropsProperties = config;
		logger.debug("[saveConfigFile] config: " + config.toString());
	}
}
