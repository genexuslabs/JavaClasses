package com.genexus.springboot;
import java.lang.reflect.Constructor;
import jakarta.servlet.Servlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

public class GAMServletsDefinition {
	@Bean
	public ServletRegistrationBean gXOAuthAccessToken() {
		return registerServletBean("com.genexus.webpanels.GXOAuthAccessToken", "/oauth/access_token");
	}

	@Bean
	public ServletRegistrationBean gXOAuthLogout() {
		return registerServletBean("com.genexus.webpanels.GXOAuthLogout", "/oauth/logout");
	}

	@Bean
	public ServletRegistrationBean gXOAuthUserInfo() {
		return registerServletBean("com.genexus.webpanels.GXOAuthUserInfo", "/oauth/userinfo");
	}

	@Bean
	public ServletRegistrationBean gamOAuthSignIn() {
		return registerServletBean("genexus.security.api.agamextauthinput", "/oauth/gam/signin");
	}

	@Bean
	public ServletRegistrationBean gamOAuthCallback() {
		return registerServletBean("genexus.security.api.agamextauthinput", "/oauth/gam/callback");
	}

	@Bean
	public ServletRegistrationBean gamOAuthAccessToken() {
		return registerServletBean("genexus.security.api.agamoauth20getaccesstoken", "/oauth/gam/access_token");
	}

	@Bean
	public ServletRegistrationBean gamAccessTokenV2() {
		return registerServletBean("genexus.security.api.agamoauth20getaccesstoken_v20", "/oauth/gam/v2.0/access_token");
	}

	@Bean
	public ServletRegistrationBean gamOAuthUserInfo() {
		return registerServletBean("genexus.security.api.agamoauth20getuserinfo", "/oauth/gam/userinfo");
	}

	@Bean
	public ServletRegistrationBean oAuthUserInfoV2() {
		return registerServletBean("genexus.security.api.agamoauth20getuserinfo_v20", "/oauth/gam/v2.0/userinfo");
	}

	@Bean
	public ServletRegistrationBean oAuthSSORestV2() {
		return registerServletBean("genexus.security.api.agamssorestrequesttokenanduserinfo_v20", "/oauth/gam/v2.0/requesttokenanduserinfo");
	}

	@Bean
	public ServletRegistrationBean gamOAuthSignOut() {
		return registerServletBean("genexus.security.api.agamextauthinput", "/oauth/gam/signout");
	}

	@Bean
	public ServletRegistrationBean gamOAuthRequestTokenService() {
		return registerServletBean("genexus.security.api.agamstsauthappgetaccesstoken", "/oauth/RequestTokenService");
	}

	@Bean
	public ServletRegistrationBean gamOAuthQueryAccessToken() {
		return registerServletBean("genexus.security.api.agamstsauthappvalidaccesstoken", "/oauth/QueryAccessToken");
	}

	/*@Bean
	public ServletRegistrationBean gamSaml20SignOut() {
		return registerServletBean("artech.security.saml.servlet.LOGOUT", "/saml/gam/signout");
	}

	@Bean
	public ServletRegistrationBean gamSaml20SignIn() {
		return registerServletBean("artech.security.saml.servlet.SSO", "/saml/gam/signin");
	}*/

	private ServletRegistrationBean registerServletBean(String className, String path) {
		try {
			Constructor<?> constructor = Class.forName(className).getConstructor();
			ServletRegistrationBean bean = new ServletRegistrationBean((Servlet) constructor.newInstance(), path);
			return bean;
		}
		catch(Exception e) {
			return null;
		}
	}
}
