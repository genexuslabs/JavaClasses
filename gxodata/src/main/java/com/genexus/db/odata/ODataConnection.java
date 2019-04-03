package com.genexus.db.odata;

import com.genexus.ModelContext;
import com.genexus.db.driver.GXDBMSservice;
import com.genexus.db.service.ServiceConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.domain.ClientEnumValue;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import java.util.concurrent.Executor;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.client.core.http.BasicAuthHttpClientFactory;
import org.apache.olingo.client.core.http.DefaultHttpClientFactory;
import org.apache.olingo.client.core.http.ProxyWrappingHttpClientFactory;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmMember;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;

public class ODataConnection extends ServiceConnection
{
    ODataClient client;
    ModelInfo modelInfo;
       
    public ODataConnection(String connUrl, Properties connProps)
    {
        super(connUrl, connProps);
        connProps = null; // se usa props de la clase base
        String metadataLocation = String.format("Metadata%sServices%s", File.separatorChar, File.separatorChar);
        modelInfo = ModelInfo.getModel(connUrl);
        Edm model  = modelInfo != null ? modelInfo.model : null;
        if(modelInfo == null)
        {
            String metadataDocName = null;
            String proxyURI = null;
            String checkOptimisticConcurrency = null;
            String user = null, password = null;
            boolean force_auth = false;
            boolean use_chunked = false;
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
                    default: break;
                }

            }
            if(model == null)
            {
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
                }catch(URISyntaxException | IOException e){}
            }        

            HttpClientFactory handlerFactory = null;
            if(user != null && password != null && 
               !(user.equals("") && password.equals("")))
            {
                if(!force_auth)
                    handlerFactory = new BasicAuthHttpClientFactory(user, password);
                else
                {
                    final String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", user, password).getBytes(StandardCharsets.UTF_8));
                    handlerFactory = new BasicAuthHttpClientFactory(user, password)
                    {
                        @Override
                        public DefaultHttpClient create(HttpMethod method, URI uri)
                        {
                            final DefaultHttpClient httpClient = super.create(method, uri);
                            httpClient.addRequestInterceptor(new HttpProcessor()
                            {
                                @Override
                                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
                                { // Caso en que el servicio va con autenticación Basic pero no esta enviando el Challenge al dar el response 401.Unauthorized
                                  // Si se pone la property force_auth=y se manda el header de autorización de antemano
                                    request.addHeader("Authorization", authHeaderValue);                                
                                }
                                @Override
                                public void process(HttpResponse request, HttpContext context) throws HttpException, IOException
                                {
                                }
                            });
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
                client = model == null ? ODataClientFactory.getClient() : ODataClientFactory.getEdmEnabledClient(url, model, null, ContentType.JSON_FULL_METADATA);
                model = client.getRetrieveRequestFactory().getMetadataRequest(url).execute().getBody();
            }
            modelInfo = new ModelInfo(url, model, checkOptimisticConcurrency);
            modelInfo.handlerFactory = handlerFactory;
            modelInfo.useChunked = use_chunked;
            ModelInfo.addModel(connUrl, modelInfo);
        }

        if(client == null)
        {
            client = model == null ? ODataClientFactory.getClient() : ODataClientFactory.getEdmEnabledClient(url, model, null, ContentType.JSON_FULL_METADATA);
        }
        if(modelInfo.handlerFactory != null)
            client.getConfiguration().setHttpClientFactory(modelInfo.handlerFactory);
        client.getConfiguration().setUseChuncked(modelInfo.useChunked);  
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

    public String entity(String name)
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
    public void close() throws SQLException
    {
        client = null;
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        return client != null;
    }

//----------------------------------------------------------------------------------------------------
    @Override
    public String getDatabaseProductName() throws SQLException
    {
        return "OData";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException
    {
        return client.getServiceVersion().toString();
    }

    @Override
    public String getDriverName() throws SQLException
    {
        return client.getClass().getName();
    }

    @Override
    public String getDriverVersion() throws SQLException
    {
        return client.getServiceVersion().toString();
    }

// JDK8:
    @Override
    public void setSchema(String schema) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public String getSchema() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void abort(Executor executor) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getNetworkTimeout() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }            
    
}
