package com.genexus.db.odata;

import com.genexus.ModelContext;
import com.genexus.db.driver.GXDBMSservice;
import com.genexus.db.service.ServiceConnection;
import com.genexus.db.service.ServiceError;
import com.genexus.diagnostics.core.LogManager;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.ODataClientErrorException;
import org.apache.olingo.client.api.domain.ClientEnumValue;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.apache.olingo.client.core.http.DefaultHttpClientFactory;
import org.apache.olingo.client.core.http.ProxyWrappingHttpClientFactory;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class ODataConnection extends ServiceConnection
{
	private static final String GXODATA_VERSION = "1.1";

    ODataClient client;
    private ModelInfo modelInfo;

	public ODataConnection(String connUrl, Properties initialConnProps) throws SQLException
	{
        super(connUrl, initialConnProps); /// Luego de la inicialización usar props de la clase base para obtener las propiedades
        modelInfo = ModelInfo.getModel(connUrl);
		if(modelInfo == null)
        {
        	try
			{
				initializeModel(connUrl);
			}catch(ODataClientErrorException e)
			{
				throw new SQLException(e);
			}
        }else
		{
			Edm model  = modelInfo.model;
			client = model == null ? ODataClientFactory.getClient() : ODataClientFactory.getEdmEnabledClient(url, model, null, modelInfo.defaultContentType);
			if(modelInfo.handlerFactory != null)
				client.getConfiguration().setHttpClientFactory(modelInfo.handlerFactory);
		}
        client.getConfiguration().setUseChuncked(modelInfo.useChunked);
    }

    private void initializeModel(String connUrl)
	{
		Edm model = null;
		String metadataLocation = String.format("Metadata%sServices%s", File.separatorChar, File.separatorChar);
		String metadataDocName = null;
		String proxyURI = null;
		String checkOptimisticConcurrency = null;
		String user = null, password = null;
		HashSet<String> recordNotFoundServiceCodes = null;
		HashSet<String> recordAlreadyExistsServiceCodes = null;
		boolean force_auth = false;
		boolean use_chunked = false;
		String sapLoginBO = null, initialB1SessionId = null;
		ContentType defaultContentType = ContentType.JSON_FULL_METADATA;
		for(Enumeration keys = props.keys(); keys.hasMoreElements(); )
		{
			String key = ((String)keys.nextElement());
			String value = props.getProperty(key, key);
			switch(key.toLowerCase())
			{
				case "user": user = value; break;
				case "password": password = value; break;
				case "metadatalocation": metadataLocation = String.format("%s%s", value, File.separatorChar); break;
				case GXDBMSservice.DATASOURCE_NAME: metadataDocName = value; break;
				case "proxy": proxyURI = value; break;
				case "checkoptimisticconcurrency": checkOptimisticConcurrency = value; break;
				case "force_auth": force_auth = getBoolean(value); break;
				case "use_chunked": use_chunked = getBoolean(value); break;
				case "saplogin": sapLoginBO = value; break;
				case "b1session": initialB1SessionId = value; break;
				case "recordnotfoundservicecodes":
				{
					if(recordNotFoundServiceCodes == null)
						recordNotFoundServiceCodes = new HashSet<>();
					recordNotFoundServiceCodes.addAll(Arrays.asList(value.split(",")));
					break;
				}
				case "recordalreadyexistsservicecodes":
				{
					if(recordAlreadyExistsServiceCodes == null)
						recordAlreadyExistsServiceCodes = new HashSet<>();
					recordAlreadyExistsServiceCodes.addAll(Arrays.asList(value.split(",")));
					break;
				}
				default: break;
			}
		}
		try
		{
			if(metadataDocName == null)
				metadataDocName = new java.net.URI(url).getHost();
			String metadataDocLoc = String.format("%s%s.xml", metadataLocation, metadataDocName);
			File metadataDocFile = new File(metadataDocLoc);
			if(!metadataDocFile.canRead())
			{
				URL alternativeLocationURL = ModelContext.getModelContext().packageClass.getResource("./../../");
				if(alternativeLocationURL != null)
				{
					metadataDocFile = new File(new URI(alternativeLocationURL.toURI() + metadataDocLoc.replace('\\', '/')));
					if (metadataDocFile.canRead())
					{
						try (FileInputStream fis = new FileInputStream(metadataDocFile))
						{
							model = ODataClientFactory.getClient().getReader().readMetadata(fis);
						}
					}
				}
			}
		}catch(URISyntaxException | IOException ex)
		{
			LogManager.getLogger(ODataConnection.class).warn(String.format("Could not load metadata file: %s%s", metadataLocation, metadataDocName), ex);
		}

		String loginBase = null;
		if(sapLoginBO != null)
		{ // SAP BusinessOne requiere autenticarse previamente para obtener la sesion
			// Ademas no esta aceptando ContentType.JSON_FULL_METADATA
			defaultContentType = ContentType.JSON;
			loginBase = url.trim();
			while(loginBase.endsWith("/"))
				loginBase = loginBase.substring(0, loginBase.length() - 1);
			if(initialB1SessionId != null)
				B1_sessionIds.put(loginBase, String.format("B1SESSION=%s", initialB1SessionId));
			if(recordNotFoundServiceCodes == null)
				recordNotFoundServiceCodes = new HashSet<>(Arrays.asList("-2028"));
			if(recordAlreadyExistsServiceCodes == null)
				recordAlreadyExistsServiceCodes = new HashSet<>(Arrays.asList("-2035"));
		}

		HttpClientFactory handlerFactory = null;
		if(user != null && password != null &&
			!(user.equals("") && password.equals("")))
		{
			if(!force_auth && sapLoginBO == null)
				handlerFactory = new BasicAuthHttpClientFactory(user, password);
			else
			{
				final String authHeaderValue = force_auth ? "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", user, password).getBytes(StandardCharsets.UTF_8)) : null;
				final boolean fixResponse = sapLoginBO != null;
				final String m_user = user, m_password = password, m_sapLoginBO = sapLoginBO, m_loginBase = loginBase;
				handlerFactory = new BasicAuthHttpClientFactory(user, password)
				{
					@Override
					public DefaultHttpClient create(HttpMethod method, URI uri)
					{
						final DefaultHttpClient httpClient = super.create(method, uri);
						final SapB1CredentialsProvider sapB1CredentialsProvider = m_sapLoginBO != null ? new SapB1CredentialsProvider(httpClient.getCredentialsProvider(), m_user, m_password, m_sapLoginBO, m_loginBase) : null;
						if(sapB1CredentialsProvider != null)
							httpClient.setCredentialsProvider(sapB1CredentialsProvider);
						httpClient.addRequestInterceptor((request, context) ->
						{ // Caso en que el servicio va con autenticación Basic pero no esta enviando el Challenge al dar el response 401.Unauthorized
							// Si se pone la property force_auth=y se manda el header de autorización de antemano
							if(authHeaderValue != null)
								request.addHeader("Authorization", authHeaderValue);
							// Caso SAP BusinessOne se manda la B1SESSION
							if(sapB1CredentialsProvider != null)
								sapB1CredentialsProvider.addRequestHeaders(request);
						});
						if(fixResponse)
						{
							// Preprocesa el response para soportar que los datos del JSon vengan como '"value" : ' en vez de '"value":'
							httpClient.addResponseInterceptor(new HttpResponseInterceptor()
							{
								@Override
								public void process(HttpResponse response, HttpContext context) throws IOException
								{
									HttpEntity entity = response.getEntity();
									if(entity != null)
									{ // Solo interceptamos cuando efectivamente hay content
										StatusLine statusLine = response.getStatusLine();
										boolean inBadRequest = false;
										if(statusLine != null && statusLine.getStatusCode() == 400)
										{ // Sap B1 manda codigos de error numericos en vez de texto como dice el estándar
											response.setEntity(fixEntity(entity, (String content)->
											{
												int index = content.indexOf("\"code\"");
												if(index >= 0)
												{
													String head = content.substring(0, index+6);
													String tail = content.substring(index+7).trim();
													if(tail.startsWith(":"))
													{
														tail = tail.substring(1).trim();
														if(!tail.startsWith("'\""))
														{
															int index2 = tail.indexOf(',');
															int index3 = tail.indexOf('}');
															if(index2 == -1)
																index2 = index3;
															else if(index3 != -1 && index3 < index2)
																index2 = index3;
															if(index2 != -1)
															{
																return String.format("%s : \"%s\"%s", head, tail.substring(0, index2), tail.substring(index2));
															}
														}
													}
												}
												return content;
											}));
										}
										else
										{
											if (entity.isStreaming())
											{
												response.setEntity(new HttpEntityWrapper(entity)
												{
													@Override
													public InputStream getContent() throws IOException
													{
														return new FilterInputStream(super.getContent())
														{
															final int[] PREFIX_LOWER = new int[]{'\"', 'v', 'a', 'l', 'u', 'e', '\"'};
															final int[] PREFIX_UPPER = new int[]{'\"', 'V', 'A', 'L', 'U', 'E', '\"'};
															private int index = 0;

															public int read() throws IOException
															{
																int b = super.read();
																if (b < 0)
																	return b;
																if (index < PREFIX_LOWER.length)
																{
																	if (b == PREFIX_LOWER[index] || b == PREFIX_UPPER[index])
																		index++;
																	else index = 0;
																} else
																{
																	while (b == ' ')
																		b = super.read();
																	index = 0;
																}
																return b;
															}
														};
													}
												});
											} else
											{
												response.setEntity(fixEntity(entity, (String content)->content.replace("\"value\" : ", "\"value\":")));
											}
										}
									}
								}
							});
						}
						return httpClient;
					}
				};
			}
		}
		if(proxyURI != null)
		{
			handlerFactory = (handlerFactory != null ? new ProxyWrappingHttpClientFactory(URI.create(proxyURI), (DefaultHttpClientFactory) handlerFactory) : new ProxyWrappingHttpClientFactory(URI.create(proxyURI)));
		}

		client = ODataClientFactory.getClient();
		client.getConfiguration().setDefaultPubFormat(defaultContentType);
		if(handlerFactory != null)
			client.getConfiguration().setHttpClientFactory(handlerFactory);
		if(model == null)
			model = client.getRetrieveRequestFactory().getMetadataRequest(url).execute().getBody();
		modelInfo = new ModelInfo(url, model, checkOptimisticConcurrency);
		modelInfo.handlerFactory = handlerFactory;
		modelInfo.useChunked = use_chunked;
		modelInfo.defaultContentType = defaultContentType;
		modelInfo.recordNotFoundServiceCodes = recordNotFoundServiceCodes;
		modelInfo.recordAlreadyExistsServiceCodes = recordAlreadyExistsServiceCodes;
		ModelInfo.addModel(connUrl, modelInfo);
	}

	ServiceError getServiceError(String errorCode)
	{
		if(errorCode != null)
		{
			if (modelInfo.recordNotFoundServiceCodes != null && modelInfo.recordNotFoundServiceCodes.contains(errorCode))
				return ServiceError.OBJECT_NOT_FOUND;
			if (modelInfo.recordAlreadyExistsServiceCodes != null && modelInfo.recordAlreadyExistsServiceCodes.contains(errorCode))
				return ServiceError.DUPLICATE_KEY;
		}
		return ServiceError.INVALID_QUERY;
	}

	private static HttpEntity fixEntity(HttpEntity entity, Function<String, String> fixer) throws IOException
	{
		Header contentTypeHeader = entity.getContentType();
		org.apache.http.entity.ContentType contentType = contentTypeHeader != null ? org.apache.http.entity.ContentType.parse(contentTypeHeader.getValue()) : org.apache.http.entity.ContentType.DEFAULT_TEXT;
		String content = EntityUtils.toString(entity, contentType.getCharset());
		return new StringEntity(fixer.apply(content), contentType);
	}
        
    private boolean getBoolean(String value)
    {
        return value.equalsIgnoreCase("y") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
    }

    boolean needsCheckOptimisticConcurrency(URI updURI)
    {
        return modelInfo.needsCheckOptimisticConcurrency(updURI);
    }    
    
    public int getEnumValue(ClientEnumValue enumValue)
    {
        String typeName = enumValue.getTypeName();
        return Integer.parseInt(modelInfo.getModel().getEnumType(new FullQualifiedName(typeName)).getMember(enumValue.getValue()).getValue());
    }
    
    public String toEnumValue(EdmEnumType type, int value)
    {
        String sValue = Integer.toString(value);
        for(String memberName:type.getMemberNames())
        {
            EdmMember member = type.getMember(memberName);
            if(member.getValue().equals(sValue))
                return member.getName();
        }
        throw new RuntimeException(String.format("Cannot parse enum value %s - %d", type.toString(), value));
    }

    private String entity(String name)
    {
        return modelInfo.entity(name);
    }

    public String entity(EdmEntityType [] fromEntity, String name)
    {
        if(fromEntity == null || fromEntity[0] == null)
            return entity((EdmEntityType)null, name);
        String entityName = entity(fromEntity[0], name);
        if(entityName == null)
            return entity(name);
        EdmNavigationProperty navProp = fromEntity[0].getNavigationProperty(entityName);
        if(navProp != null)
            fromEntity[0] = navProp.getType();
        return entityName;        
    }
    
    public String entity(EdmEntityType fromEntity, String name)
    {
        return modelInfo.entity(fromEntity, name);
    }
    
    Edm getModel()
    {
        return modelInfo.getModel();
    }
    
//----------------------------------------------------------------------------------------------------

    @Override
    public void close()
	{
        client = null;
    }

    @Override
    public boolean isClosed()
	{
        return client != null;
    }

//----------------------------------------------------------------------------------------------------
    @Override
    public String getDatabaseProductName()
	{
        return "OData";
    }

    @Override
    public String getDatabaseProductVersion()
	{
        return client.getServiceVersion().toString();
    }

    @Override
    public String getDriverName()
	{
        return client.getClass().getName();
    }

    @Override
    public String getDriverVersion()
	{
        return String.format("%s/%s", client.getServiceVersion().toString(), GXODATA_VERSION);
    }

// JDK8:
    @Override
    public void setSchema(String schema)
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getSchema()
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void abort(Executor executor)
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds)
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getNetworkTimeout()
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean generatedKeyAlwaysReturned()
	{
        throw new UnsupportedOperationException("Not supported yet."); 
    }

	private static final HashMap<String, String> B1_sessionIds = new HashMap<>();
	private class SapB1CredentialsProvider implements CredentialsProvider
	{
		private final CredentialsProvider parent;
		private final String user;
		private final String password;
		private final String sapLoginBO;
		private final String loginBase;
		private boolean doLogin = true;
		SapB1CredentialsProvider(CredentialsProvider parent, String user, String password, String sapLoginBO, String loginBase)
		{
			this.parent = parent;
			this.user = user;
			this.password = password;
			this.sapLoginBO = sapLoginBO;
			this.loginBase = loginBase;
			if(B1_sessionIds.get(loginBase) == null)
				loginBO();
		}

		@Override
		public void setCredentials(AuthScope authScope, Credentials credentials)
		{
			parent.setCredentials(authScope, credentials);
		}

		@Override
		public Credentials getCredentials(AuthScope authScope)
		{
			if(doLogin)
				loginBO();
			return parent.getCredentials(authScope);
		}

		@Override
		public void clear()
		{
			doLogin = true;
			B1_sessionIds.remove(loginBase);
			parent.clear();
		}

		private void loginBO()
		{
			try
			{
				doLogin = false;
				B1_sessionIds.remove(loginBase);
				URI loginURI = new URI(String.format("%s/Login", loginBase));
				HttpPost login = new HttpPost(loginURI);
				StringEntity sloginInfo = new StringEntity(String.format("{\"UserName\":\"%s\", \"Password\":\"%s\", \"CompanyDB\":\"%s\"}", user, password, sapLoginBO));
				login.setEntity(sloginInfo);
				DefaultHttpClient webClient = new DefaultHttpClient();
				HttpResponse loginResponse = webClient.execute(login);
				Header cookieHdr = loginResponse.getFirstHeader("Set-Cookie");
				if(cookieHdr != null)
				{
					String cookie = cookieHdr.getValue();
					int cookieStart = cookie.indexOf("B1SESSION=");
					if (cookieStart >= 0)
					{
						int cookieEnd = cookie.indexOf(';', cookieStart);
						String b1SessionCookie = cookie.substring(cookieStart, cookieEnd - cookieStart);
						B1_sessionIds.put(loginBase, b1SessionCookie);
						doLogin = true;
					}
				}
			}catch (URISyntaxException | IOException ex)
			{
				LogManager.getLogger(ODataConnection.class).warn(String.format("Could not login to %s", loginBase), ex);
			}
		}

		void addRequestHeaders(HttpRequest request)
		{
			String b1SessionCookie = B1_sessionIds.get(loginBase);
			if(b1SessionCookie != null)
				request.addHeader("Cookie", b1SessionCookie);
		}
	}
}
