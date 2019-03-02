// $Log: GXThreadLocal.java,v $
// Revision 1.1  2005/05/25 16:41:11  gusbro
// Release Inicial
//
//

package com.genexus.util;
import com.genexus.common.interfaces.SpecificImplementation;

public class GXThreadLocal
{
	private GXThreadLocal(){ ; }
	
	/** Retorna una instancia de ThreadLocal
	 * @return IThreadLocal
	 */
	public static IThreadLocal newThreadLocal()
	{
		return SpecificImplementation.NativeFunctions.newThreadLocal(null);
	}

	/** Retorna una instancia de ThreadLocal
	 * @param initializer instancia que crea el objeto inicial asociado sl ThreadLocal
	 * @return IThreadLocal
	 */
	public static IThreadLocal newThreadLocal(IThreadLocalInitializer initializer)
	{
		return SpecificImplementation.NativeFunctions.newThreadLocal(initializer);
	}
}
