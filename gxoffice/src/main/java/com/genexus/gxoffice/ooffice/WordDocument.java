//30-08-2005
// Se define el Metodo RunMacro.  

//26-08-2005
//Se agrega control de finalizaci�n de macros/cmds, a BasicMacroTools.class

//19-08-2005
//se corrige filtro para salvar archivo .htm
//se agrego MacroListener para recuperar el c�digo Result de ejecuci�n para macros/comandos

//18-08-2005
//se corrigieron save y saveas. problema de validaci�n estado de conexi�n.
//se corrije printout para permitir vista previa.
//se agrega UnBind.


//17-08-2005
// se modifica la creaci�n de la conexi�n por problemas al cerrar el documento.
// se modifico la basicmacrotools, se agregaro m�todo para ejecutar comando y macros sin par�metros.
// se agrega el m�todo spellcheck (que es un comando de basicmacrotools)

//11-08-2005
// se evaluan nuevamente y corrigen todos los codigos de error. Se agregaron nuevos c�digos.
// se crea la clase BasicMacroTools para la ejecuci�n de macros.
// se agrego 1 parametro al m�todo Open, sobrecargando, para recibir el nro de port y host donde recide el daemon de openoffice. 


//4-08-2005 - B@tero
//se agrega metodos print. falta adecuar el comportamiento a los par�metros.

// 2-08-2005 - B@tero
// Se corrigieron problemas en el replace
// se agregan metodos para manejo de errores. y se corrigen todos los metodos para trabajar en funcion a ellos
// se cambian las estructuras para adecuarse a las clases de genexus.
// se agrega metodos hide y show. pero solo funcionan si las ventanas ya fueron creadas durante el loadcomponentfromURL
 

// 1-08-2005 - B@tero
// Se agregaron ReadOnly
// Se agrega clase oofficeconnectionmanager, para administrar las conexiones a openoffice
// Se completa Replace, con todos sus par�metros.

// 29-07-2005 - B@tero
// Se agregaron los metodos SetText, Append, SaveAs y Replace
// SaveAs aun tiene problemas con filtros htm y dot
// Replace funciona solo con los 2 primeros parametros

package com.genexus.gxoffice.ooffice;

import java.io.File;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.document.MacroExecMode;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.util.XCloseable;
import com.sun.star.text.XText;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;
import com.sun.star.view.XPrintable;
import com.sun.star.frame.XFrame;
import com.genexus.gxoffice.ooffice.BasicMacroTools;
import com.genexus.gxoffice.IWordDocument;

/*
 * Created on 27/07/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author dvillagra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WordDocument implements IWordDocument{
	
    String fileName;
    public boolean ReadOnly = false;    
	private short errCod = 0;
	private String errDescription="OK"; 

	XComponentLoader xComponentLoader = null;
	XDesktop xDesktop = null;
	private XComponent xDoc;
	private XTextDocument aTextDocument = null;	
	
	public short Open(String wName, String wHost, short wPort)
	{
		
		if (OOfficeConnectionManager.connect(wHost, wPort))
		{
			xDesktop = OOfficeConnectionManager.getDesktop();
		}			
		else
		{
			errCod = 10;
			errDescription = "Could not complete operation.";
			return errCod;
		}
		
		//create an array of PropertyValue structs for loadComponentFromURL
        PropertyValue[] loadProps = new PropertyValue [3];
        loadProps[0] = new PropertyValue();
        loadProps[0].Name = "Hidden";
        loadProps[0].Value = new Boolean(false); 
        loadProps[1] = new PropertyValue();
        loadProps[1].Name = "ReadOnly";
        loadProps[1].Value = new Boolean(ReadOnly);        
        loadProps[2] = new PropertyValue();
        loadProps[2].Name = "MacroExecutionMode";
        loadProps[2].Value = new Short(MacroExecMode.ALWAYS_EXECUTE);        
        
		try	
		{	
			xComponentLoader =(XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class, xDesktop);
			
			if (new File(wName).exists())
			{		        
	        	String urlName = "file:///";        	
	        	urlName = urlName.concat(wName.replace('\\','/'));        	
	        	xDoc = xComponentLoader.loadComponentFromURL(urlName, "_blank", 0, loadProps);
	        	if (xDoc == null)
	        	{
	    			errCod = 6; //error creando doc file
	    			errDescription = "Could not open file.";	    			
	    		}
	        	else
	        	{
					errCod = 0;
					errDescription="OK";
	        	}
			}
			else
			{								
				xDoc = xComponentLoader.loadComponentFromURL("private:factory/swriter", "_blank", 0, loadProps);
				errCod = 0;
				errDescription="OK";				
			}
		}
		
		catch(Exception e)
		
		{						
			errCod = 6; //error creando xls file
			errDescription = "Could not open file.";			
			System.err.println("GXOffice Error: "+ e.toString());			
		}	
		
		fileName = wName;
		
        return errCod;
	}
	
	public short Open(String wName)
	{
		String ooHost = com.genexus.ModelContext.getModelContext().getPreferences().getProperty("OOfficeHost", "localhost");
		short ooPort = 8100;
		try
		{
			ooPort = Short.parseShort(com.genexus.ModelContext.getModelContext().getPreferences().getProperty("OOfficePort", "8100"));
		}
		catch(NumberFormatException nfe) { System.err.println("OpenOffice port in wrong format!"); }
		
		return Open(wName, ooHost, ooPort);
	}
		
  	public short Save()
  	{ 		
  		return SaveAs(fileName);	
  	}
  	
  	public short SaveAs(String Name)
  	{
		return SaveAs(Name, "DOC");
  	}
  
  	public short SaveAs(String Name, String Type)
  	{
		return SaveAs(Name, Type, (short) 0);
  	}

  	public short SaveAs(String Name, String Type, short DOSText)
  	{
		return SaveAs(Name, Type, DOSText, (short) 0);
  	}

  	public short SaveAs(String Name, String Type, short DOSText, short LineBreaks)
  	{
  		/**Store a document,using Filters */
  		/** DOSText o LineBreaks no soportados completamente */

  		if (!OOfficeConnectionManager.isConnected())
  		{
			errCod = 3;
			errDescription="Application no longer valid.";				
  			return errCod;
  		}
  		
  		if (xDoc == null)
  		{
			errCod = 2;
			errDescription="Document no longer valid.";				
  			return errCod;
  		}		
  		
  		XStorable xStorable = (XStorable)UnoRuntime.queryInterface(XStorable.class, xDoc);
  		
  		PropertyValue [] storeProps = new PropertyValue [1];
  		storeProps[0] = new PropertyValue(); 
  		storeProps[0].Name ="FilterName"; 		
  		
  		Type = Type.toUpperCase();
  		
  		if (Type.equalsIgnoreCase("DOC")) // (Word format)
		{
  			storeProps[0].Value = "MS Word 97";  			
		}
  		if (Type.equalsIgnoreCase("RTF")) // (Rich Text Format)
		{
  			storeProps[0].Value = "Rich Text Format";
		}
  		if (Type.equalsIgnoreCase("HTM")) // (HyperText Markup/ HyperText Markup Language))
		{
  			storeProps[0].Value = "HTML (StarWriter)";
		}
  		if (Type.equalsIgnoreCase("DOT")) // (DOC Template)
		{
  			storeProps[0].Value = "MS Word 97 Vorlage";
		}
  		if (Type.equalsIgnoreCase("TXT")) // (Texto)
		{
  			storeProps[0].Value = "Text";
		}   		
  		if (Type.equalsIgnoreCase("PDF")) // (Texto)
		{
  			storeProps[0].Value = "writer_pdf_Export";
		}   		

  		try {
  			
        	String urlName = "file:///";        	
        	urlName = urlName.concat(Name.replace('\\','/'));
  			xStorable.storeAsURL(urlName, storeProps);  			
  			setOK();
  			
  		} catch (Exception e) {
			errCod = 7;
			errDescription="Could not save file.";  			
			System.err.println("GXOffice Error: "+ e.toString());  			
  		}
  		
  		return errCod;	
  	}
  	
    public short Close()
    {
        try
		{
            XCloseable xCloseable = ( XCloseable ) UnoRuntime.queryInterface(
                    XCloseable.class, xDoc );

            if (xCloseable != null ) {            	
                xCloseable.close(false);
                OOfficeConnectionManager.release();
                setOK();                
            } 
            else
            {
                XComponent xComponent = ( XComponent ) UnoRuntime.queryInterface(
                    XComponent.class, xDoc );
                xComponent.dispose();
                OOfficeConnectionManager.release();
                setOK();
            }
        	
		}catch (Exception e)
		{
			errCod = 10;
			errDescription="Could not complete operation.";  			
			System.err.println("GXOffice Error: "+ e.toString());
			OOfficeConnectionManager.release();
		}
        return errCod;
    }
    
    public short Unbind()
    {
    	if (OOfficeConnectionManager.release())
    	{
    		errCod = 10;
    		errDescription="Could not complete operation.";    		
    	}
    	else
    	{
			errCod = 0;
			errDescription="OK";
    	}    	
    	return errCod;
    }

  	public void setText(String Text)
  	{  		
  		try {
	  		if (xDoc != null){
	  	  		aTextDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xDoc );  		
	  			XText xText = (XText)aTextDocument.getText();
	  			
	  	  		xText.setString(Text);
	  	  		setOK();
	  		}
	  		else
	  		{
				errCod = 2;
				errDescription="Document no longer valid.";				
	  		}
  		}
  		catch(Exception e)
		{
			errCod = 10;
			errDescription="Could not complete operation.";  			
			System.err.println("GXOffice Error: "+ e.toString());  			
		}
  	}
  	
  	public String getText()
  	{
  		try {
	  		if (xDoc != null){
        
		  		aTextDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xDoc );  		
				XText xText = (XText)aTextDocument.getText();		
				setOK();
				
		        return xText.getString();
	  		}
	  		else
	  		{
				errCod = 2;
				errDescription="Document no longer valid.";				
	  		}
  		}
  		catch(Exception e)
		{
			errCod = 10;
			errDescription="Could not complete operation.";  			
			System.err.println("GXOffice Error: "+ e.toString());			
		}        
  		return null;
  	}
  	
  	public short Append(String Text)
  	{	
  		if (xDoc != null)
  		{
	  		try
			{
	  	  		aTextDocument = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xDoc );  		
	  			XText xText = (XText)aTextDocument.getText();
	  			
	  			XTextRange xEnd = xText.getEnd();
	  			xEnd.setString(Text);
	  			
	  			setOK();
	  			
			}
	  		catch (Exception e)
			{
				errCod = 10;
				errDescription="Could not complete operation.";  			
				System.err.println("GXOffice Error: "+ e.toString());			
			}
  		}
  		else
  		{
			errCod = 2;
			errDescription="Document no longer valid.";				
  		}
  		return errCod;
  	}  	

  	public short Replace(String oldVal, String newVal)
	{
		return Replace(oldVal, newVal, (short) 0);
	}

  	public short Replace(String oldVal, String newVal, short MatchCase)
	{
		return Replace(oldVal, newVal, MatchCase, (short) 0);
	}

  	public short Replace(String oldVal, String newVal, short MatchCase, short MatchWholeWord)
  	{
  		Boolean caseSensitive; 
  		Boolean matchWords;
  		
  		if (xDoc == null)
  		{
			errCod = 2;
			errDescription="Document no longer valid.";		
  		}
  		else
  		{  		
	  		try
			{
	  			
		        XReplaceDescriptor xReplaceDescriptor = null;
		        XReplaceable xReplaceable = null;
		        	
		        xReplaceable = (XReplaceable) UnoRuntime.queryInterface(
		            com.sun.star.util.XReplaceable.class, xDoc );
		        
		        // You need a descriptor to set properies for Replace
		        xReplaceDescriptor = (XReplaceDescriptor) xReplaceable.createReplaceDescriptor();
		        
		        // Set the properties the replace method need
		        xReplaceDescriptor.setSearchString( oldVal );
		        xReplaceDescriptor.setReplaceString( newVal );        
		
				if (MatchCase == 1)		
				{
					caseSensitive = new Boolean(true);
				}
				else
				{
					caseSensitive = new Boolean(false);
				}
				
				if (MatchWholeWord == 1)		
				{
		        	matchWords = new Boolean(true);        				
				}
				else
				{
					matchWords = new Boolean(false);
				}
			
	        	xReplaceDescriptor.setPropertyValue("SearchWords", matchWords);
	        	xReplaceDescriptor.setPropertyValue("SearchCaseSensitive", caseSensitive);
	
	            // Replace all words
	            xReplaceable.replaceAll( xReplaceDescriptor );
	            
	            setOK();
	            
			} catch (Exception e)
			{
				errCod = 10;
				errDescription="Could not complete operation.";  			
				System.err.println("GXOffice Error: "+ e.toString());
			}
  		}
		
        return errCod;
  	}
  	
  	public void setReadOnly(short _jcomparam_0)
  	{
  		if (_jcomparam_0 == 1)
  		{
  			ReadOnly = true;
  		}
  		else 
  		{
  			ReadOnly = false;
  		}
  	}
	
    public short getReadOnly()
    {
    	if (ReadOnly == true)
    	{
    		return 1;
    	}
    	else
    	{
    		return 0;
    	}
    }
    public short Hide()
    {
    	if (xDesktop != null)
    	{
    		try
			{
    			xDesktop.getCurrentFrame().getContainerWindow().setVisible(false);	
			}
    		catch (Exception e)
			{
				errCod = 10;
				errDescription="Could not complete operation.";  			
				System.err.println("GXOffice Error: "+ e.toString());    			
			}    			
    	}
    	else
    	{
			errCod = 2;
			errDescription="Document no longer valid.";   		
    	}
    	
    	return errCod;
    }
    public short Show()
    {    	
    	if (xDesktop != null)
    	{
    		try
			{
    			xDesktop.getCurrentFrame().getContainerWindow().setVisible(true);
			}
    		catch (Exception e)
			{
				errCod = 10;
				errDescription="Could not complete operation.";  			
				System.err.println("GXOffice Error: "+ e.toString());    			
			}
    	}
    	else
    	{
			errCod = 2;
			errDescription="Document no longer valid.";   		
    	}
    	
    	return errCod;
   }
    
	public void setErrDisplay(short _jcomparam_0)
	{
		
	}
    public short getErrDisplay()
    {
    	return -1;
    }
    
	public short getErrCode()
	{
		return this.errCod;
	}
	
	public String getErrDescription()
	{
		return this.errDescription;
	}
	
	private void setOK()
	{
		errCod = 0;
		errDescription="OK";
	}	
	public void cleanup()
	{
		
	}
  	public short PrintOut(short Preview)
  	{
  		if (Preview == 1)
  		{
  			return RunCmd("PrintPreview"); 
  		}
  		else
  		{
  			return PrintOut(Preview, (short) 0);	
  		}
  	}

  	public short PrintOut()
  	{
		return PrintOut((short) 0, (short) 0);
  	}

  	public short PrintOut(short Preview, short Background)
  	
  	{	
	    // Querying for the interface XPrintable on the loaded document
	    XPrintable xprintable =
	    ( XPrintable ) UnoRuntime.queryInterface( XPrintable.class, xDoc );
	    
	    // Setting the property "Name" for the favoured printer (name of IP address)
	    PropertyValue []propertyvalue = new PropertyValue[0]; // empty array - all default values
	    try
		{
	    	// Printing the loaded document
	    	xprintable.print( propertyvalue );
	    	setOK();
		}
	    catch (Exception e)
		{
			errCod = 10;
			errDescription="Could not complete operation.";  			
			System.err.println("GXOffice Error: "+ e.toString());	    	
		}
	    return errCod;
  	}
  	
  	public short RunMacro(String MacroName)
  	{  		
  		return RunMacro(MacroName, "");
  	}  	
  	
  	public short RunMacro(String MacroName, Object[] MacroParms)
  	{  	
  		String xMacroParams = "";
  		
  		for (int i = 0; i < MacroParms.length; i++){
  			xMacroParams = xMacroParams.concat(MacroParms[i].toString() + ",");  			
  		}  		
  		
  		return RunMacro(MacroName, xMacroParams);
  	}  	

  	private short RunMacro(String MacroName, String Params)
  	{
  		XMultiServiceFactory msf = OOfficeConnectionManager.getXMultiServiceFactory();
  		XFrame xFrame = OOfficeConnectionManager.getCurrentXFrame();  		
  		BasicMacroTools xMacro = null;

  		String xMacroName = MacroName + "(" + Params + ")";
  		
  		try {
  			xMacro = new BasicMacroTools(msf, xFrame, xDoc);
  			if (xMacro.runMacro(xMacroName))
  			{
  				errCod = 0;
  				errDescription="OK";  				  				
  			}
  			else
  			{
  				errCod = 9;
  				errDescription="Error running macro.";
  			} 			
  		}  		
  		catch (Exception e)
		{
			errCod = 9;
			errDescription="Error running macro.";  			
			System.err.println("GXOffice Error: "+ e.toString());	    	
		}
  		return errCod;
  	}
  	
  	public short SpellCheck()
  	{  		
  		return RunCmd("Spelling");
  	}
  	
  	private short RunCmd(String Command)
  	{
  		XMultiServiceFactory msf = OOfficeConnectionManager.getXMultiServiceFactory();
  		XFrame xFrame = OOfficeConnectionManager.getCurrentXFrame();  		
  		BasicMacroTools xCommand = null;
  		
  		try {
  			xCommand = new BasicMacroTools(msf, xFrame, xDoc);  			
  			if (xCommand.runCommand(Command))
  			{
  				errCod = 0;
  				errDescription="OK";
  			}
  			else
  			{
  				errCod = 10;
  				errDescription="Could not complete operation.";  				  				
  			}
  			
  		}  		
  		catch (Exception e)
		{
			errCod = 10;
			errDescription="Could not complete operation.";  			
			System.err.println("GXOffice Error: "+ e.toString());			
		} 
  		return errCod;  		
  	}
  	public void setTemplate(String _jcomparam_0)
  	{
  		
  	}
    
  	public String getTemplate()
  	{
  		return "";
  	}
  	
}
