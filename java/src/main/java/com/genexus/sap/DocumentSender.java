


package com.genexus.sap;

import com.genexus.ModelContext;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;

import com.sap.conn.jco.server.JCoServerFactory;
import com.sap.conn.jco.JCoCustomRepository;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.JCoListMetaData;
import com.sap.conn.jco.JCo;
import com.genexus.diagnostics.Log;

public class DocumentSender
{
    private String serverName;
	private String repositoryName;
    private ModelContext _context;
	JCoServer sender = null;

    
    public DocumentSender(String server, String repository, ModelContext context)
    {
        this.serverName = server;
        this.repositoryName = repository;
        this._context = context;
    }

   	public void start() 
	{		
		Log.info("GX SAP - DMS Sender Start " +  serverName + " " + repositoryName);		
	
		try {		
			sender = setupServerUpload(serverName, repositoryName);
			sender.start();
		}
		catch (JCoException ex)
		{
			Log.error("GX SAP - Error Starting " + ex) ;
		}
	}

	public void stop() 
	{
		String senderName = "";
		if (serverName == null || serverName.equals(""))
		{
			Object senderObj = _context.getContextProperty("SAPSenderServerName");
			if (senderObj != null)
			{
				senderName = (String)senderObj;
			}
		}		
		else
		{
			senderName = serverName;
		}
		try {
			if (!senderName.equals(""))
			{
				sender = JCoServerFactory.getServer(senderName);				
				if (sender != null) sender.stop();
				Log.info("GX SAP - DMS Sender stop " +  senderName );
			}
			else
			{
				Log.error("GX SAP - Error Stopping sender"  );	
			}
		}
		catch (JCoException ex)
		{
			Log.error("GX SAP - Error Stopping " + ex) ;
		}
	}
     
	public JCoServer setupServerUpload(String serverName, String repositoryName) throws JCoException
	{
		JCoServer server = JCoServerFactory.getServer(serverName);

		JCoCustomRepository repo = JCo.createCustomRepository(repositoryName);
		JCoListMetaData imports, exports, tables;
		JCoFunctionTemplate FTP_CLIENT_TO_R3;
	
		imports = JCo.createListMetaData("IMPORTING");
		imports.add("FNAME", JCoMetaData.TYPE_CHAR, 256, 512, JCoListMetaData.IMPORT_PARAMETER);		
		imports.add("MODE", JCoMetaData.TYPE_CHAR, 1, 2, JCoListMetaData.IMPORT_PARAMETER | JCoListMetaData.OPTIONAL_PARAMETER);
		imports.lock();

		exports = JCo.createListMetaData("EXPORTING");
		exports.add("ERROR", JCoMetaData.TYPE_INT, 4, 4, JCoListMetaData.EXPORT_PARAMETER);
		exports.add("LENGTH", JCoMetaData.TYPE_INT, 4, 4, JCoListMetaData.EXPORT_PARAMETER);
		exports.lock();

		JCoRecordMetaData tabLine = JCo.createRecordMetaData("BLOB");
		tabLine.add("LINE", JCoMetaData.TYPE_BYTE, EnterpriseConnect.BLOB_LENGTH, 0, EnterpriseConnect.BLOB_LENGTH, 0);
		tabLine.lock();

		tables = JCo.createListMetaData("TABLES");
		tables.add("BLOB", JCoMetaData.TYPE_TABLE, tabLine, 0);
		tables.lock();

		FTP_CLIENT_TO_R3 = JCo.createFunctionTemplate("FTP_CLIENT_TO_R3", imports, exports, null, tables, null);
		repo.addFunctionTemplateToCache(FTP_CLIENT_TO_R3);
		server.setRepository(repo);

		DefaultServerHandlerFactory.FunctionHandlerFactory handlerFactory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
		handlerFactory.registerHandler("FTP_CLIENT_TO_R3", new DocumentClient.FTP_CLIENT_TO_R3Handler());
		server.setCallHandlerFactory(handlerFactory);

		DocumentClient.ErrorHandler hdl = new DocumentClient.ErrorHandler();
		server.addServerErrorListener(hdl);
		server.addServerExceptionListener(hdl);

		return server;
	}

}