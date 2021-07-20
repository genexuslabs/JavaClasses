package com.genexus.sap;

import java.io.FileOutputStream;
import java.io.IOException;


import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;

import com.sap.conn.jco.server.JCoServerFunctionHandler;

public class DocumentClient {
    private static final int BLOB_LENGTH = 1022;

    static class ErrorHandler implements JCoServerErrorListener, JCoServerExceptionListener {   

        @Override
        public void serverExceptionOccurred(JCoServer server, String connectionID, JCoServerContextInfo serverCtx, Exception error) {
            // Technical problem in server connection (network, logon data, etc.)
            error.printStackTrace();
        }

        @Override
        public void serverErrorOccurred(JCoServer server, String connectionID, JCoServerContextInfo serverCtx, Error error) {
            // Technical problem in server connection (out-of-memory, etc.)
            error.printStackTrace();
        }

    }

    // BAPI_DOCUMENT_CHECKOUTVIEW2 will send the file data via this function module.
    static class FTP_R3_TO_CLIENTHandler implements JCoServerFunctionHandler {
    
        @Override
        public void handleRequest(JCoServerContext serverCtx, JCoFunction function) throws AbapException, AbapClassException {
            String fname;
            int length;
            JCoTable blob;
    
            // In the case of BAPI_DOCUMENT_CHECKOUTVIEW2, MODE is always binary, so the MODE and TEXT parameters of FTP_R3_TO_CLIENT can be ignored.
            JCoParameterList imports = function.getImportParameterList();
            fname = imports.getString("FNAME");
            length = imports.getInt("LENGTH");
            blob = function.getTableParameterList().getTable("BLOB");
            FileOutputStream out = null;
            System.out.println(   " file handle " + fname);
            try {
                out = new FileOutputStream(fname);
                boolean hasNextRow = false;
                if (!blob.isEmpty()){
                    hasNextRow = true;
                    blob.firstRow();
                }
                while (length > BLOB_LENGTH){
                    if (hasNextRow){
                        System.out.println(  " write .... " );
                        out.write(blob.getByteArray(0), 0, BLOB_LENGTH);
                        length -= BLOB_LENGTH;
                        hasNextRow = blob.nextRow();
                    }
                    else throw new IOException("Not enough data in table BLOB ("+String.valueOf(BLOB_LENGTH * blob.getNumRows())+") for requested file size (" + String.valueOf(length) + ")");
                }
                if (length > 0){
                    if (hasNextRow) out.write(blob.getByteArray(0), 0, length);
                    else throw new IOException("Not enough data in table BLOB ("+String.valueOf(BLOB_LENGTH * blob.getNumRows())+") for requested file size (" + String.valueOf(length) + ")");
                }
            }
            catch (IOException e) {
                // Unfortunately there is no way of transmitting error details back to SAP, so we better log it here,
                // if we want to keep the chance of trouble-shooting later, what exactly went wrong...
                e.printStackTrace();
                function.getExportParameterList().setValue("ERROR", 3);
            }
            finally{
                if (out != null){
                    try{
                        out.close();
                    }
                    catch (IOException ioe){}
                }
            }
        }
    
    }

}