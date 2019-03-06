// $Log: GXReorganization.java,v $
// Revision 1.5  2005/08/26 21:24:13  aaguiar
// - Los progs de manejo de redundancias llamaban a la addMsg que no tenia instanciado el Log,
//   con lo cual se caia la app
//
// Revision 1.4  2005/05/10 17:34:01  gusbro
// - Al finalizar una reorg hago un commit sino no funcionan bien los drivers de inet
//
// Revision 1.3  2005/02/16 20:52:17  iroqueta
// Se estaba tomando mal el nombre de la base de datos en una reorg con db2 si habia mas de un datasource.
//
// Revision 1.2  2004/04/13 22:50:48  gusbro
// - Agrego metodo getMainDBName utilizado en reorgs con AS400
//
// Revision 1.1.1.1  2002/04/17 19:52:44  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/04/17 19:52:44  gusbro
// GeneXus Java Olimar
//
//
//   Rev 1.6   23 Sep 1998 19:48:24   AAGUIAR
//
//   Rev 1.5   23 Jul 1998 18:58:56   AAGUIAR
//	- Se sacan los mensajes del resource file.
//
//   Rev 1.4   Jun 02 1998 15:16:04   AAGUIAR
//	-	Se cambio mensaje que se despliega cuando no se
//		hace una reorg por la preference de reorganize
//		server tables = No.
//
//   Rev 1.3   Jun 01 1998 18:07:14   AAGUIAR
//	-	Se cambiaron las lecturas de preferences, para
//		adaptarlas al nuevo modelo.
//
//   Rev 1.2   May 29 1998 10:05:12   AAGUIAR
//	-	Se cambiaron las lecturas de preferences, para
//		adaptarlas al nuevo modelo.
//
//   Rev 1.1   May 28 1998 10:00:38   DMENDEZ
//Sincro28May1998

package com.genexus;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import com.genexus.util.*;
import com.genexus.db.*;
import com.genexus.platform.INativeFunctions;
import com.genexus.platform.NativeFunctions;

public abstract class GXReorganization
{

	protected File reorganizationFlag;
	protected boolean working = false;
	protected boolean autoCommit = true;
	int handle;

	private static final String replyIgnore = "CALL QSYS/QCMDEXC('QSYS/CHGJOB INQMSGRPY(*SYSRPYL)',0000000031.00000)";
	
	private static final String ReoFlagGen = "REORGPGM.GEN";
	protected static final String ReoFlagExp = "REORGPGM.EXP";
	
	public abstract void execute();
	public abstract String getPackageDir();
	
	private GXProcedure reorgProcedure;

	protected static Messages msg;
	protected ModelContext context;

	public GXReorganization(Class gxCfg)
	{
		ApplicationContext.getInstance().setReorganization(true);
		Application.init(gxCfg);
		ServerPreferences.fileName = "reorg.cfg";

		context = new ModelContext(gxCfg);
		handle = Application.getConnectionManager().createUserInformation(Namespace.getNamespace(context.getNAME_SPACE())).getHandle();

		msg = Application.getConnectionManager().getUserInformation(handle).getLocalUtil().getMessages();
		
/*
		try
{
	System.in.read();
}catch(Throwable ee){;}
System.err.println("Sigo...");
*/		
	}
	
	public static String getMainDBName(ModelContext context, int handle)
	{
		UserInformation ui = Application.getConnectionManager().getUserInformation(handle);
		return ui.getNamespace().getDataSource("DEFAULT").jdbcDBName;
	}

	protected void cleanup()
	{
		Application.exitApplet();
	}
	protected int getHandle()
	{
		return handle;
	}

	private static boolean force = false;
	private static boolean recordcount = false;
	private static boolean ignoreresume = false;
	private static boolean noprecheck = false;
	private static boolean notexecute = false;

	private void processParameters(String args[])
	{
		int i = 0;
		while (i < args.length)
		{
			if	(args[i].startsWith("-"))
			{
				if	(args[i].toLowerCase().startsWith("-force"))
				{
					force = true;
				}
				if (args[i].toLowerCase().startsWith("-recordcount"))
				{
					recordcount = true;
				}
				if (args[i].toLowerCase().startsWith("-ignoreresume"))
				{
					ignoreresume = true;
				}
				if (args[i].toLowerCase().startsWith("-noverifydatabaseschema"))
				{
					noprecheck = true;
				}				
				if (args[i].toLowerCase().startsWith("-donotexecute"))
				{
					notexecute = true;
				}				
			}
			else
			{
				break;
			}
			i++;
		}
	}
	
	public static boolean getRecordCount()
	{
		return recordcount;
	}
	
	public static void printRecordCount(String tableName, int recordCount)
	{
		if (!executingResume)
		{
			addMsg(msg.getMessage("GXM_table_recordcount", new Object[] { tableName, new Integer(recordCount) }));
			//addMsg("Table " + tableName + " has " + recordCount + " records.");
		}
	}	

	boolean doReorganization;
	public void executeReorg(String args[], boolean isCreateDataBase)
	{
		createDataBase = isCreateDataBase;
		processParameters(args);
		if (notexecute)
		{
			addMsg(msg.getMessage("GXM_dbnotreorg"));
		}
		else
		{
				ApplicationContext.getInstance().setMsgsToUI(false);

			executeReorg();
		}
	}
	
	public boolean ExecDataInitialization()
	{
		boolean isOK = true;
		try
		{
			if(reorgProcedure != null)
			{
				Class reorgClass = reorgProcedure.getClass();
				Method method = reorgClass.getMethod("ExecDataInitialization", new Class[]{});
				if(method != null)
				{
					method.invoke(reorgProcedure, (Object[])null);
				} 
			}
		}
		catch(NoSuchMethodException ex)
		{
		}		
		catch(Exception ex)
		{
				isOK = false;
				ex.printStackTrace();			
		}
		return isOK;
	}
	
	protected void setReorgProcedure(GXProcedure reorgProcedure)
	{
		this.reorgProcedure = reorgProcedure;
	}

	protected void executeReorg()
	{
		reorganizationFlag = new File(ReoFlagGen );

		doReorganization = Application.getClientContext().getClientPreferences().getCS_REORGJAVA();
      
		// Esto es para que los procs de reorg no terminen con System.exit
      	Application.realMainProgram = this;

		if	(!force)
		{
			returnValue = true;
			NativeFunctions.getInstance().executeWithPermissions(new ReorgEnabled1(), INativeFunctions.FILE_READ);

			if	(!returnValue)
				return;
		}

			if (doReorganization)
			{
				if (!recordcount)
				{
					beginResume();
					if (!inavlidResumeVersion)
					{
						try
						{
							processExternalScript("beforeReorganizationScript.txt");
						}
						catch(Exception ex)
						{
						}
					}
				}
				if(!inavlidResumeVersion)
				{
					try
					{
						execute();
					}
					catch(GXRuntimeException e)
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				addMsg(msg.getMessage("GXM_dbnotreorg"));
				addMsg(msg.getMessage("GXM_reorgpref"));
			}

			ReorgSubmitThreadPool.waitForEnd();
			if	(success() && !ReorgSubmitThreadPool.hasAnyError())
			{
				boolean isOK = true;
				if (!recordcount)
				{
					isOK = ExecDataInitialization();
					deleteResumeFile();
					try
					{
						processExternalScript("afterReorganizationScript.txt");
					}
					catch(Exception ex)
					{
					}
					if (isOK)
						addMsg(msg.getMessage("GXM_reorgsuccess"));
					else
						addMsg(msg.getMessage("GXM_reorgnotsuccess"));
				}
				Application.commit(context, getHandle(), "DEFAULT", null, "GXReorganization");
				if (!isOK)
					System.exit(2);
			}
			else
			{
				addMsg(msg.getMessage("GXM_reorgnotsuccess"));
				System.exit(1);
			}

			System.exit(0);
	}

	class ReorgEnabled1 implements Runnable
	{
		public void run()
		{
			
			if	(!reorganizationFlag.exists())
			{
				msg(msg.getMessage("GXM_noreorg"));
				returnValue = false;
				cleanup();
				return;
			}
		}
	}

	private void msg(String text)
	{
		System.out.println("! " + text);
	}

	class ProcessFiles implements Runnable
	{
		public void run()
		{
			if (new File(ReoFlagExp).exists())
			{
				new File(ReoFlagExp).delete();
			}

			if	(!reorganizationFlag.renameTo(new File("REORGPGM.EXP")))
			{
				addMsg(msg.getMessage("GXM_reorgrenre"));
				returnValue = false;
				return;
			}
    	}
	}

	protected boolean returnValue;
	public boolean success()	
	{
		if (checkError)
		{
			addMsg(msg.getMessage("GXM_error_in_schema_verification"));
			//addMsg("An error was found in the database schema verification process.");
			addMsg(errorMessage);
			deleteResumeFile();
			return false;
		}
		
		if (inavlidResumeVersion)
		{
			return false;
		}
		
		returnValue = true;

		if	(!force)
		{
			//addMsg(msg.getMessage("reorgupdgxdb"));

			NativeFunctions.getInstance().executeWithPermissions(new ProcessFiles(), INativeFunctions.FILE_ALL);
		}

		return returnValue;
	}

	public static void addMsg(String msg)
	{
			System.out.println(msg);
	}
	
	public static void addMsg(int index, String msg)
	{
		System.out.println(msg);	
	}
	
	public static void replaceMsg(int index, String msg)
	{
			System.out.println(msg);
	}
	
   private void processExternalScript(String fileName) throws Exception
   {
	   try
	   {
		   BufferedReader input = new BufferedReader( new FileReader(fileName) );
		   String line = readSentence(input);
		   while (!line.equals(""))
		   {
			   if (!executedBefore(line))
			   {
				   addMsg(msg.getMessage("GXM_executing", new Object[] { line }));
				   //addMsg("Executing " + line);
				   ExecuteDirectSQL.executeWithThrow(context, getHandle(), "DEFAULT", line) ;
			   }
			   line = readSentence(input);
		   }
		   
	   }
	   catch(FileNotFoundException ex)
	   {
	   }
   }
   
	private String readSentence(BufferedReader is) throws Exception
	{
		StringBuffer result = new StringBuffer();
		boolean inLiteral = false;
		int caracter = is.read();
		while (caracter != ';' || inLiteral)
		{
			if (caracter == -1)
			{
				return "";
			}			
			if (caracter != (char)13 && caracter != (char)10)
			{
				if (caracter == '"')
				{
					inLiteral = !inLiteral;
				}
				result.append( (char) caracter );
			}
			caracter = is.read();
		}
		return result.toString();
	}
	
	//Implementaciones para la retoma
	private static final String resumeFileName = "resumereorg.txt";
	private static Vector executedStatements = new Vector();
	private static boolean executingResume = false;
	private boolean inavlidResumeVersion = false;
	private static boolean createDataBase = false;
	
	public static void setCreateDataBase()
	{
		createDataBase = true;
	}
	
	private void beginResume()
	{
		try
		{
			if (createDataBase || ignoreresume)
			{
				try
				{
					new File(resumeFileName).delete();
				}
				catch(Exception e)
				{
				}				
			}
			
			FileReader reader = new FileReader(resumeFileName);
			BufferedReader input = new BufferedReader(reader);
			String statement = input.readLine();
			if (statement != null)
			{
				if (!statement.equals(Application.getClientContext().getClientPreferences().getREORG_TIME_STAMP()))
				{
					inavlidResumeVersion = true;
					addMsg(msg.getMessage("GXM_lastreorg_failed1"));
					//addMsg("The last reorganization has failed and you are trying to execute a different reorganization.");
					addMsg(msg.getMessage("GXM_lastreorg_failed2"));
					//addMsg("Unexpected errors may occur if you don't try to finalize previous reorganization before running this one.");
					addMsg(msg.getMessage("GXM_lastreorg_failed3"));
					//addMsg("If you want to run this reorganization anyway, use 'ignoreresume' parameter.");
					input.close();
					return;
				}				
			}
			while (statement != null)
			{
				executedStatements.addElement(statement);
				statement = input.readLine();
			}
			input.close();
			reader.close();
			executingResume = true;
		}
		catch(FileNotFoundException fnfe)
		{
		}
		catch(IOException ioe)
		{
		}
		finally
		{
			serializeExecutedStatements();
		}
	}
	
	public static boolean executedBefore(String statement)
	{
		if (executingResume)
		{
			return executedStatements.contains(statement);
		}
		
		return false;
	}
	
	static Object lock = new Object();
	
	public static void addExecutedStatement(String statement)
	{
		//Si la sentencia es la que se usa para ignorar el mensaje de drop column en Iseries no se escribe en el archivo de resume
		if (statement.trim().startsWith(replyIgnore))
		{
			return;
		}
					
		if (!recordcount) //Si estoy con recordCount entonces no tengo que poner nada en el archivo de retoma.
		{
			synchronized (lock)
			{
				try
				{
					output.write(statement);
					output.write(newline);
					output.flush();
				}
				catch(IOException ioe)
				{
				}		
			}
		}
	}
		
	private static FileWriter output;
	private static final String newline = CommonUtil.newLine( );
	
	private static void serializeExecutedStatements()
	{
		try
		{
			output = new FileWriter(resumeFileName, true);
			if (!executingResume)
			{
				output.write(Application.getClientContext().getClientPreferences().getREORG_TIME_STAMP());
				output.write(newline);
				output.flush();				
			}
		}
		catch(IOException ioe)
		{
		}
	}
	
	private void deleteResumeFile()
	{
		try
		{
			output.close();
			new File(resumeFileName).delete();
		}
		catch(Exception e)
		{
		}
	}
	
	//Implementaciones para el precheck
	private static boolean checkError = false;
	private static String errorMessage;
	
	public static void setCheckError(String checkErrorMessage)
	{
		errorMessage = checkErrorMessage;
		checkError = true;
	}
	
	public static boolean isResumeMode()
	{
		return executingResume;
	}
	
	public static boolean mustRunCheck()
	{
		return(!executingResume && !noprecheck);
	}
	
	public static String getSchemaName()
	{
		String schema;
		String section = Application.getClientPreferences().getNAME_SPACE() + "|DEFAULT";
		schema = Application.getClientPreferences().getIniFile().getProperty(section, "CS_SCHEMA", "");
		if (schema.equals(""))
			return Application.getClientPreferences().getIniFile().getPropertyEncrypted(section, "USER_ID","");
		else
			return schema;
	}
	
}



