/*
 * Created on 01/07/2005
 * 
 * 13/7 Willy:
 *			Cambio de package
 * 12/6 Willy: entran los fuentes al svn
 */
package com.genexus.gxoffice.poi.hssf;

import java.util.Hashtable;
import org.apache.poi.hssf.usermodel.*;

/**
 * @author Administrador
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ColorManager {
	private static Hashtable colors=new Hashtable();
	public static int getColor(HSSFWorkbook wb)
	{
		if(!colors.containsKey(wb))
			colors.put(wb,new Integer(17));
		
		int n=((Integer)colors.get(wb)).intValue();
		n++;
		colors.remove(wb);
		colors.put(wb,new Integer(n));
		return n;
	}
	public static void cleanup(HSSFWorkbook wb)
	{
		if (wb != null)
			if(colors.containsKey(wb))
				colors.remove(wb);
	}
}