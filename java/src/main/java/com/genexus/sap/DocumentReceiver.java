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

public class DocumentReceiver
{
    private String serverName;
	private String repositoryName;
    private ModelContext _context;
    JCoServer receiver = null;


    public DocumentReceiver(String server, String repository, ModelContext context)
    {
        this.serverName = server;
        this.repositoryName = repository;
        this._context = context;
    }

    public void start()
	{	
		Log.info("GX SAP - DMS Receiver Start " +  serverName + " " + repositoryName);
		try {
			receiver = setupServerDownload(serverName, repositoryName);
			receiver.start();
		}
		catch (JCoException ex)
		{
			Log.error("GX SAP - Error Starting " + ex) ;
		}
	}

    public void stop()
    {

        String receiverName = "";
		if (serverName == null || serverName.equals(""))
		{
			Object receiverObj = _context.getContextProperty("SAPReceiverServerName");
			if (receiverObj != null)
			{
				receiverName = (String)receiverObj;
			}
		}		
		else
		{
			receiverName = serverName;
		}
		
		try {
			if (!receiverName.equals(""))
			{
				receiver = JCoServerFactory.getServer(receiverName);
				if (receiver != null) receiver.stop();
				Log.info("GX SAP - DMS Receiver stop " +  receiverName );			
			}
			else
			{
				Log.error("GX SAP - Error Stopping receiver."  );	
			}
		}
		catch (JCoException ex)
		{
			Log.error("GX SAP - Error Stopping " + ex) ;
		}

    }

	public JCoServer setupServerDownload(String serverName, String repositoryName) throws JCoException
	{
		JCoServer server = JCoServerFactory.getServer(serverName);
		JCoCustomRepository repo = JCo.createCustomRepository(repositoryName);
		JCoListMetaData imports, exports, tables;
		JCoFunctionTemplate FTP_R3_TO_CLIENT;
		
		imports = JCo.createListMetaData("IMPORTING");
		imports.add("FNAME", JCoMetaData.TYPE_CHAR, 256, 512, JCoListMetaData.IMPORT_PARAMETER);
		imports.add("LENGTH", JCoMetaData.TYPE_INT, 4, 4, JCoListMetaData.IMPORT_PARAMETER);
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

		FTP_R3_TO_CLIENT = JCo.createFunctionTemplate("FTP_R3_TO_CLIENT", imports, exports, null, tables, null);
		repo.addFunctionTemplateToCache(FTP_R3_TO_CLIENT);
		server.setRepository(repo);

		DefaultServerHandlerFactory.FunctionHandlerFactory handlerFactory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
		handlerFactory.registerHandler("FTP_R3_TO_CLIENT", new DocumentClient.FTP_R3_TO_CLIENTHandler());
		server.setCallHandlerFactory(handlerFactory);

		DocumentClient.ErrorHandler hdl = new DocumentClient.ErrorHandler();
		server.addServerErrorListener(hdl);
		server.addServerExceptionListener(hdl);

		return server;
	}

}