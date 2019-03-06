package com.genexus.specific.java;

import com.genexus.GxSilentTrnSdt;
import com.genexus.IGxSilentTrn;
import com.genexus.common.interfaces.IExtensionGXSilentTrnSdt;
import com.genexus.common.interfaces.IPendingEventHelper;

public class GXSilentTrnSdt implements IExtensionGXSilentTrnSdt {

	@Override
	public IPendingEventHelper CreatePendingEventHelper() {
		// TODO Auto-generated method stub
		return new DummyPendingEventHandler();
	}
	
	class DummyPendingEventHandler implements IPendingEventHelper {

		@Override
		public void prePendingEvents(Object trn, Object t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void postPendingEvents(Object trn, Object t) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
