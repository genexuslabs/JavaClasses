package com.genexus.util;

import java.io.File;
import java.util.Vector;

import com.genexus.*;
import com.genexus.xml.XMLReader;

public class GXDataInitialization extends GXProcedure {
	static String packagePath = "";
	static final String GXCFG = "GXcfg";
	public static void main(String arg[]) {
		if (arg.length > 1)
			packagePath = arg[1];
		try
		{
			if (!packagePath.equals(""))
			{
				if	(!packagePath.endsWith("."))
				{
					packagePath += ".";
				}
				Preferences.defaultResourceClass = Class.forName(packagePath + GXCFG);
				Application.init(Class.forName(packagePath + GXCFG)); 
			}
			else
			{
				Application.init(Class.forName(GXCFG)); 
			}
			Application.executingGeneratorTool = true;

			GXDataInitialization pgm = new GXDataInitialization(-1);
			Application.realMainProgram = pgm;
			String dynTrnInitializerFile;
			if (arg.length > 0)
				dynTrnInitializerFile = arg[0];
			else
				dynTrnInitializerFile = "DynTrnInitializers.xml";

			int result = pgm.execDataInitialization(dynTrnInitializerFile);
			if (result == 0)
			{
				File file = new File(dynTrnInitializerFile);
				file.delete();
			}

			System.exit(result);
		}
		catch(Throwable ex)
		{
			ex.printStackTrace();
			System.out.println(ex.toString());
			System.exit(1);
		}

	}

	public GXDataInitialization( int remoteHandle )
	{
		super( remoteHandle , new ModelContext( GXDataInitialization.class ), "" );
	}

	public int execDataInitialization(String dynTrnInitializersFile) {

		XMLReader reader = new XMLReader();
		reader.open(dynTrnInitializersFile);
		short ok = reader.readType(1, "Object");
		Vector<String> lst = new Vector<String>();
		while (ok == 1) {
			lst.add((String) reader.getAttributeByName("Name"));
			ok = reader.readType(1, "Object");
		}
		reader.close();

		try {
			for (String fullName : lst) {

				System.out.println(localUtil.getMessages().getMessage("GXM_runpgm", new Object[] {fullName}));
				 
				Class<?> dpClass = Class.forName(packagePath + fullName.toLowerCase());
				Object dp = dpClass.getConstructor(new Class<?>[] {int.class, ModelContext.class}).newInstance(new Object[] {new Integer(remoteHandle), context});
				Class[] parameterTypes = new Class[] {};
				Object[] arguments = new Object[] {};
				GXBCCollection bcs = (GXBCCollection) dp.getClass().getMethod("executeUdp", parameterTypes).invoke(dp, arguments);

				if (!bcs.insertOrUpdate()) {
					int idx = 1;
					while (idx <= bcs.size()) {
						GxSilentTrnSdt bc = ((GxSilentTrnSdt) bcs.elementAt(-1 + idx));
						if (!bc.Success()) {
							System.out.println(MessagesToString(bc.GetMessages()));
						} 
						idx = (int) (idx + 1);
					}
				}
			}
			cleanup();
			return 0;
		} catch (Exception e) {
			System.out.println("ERROR:" + e.getMessage());
			e.printStackTrace();
			return 1;
		}
	}

	protected void cleanup() {
		Application.commit(context, remoteHandle, "DEFAULT", null,"GXDataInitialization");
		exitApplication();
	}

	public void initialize( )
	{
	}

	public static String MessagesToString(GXBaseCollection<SdtMessages_Message> messages)
	{
		StringBuilder str = new StringBuilder();
		for (SdtMessages_Message msg : messages)
		{
			str.append(String.format("%s:%s  Code: %s%s  Description: %s%s", msg.getgxTv_SdtMessages_Message_Type() == 1 ? "Error" : "Warning", CommonUtil.newLine(), msg.getgxTv_SdtMessages_Message_Id(), CommonUtil.newLine(), msg.getgxTv_SdtMessages_Message_Description( ), CommonUtil.newLine()));
		}
		return str.toString();
	}
}
