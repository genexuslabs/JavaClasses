package com.genexus.security;

import com.genexus.ModelContext;

public interface SecurityProvider
{
	GXResult checkaccesstoken(int remoteHandle, ModelContext context, String token, boolean[] flag);
	GXResult checkaccesstokenprm(int remoteHandle, ModelContext context, String token, String permissionPrefix, boolean[] permissionFlag, boolean[] flag);
	void checksession(int remoteHandle, ModelContext context, String reqUrl, boolean[] flag);
	void checksessionprm(int remoteHandle, ModelContext context, String reqUrl, String permissionPrefix, boolean[] flag, boolean[] isPermissionOK);
	GXResult refreshtoken(int remoteHandle, ModelContext context, String clientId, String clientSecret, String refreshToken, OutData outData, boolean[] flag);
	GXResult logindevice(int remoteHandle, ModelContext context, String clientId, String clientSecret, OutData outData, boolean[] flag);
	GXResult externalauthenticationfromsdusingtoken(int remoteHandle, ModelContext context, String grantType, String nativeToken, String nativeVerifier, String clientId, String clientSecret, String[] scope, String additional_parameters, OutData outData, boolean[] flag);
	GXResult externalauthenticationfromsdusingtoken(int remoteHandle, ModelContext context, String grantType, String nativeToken, String nativeVerifier, String clientId, String clientSecret, String[] scope, OutData outData, boolean[] flag);
	GXResult oauthauthentication(int remoteHandle, ModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, OutData outData, String[] redirectURL, boolean[] flag);
	GXResult oauthauthentication(int remoteHandle, ModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, String additional_parameters, OutData outData, String[] redirectURL, boolean[] flag);
	void oauthgetuser(int remoteHandle, ModelContext context, String[] userJson, boolean[] isOK);
	void oauthlogout(int remoteHandle, ModelContext context, String[] URL, short[] statusCode);
}