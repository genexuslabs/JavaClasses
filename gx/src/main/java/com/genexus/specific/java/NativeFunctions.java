package com.genexus.specific.java;

import com.genexus.common.interfaces.IExtensionNativeFunctions;
import com.genexus.platform.INativeFunctions;
import com.genexus.util.IThreadLocal;
import com.genexus.util.IThreadLocalInitializer;

public class NativeFunctions implements IExtensionNativeFunctions {

	@Override
	public IThreadLocal newThreadLocal(IThreadLocalInitializer initializer) {
		return new InheritableThreadLocalSun(initializer);
	}
	
	class InheritableThreadLocalSun extends ThreadLocal implements com.genexus.util.IThreadLocal
	{	
		private IThreadLocalInitializer initializer;

		public InheritableThreadLocalSun()
		{ 
			
		}
		
		public InheritableThreadLocalSun(IThreadLocalInitializer initializer)
		{
			this.initializer = initializer;
		}
		
		public Object get()
		{
			Object obj = super.get();
			if(obj == null && initializer != null)
			{
				set(initializer.initialValue());
				return super.get();
			}
			return obj;
		}		
	}  
	
	@Override
	public INativeFunctions getInstance() {
		return com.genexus.platform.NativeFunctions.getInstance();
	}  


}
