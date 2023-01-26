package com.genexus.security;

import com.genexus.ModelContext;

public class NoSecurityProvider implements SecurityProvider
{
	public GXResult checkaccesstoken(int remoteHandle, ModelContext context, String token, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}
	
	public GXResult checkaccesstokenprm(int remoteHandle, ModelContext context, String token, String permissionPrefix, boolean[] permissionFlag, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}
	
	public void checksession(int remoteHandle, ModelContext context, String reqUrl, boolean[] flag)
	{
		flag[0] = false;
	}
	
	public void checksessionprm(int remoteHandle, ModelContext context, String reqUrl, String permissionPrefix, boolean[] flag, boolean[] isPermissionOK)
	{
		isPermissionOK[0] = false;
	}
	
	public GXResult refreshtoken(int remoteHandle, ModelContext context, String clientId, String clientSecret, String refreshToken, OutData outData, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}
	
	public GXResult logindevice(int remoteHandle, ModelContext context, String clientId, String clientSecret, OutData outData, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}
	
	public GXResult externalauthenticationfromsdusingtoken(int remoteHandle, ModelContext context, String grantType, String nativeToken, String nativeVerifier, String clientId, String clientSecret, String[] scope, OutData outData, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}

	public GXResult externalauthenticationfromsdusingtoken(int remoteHandle, ModelContext context, String grantType, String nativeToken, String nativeVerifier, String clientId, String clientSecret, String[] scope, String additional_parameters, OutData outData, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}
	public GXResult oauthauthentication(int remoteHandle, ModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, OutData outData, String[] redirectURL, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}
	
	public GXResult oauthauthentication(int remoteHandle, ModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, String additional_parameters, OutData outData, String[] redirectURL, boolean[] flag)
	{
		flag[0] = false;
		return new GXResult();
	}

	public void oauthgetuser(int remoteHandle, ModelContext context, String[] userJson, boolean[] isOK)
	{
		isOK[0] = false;
		userJson[0] = "";
	}
	
	public void oauthlogout(int remoteHandle, ModelContext context, String[] URL, short[] statusCode)
	{
		URL[0] = "";
		statusCode[0] = -1;
	}
}