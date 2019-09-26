package com.genexus.db.odata;

import com.genexus.ModelContext;
import com.genexus.db.driver.GXDBMSservice;
import com.genexus.db.service.ServiceConnection;
import org.apache.http.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.olingo.client.api.ODataClient;
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
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ODataConnection extends ServiceConnection
{
    ODataClient client;
    private ModelInfo modelInfo;
	private ContentType defaultContentType = ContentType.JSON_FULL_METADATA;

	public ODataConnection(String connUrl, Properties initialConnProps)
    {
        super(connUrl, initialConnProps); /// Luego de la inicializacion usar props de la clase base para obtener las propiedades
        modelInfo = ModelInfo.getModel(connUrl);
        Edm model  = modelInfo != null ? modelInfo.model : null;
		if(modelInfo == null)
        {
        	model = initializeModel(connUrl);
        }

        if(client == null)
        {
            client = model == null ? ODataClientFactory.getClient() : ODataClientFactory.getEdmEnabledClient(url, model, null, defaultContentType);
        }
        if(modelInfo.handlerFactory != null)
            client.getConfiguration().setHttpClientFactory(modelInfo.handlerFactory);
        client.getConfiguration().setUseChuncked(modelInfo.useChunked);  
    }

    private Edm initializeModel(String connUrl)
	{
		Edm model = null;
		String metadataLocation = String.format("Metadata%sServices%s", File.separatorChar, File.separatorChar);
		String metadataDocName = null;
		String proxyURI = null;
		String checkOptimisticConcurrency = null;
		String user = null, password = null;
		boolean force_auth = false;
		boolean use_chunked = false;
		String sapLoginBO = null, b1SessionId = null;
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
				case "b1session": b1SessionId = value; break;
				default: break;
			}

		}
		try
		{
			if(metadataDocName == null)
				metadataDocName = new java.net.URI(url).getHost();
			String metadataDocLoc = String.format("%s%s.xml",  metadataLocation, metadataDocName);
			File metadataDocFile = new File(metadataDocLoc);
			if(!metadataDocFile.canRead())
				metadataDocFile = new File(new URI(ModelContext.getModelContext().packageClass.getResource("/../../").toURI()+metadataDocLoc.replace('\\', '/')));
			if(metadataDocFile.canRead())
			{
				try (FileInputStream fis = new FileInputStream(metadataDocFile))
				{
					model = ODataClientFactory.getClient().getReader().readMetadata(fis);
				}
			}
		}catch(URISyntaxException | IOException ignored){}

		if(sapLoginBO != null)
		{ // SAP BusinessOne requiere autenticarse previamente para obtener la sesion
			// Ademas no esta aceptando ContentType.JSON_FULL_METADATA
			defaultContentType = ContentType.JSON;
			String loginBase = url.trim();
			while(loginBase.endsWith("/"))
				loginBase = loginBase.substring(0, loginBase.length() - 1);

			try
			{
				HttpPost login = new HttpPost(new URI(String.format("%s/Login", loginBase)));
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
						b1SessionId = cookie.substring(cookieStart + 10, cookieEnd - cookieStart);
					}
				}
			}catch (URISyntaxException | IOException ignored){}
		}

		HttpClientFactory handlerFactory = null;
		if(user != null && password != null &&
			!(user.equals("") && password.equals("")))
		{
			if(!force_auth && b1SessionId == null)
				handlerFactory = new BasicAuthHttpClientFactory(user, password);
			else
			{
				final String authHeaderValue = force_auth ? "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", user, password).getBytes(StandardCharsets.UTF_8)) : null;
				final String b1SessionCookie = b1SessionId != null ? String.format("B1SESSION=%s", b1SessionId) : null;
				final boolean fixResponse = b1SessionCookie != null;
				handlerFactory = new BasicAuthHttpClientFactory(user, password)
				{
					@Override
					public DefaultHttpClient create(HttpMethod method, URI uri)
					{
						final DefaultHttpClient httpClient = super.create(method, uri);
						httpClient.addRequestInterceptor((request, context) ->
						{ // Caso en que el servicio va con autenticación Basic pero no esta enviando el Challenge al dar el response 401.Unauthorized
							// Si se pone la property force_auth=y se manda el header de autorización de antemano
							if(authHeaderValue != null)
								request.addHeader("Authorization", authHeaderValue);
							// Caso SAP BusinessOne se manda la B1SESSION
							if(b1SessionCookie != null)
								request.addHeader("Cookie", b1SessionCookie);
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
									if(entity.isStreaming())
									{
										response.setEntity(new HttpEntityWrapper(entity)
										{
											@Override
											public InputStream getContent() throws IOException
											{
												return new FilterInputStream(super.getContent())
												{
													final int [] PREFIX_LOWER = new int[]{'\"','v','a','l','u','e','\"'};
													final int [] PREFIX_UPPER = new int[]{'\"','V','A','L','U','E','\"'};
													private int index = 0;
													public int read() throws IOException
													{
														int b = super.read();
														if(b < 0)
															return b;
														if(index < PREFIX_LOWER.length)
														{
															if(b == PREFIX_LOWER[index] || b == PREFIX_UPPER[index])
																index++;
															else index = 0;
														}else
														{
															while(b == ' ')
																b = super.read();
															index = 0;
														}
														return b;
													}
												};
											}
										});


									}else
									{
										Header contentTypeHeader = entity.getContentType();
										org.apache.http.entity.ContentType contentType = contentTypeHeader != null ? org.apache.http.entity.ContentType.parse(contentTypeHeader.getValue()) : org.apache.http.entity.ContentType.DEFAULT_TEXT;
										String content = EntityUtils.toString(entity, contentType.getCharset());
										String fixedContent = content.replace("\"value\" : ", "\"value\":");
										response.setEntity(new StringEntity(fixedContent, contentType));
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

		if(model == null)
		{
			client = ODataClientFactory.getClient();
			client.getConfiguration().setDefaultPubFormat(defaultContentType);
			model = client.getRetrieveRequestFactory().getMetadataRequest(url).execute().getBody();
		}
		modelInfo = new ModelInfo(url, model, checkOptimisticConcurrency);
		modelInfo.handlerFactory = handlerFactory;
		modelInfo.useChunked = use_chunked;
		ModelInfo.addModel(connUrl, modelInfo);
		return model;
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
        return client.getServiceVersion().toString();
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
    
}
