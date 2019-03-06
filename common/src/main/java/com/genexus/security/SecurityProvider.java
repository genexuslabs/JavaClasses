package com.genexus.security;

import com.genexus.common.classes.AbstractModelContext;

public interface SecurityProvider
{
	GXResult checkaccesstoken(int remoteHandle, AbstractModelContext context, String token, boolean[] flag);
	GXResult checkaccesstokenprm(int remoteHandle, AbstractModelContext context, String token, String permissionPrefix, boolean[] permissionFlag, boolean[] flag);
	void checksession(int remoteHandle, AbstractModelContext context, String reqUrl, boolean[] flag);
	void checksessionprm(int remoteHandle, AbstractModelContext context, String reqUrl, String permissionPrefix, boolean[] flag, boolean[] isPermissionOK);
	GXResult refreshtoken(int remoteHandle, AbstractModelContext context, String clientId, String clientSecret, String refreshToken, OutData outData, boolean[] flag);
	GXResult logindevice(int remoteHandle, AbstractModelContext context, String clientId, String clientSecret, OutData outData, boolean[] flag);
	GXResult externalauthenticationfromsdusingtoken(int remoteHandle, AbstractModelContext context, String grantType, String nativeToken, String nativeVerifier, String clientId, String clientSecret, String[] scope, OutData outData, boolean[] flag);
	GXResult oauthauthentication(int remoteHandle, AbstractModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, OutData outData, String[] redirectURL, boolean[] flag);
	GXResult oauthauthentication(int remoteHandle, AbstractModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, String additional_parameters, OutData outData, String[] redirectURL, boolean[] flag);
	void oauthgetuser(int remoteHandle, AbstractModelContext context, String[] userJson, boolean[] isOK);
	void oauthlogout(int remoteHandle, AbstractModelContext context);	
}