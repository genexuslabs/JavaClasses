package com.genexus.security;

import com.genexus.common.classes.AbstractModelContext;

public class NoSecurityProvider implements SecurityProvider
{
	public GXResult checkaccesstoken(int remoteHandle, AbstractModelContext context, String token, boolean[] flag)
	{
		flag[0] = true;
		return new GXResult();
	}
	
	public GXResult checkaccesstokenprm(int remoteHandle, AbstractModelContext context, String token, String permissionPrefix, boolean[] permissionFlag, boolean[] flag)
	{
		flag[0] = true;
		return new GXResult();
	}
	
	public void checksession(int remoteHandle, AbstractModelContext context, String reqUrl, boolean[] flag)
	{
		flag[0] = true;
	}
	
	public void checksessionprm(int remoteHandle, AbstractModelContext context, String reqUrl, String permissionPrefix, boolean[] flag, boolean[] isPermissionOK)
	{
		isPermissionOK[0] = true;
	}
	
	public GXResult refreshtoken(int remoteHandle, AbstractModelContext context, String clientId, String clientSecret, String refreshToken, OutData outData, boolean[] flag)
	{
		flag[0] = true;
		return new GXResult();
	}
	
	public GXResult logindevice(int remoteHandle, AbstractModelContext context, String clientId, String clientSecret, OutData outData, boolean[] flag)
	{
		flag[0] = true;
		return new GXResult();
	}
	
	public GXResult externalauthenticationfromsdusingtoken(int remoteHandle, AbstractModelContext context, String grantType, String nativeToken, String nativeVerifier, String clientId, String clientSecret, String[] scope, OutData outData, boolean[] flag)
	{
		flag[0] = true;
		return new GXResult();
	}
	
	public GXResult oauthauthentication(int remoteHandle, AbstractModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, OutData outData, String[] redirectURL, boolean[] flag)
	{
		flag[0] = true;
		return new GXResult();
	}
	
	public GXResult oauthauthentication(int remoteHandle, AbstractModelContext context, String grantType, String userName, String userPassword, String clientId, String clientSecret, String scope, String additional_parameters, OutData outData, String[] redirectURL, boolean[] flag)
	{
		flag[0] = true;
		return new GXResult();
	}

	public void oauthgetuser(int remoteHandle, AbstractModelContext context, String[] userJson, boolean[] isOK)
	{
		isOK[0] = true;
		userJson[0] = "";
	}
	
	public void oauthlogout(int remoteHandle, AbstractModelContext context)
	{
	}
}