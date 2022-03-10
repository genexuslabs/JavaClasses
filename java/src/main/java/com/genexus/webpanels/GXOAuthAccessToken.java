package com.genexus.webpanels;

import com.genexus.Application;
import com.genexus.ModelContext;
import com.genexus.PrivateUtilities;
import com.genexus.internet.HttpContext;
import com.genexus.security.GXResult;
import com.genexus.security.GXSecurityProvider;
import com.genexus.security.OutData;

import json.org.json.JSONException;
import json.org.json.JSONObject;

public class GXOAuthAccessToken extends GXWebObjectStub
{   
		protected void doExecute(HttpContext context) throws Exception
    {	
    	new WebApplicationStartup().init(Application.gxCfg, context);
    	context.setStream();
    	try
    	{
    		boolean isRefreshToken = false;
    		boolean isDevice = false;
    		boolean isExternalSDAuth = false;
				String clientId = context.getHttpRequest().getVariable("client_id");
				String clientSecret = context.getHttpRequest().getVariable("client_secret");
				String grantType = context.getHttpRequest().getVariable("grant_type");
				String nativeToken = context.getHttpRequest().getVariable("native_token");			
				String nativeVerifier = context.getHttpRequest().getVariable("native_verifier");
				String avoid_redirect = context.getHttpRequest().getVariable("avoid_redirect");
				String refreshToken = "";
			
				String userName = "";
				String userPassword = "";
				String additional_parameters = "";
				String scope = "";
				if (grantType.equalsIgnoreCase("refresh_token"))
			
				{
					refreshToken = context.getHttpRequest().getVariable("refresh_token");
					isRefreshToken = true;
				}
				else
				{
					if (grantType.equalsIgnoreCase("device"))
					{
						isDevice = true;
					}
					else
					{
						if (!nativeToken.equals(""))
						{
							isExternalSDAuth = true;
							additional_parameters = context.getHttpRequest().getVariable("additional_parameters");
						}
						else
						{
							userName = context.getHttpRequest().getVariable("username");
							userPassword = context.getHttpRequest().getVariable("password");
							scope = context.getHttpRequest().getVariable("scope");
							additional_parameters = context.getHttpRequest().getVariable("additional_parameters");
						}
					}
				}
			
				OutData gamout = new OutData();
				GXResult result;
				String[] redirectURL = new String[] {""};
				boolean[] flag = new boolean[]{false};
				String[] scopeInOut = new String[] {scope};
			
				ModelContext modelContext =  new ModelContext(Application.gxCfg);
				modelContext.setHttpContext(context);
				ModelContext.getModelContext().setHttpContext(context);
			
				if (isRefreshToken)
				{
					result = GXSecurityProvider.getInstance().refreshtoken(-2, modelContext, clientId, clientSecret, refreshToken, gamout, flag);
				}
				else
				{
					if (isDevice)
					{
						result = GXSecurityProvider.getInstance().logindevice(-2, modelContext, clientId, clientSecret, gamout, flag);
					}
					else
					{
						if (isExternalSDAuth)
						{
							result = GXSecurityProvider.getInstance().externalauthenticationfromsdusingtoken(-2, modelContext, grantType, nativeToken, nativeVerifier, clientId, clientSecret, scopeInOut, additional_parameters, gamout, flag);
						}				
						else 
						{
							if (additional_parameters.equals(""))
							{
								result = GXSecurityProvider.getInstance().oauthauthentication(-2, modelContext, grantType, userName, userPassword, clientId, clientSecret, scope, gamout, redirectURL, flag);
							}
							else
							{
								result = GXSecurityProvider.getInstance().oauthauthentication(-2, modelContext, grantType, userName, userPassword, clientId, clientSecret, scope, additional_parameters, gamout, redirectURL, flag);
							}
						}

					}
				}
			
				if(!flag[0])
				{
					context.getResponse().setContentType("application/json");
					String gamError = result.getCode();

					if (gamError.equals("400") || gamError.equals("410"))
					{
						context.getResponse().setStatus(202);
					}
					else
					{
						context.getResponse().setStatus(401);
					}

					String messagePermission = result.getDescription();
					String messagePermissionEncoded = messagePermission;
					if (PrivateUtilities.containsNoAsciiCharacter(messagePermission))
					{
						messagePermissionEncoded = PrivateUtilities.encodeURL(messagePermission);
						messagePermissionEncoded = "Encoded:" + messagePermissionEncoded; 
					}
					String OauthRealm = "OAuth realm=\"" + context.getRequest().getServerName() + "\"" + ",error_code=\"" + gamError + "\"" + ",error_description=\"" + messagePermissionEncoded + "\"";
					context.getResponse().addHeader("WWW-Authenticate", OauthRealm);				
					SetError(gamError, messagePermission);				
					context.writeText(errorJson.toString());
					context.getResponse().flushBuffer();
					return;
				}
				else
				{
					if (!isDevice && !isRefreshToken && ((String)gamout.get("gxTpr_Access_token")).equals(""))
					{
						context.getResponse().setContentType("application/json");
						if (avoid_redirect!=null && !avoid_redirect.equals(""))
							context.getResponse().setStatus(200);
						else
							context.getResponse().setStatus(303);
						context.getResponse().addHeader("location", redirectURL[0]); 
						JSONObject jObj = new JSONObject();
						jObj.put("Location", redirectURL[0]);
						context.writeText(jObj.toString());
						context.getResponse().flushBuffer();
						return;					
					}
					else
					{
						context.getResponse().setContentType("application/json");
						context.getResponse().setStatus(200);
						context.writeText((String)gamout.getjsonString());
						context.getResponse().flushBuffer();
						return;			
					}
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				context.sendResponseStatus(404, e.getMessage());
			}
   	}
	
   
    
    private JSONObject errorJson;
    public void SetError(String code, String message)
		{
			try
			{
				JSONObject obj = new JSONObject();
				obj.put("code", code);
				obj.put("message", message);
				errorJson = new JSONObject();
				errorJson.put("error", obj);
			}
			catch(JSONException e)
			{
				System.out.println(e.toString());
			}
		}
    
    protected boolean IntegratedSecurityEnabled( )
   	{
   		return false;
   	}
   	
   	protected int IntegratedSecurityLevel( )
   	{
   		return 0;
   	}
   	
   	protected String IntegratedSecurityPermissionPrefix( )
   	{
      return "";
   }

	protected String EncryptURLParameters() {return "NO";};
   
   protected void init(HttpContext context )
   {
   }      
}
