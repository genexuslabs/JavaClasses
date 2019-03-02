// $Log: ServerConfig.java,v $
// Revision 1.4  2005/05/13 15:27:13  gusbro
// - Cambios para soportar GxUtils.jar
//
// Revision 1.3  2002/12/30 18:54:31  aaguiar
// - Se saco un println
//
// Revision 1.2  2002/12/04 21:44:25  aaguiar
// - Se pasa el nombre del ini por referencia
//
// Revision 1.1.1.1  2001/10/30 19:33:10  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/10/30 19:33:10  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import com.genexus.util.*;

public class ServerConfig 
{
	public static void execute()
	{
		main(new String[] {});
	}

	public static void main(String arg[])
	{
		ClientContext.setModelContext(new ModelContext(ServerConfig.class));
		ApplicationContext.getInstance().setGXUtility(true);

		String[] fileName = new String[1];

		fileName[0] = (arg.length == 0)?"server.cfg":arg[0];
		
		GxUtilsLoader.runServerConfig(fileName);
	}
}
