package com.genexus.specific.android;

import com.genexus.common.interfaces.IExtensionNativeFunctions;
import com.genexus.platform.INativeFunctions;
import com.genexus.util.IThreadLocal;
import com.genexus.util.IThreadLocalInitializer;

public class NativeFunctions implements IExtensionNativeFunctions {
	
	@Override
	public IThreadLocal newThreadLocal(IThreadLocalInitializer initializer) {
		return new InheritableThreadLocalSun(initializer);
	}
	
	class InheritableThreadLocalSun extends InheritableThreadLocal implements com.genexus.util.IThreadLocal
	{	
		public InheritableThreadLocalSun(){ ; }
		
		IThreadLocalInitializer initializer;
		public InheritableThreadLocalSun(IThreadLocalInitializer initializer)
		{
			this.initializer = initializer;
		}
		
		public Object childValue(Object parentValue)
		{
			if(initializer != null)
			{
				return null; 
			}
			else 
			{
				return super.childValue(parentValue);
			}
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
