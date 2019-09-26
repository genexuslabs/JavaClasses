package com.genexus.specific.android;

import com.artech.base.synchronization.bc.PendingEventHelper;
import com.genexus.IGxSilentTrn;
import com.genexus.common.interfaces.IExtensionGXSilentTrnSdt;
import com.genexus.common.interfaces.IPendingEventHelper;

public class GXSilentTrnSdt implements IExtensionGXSilentTrnSdt {

	@Override
	public IPendingEventHelper CreatePendingEventHelper() {
		// TODO Auto-generated method stub
		return new PendingEventHelperImpl();
	}

	class PendingEventHelperImpl implements IPendingEventHelper
	{
		PendingEventHelper pendingHelper;

		@Override
		public void prePendingEvents(Object parent, Object t) {
			pendingHelper = new PendingEventHelper();
			pendingHelper.preSaveEvent((com.genexus.GxSilentTrnSdt) parent,(IGxSilentTrn) t);
			
		}

		@Override
		public void postPendingEvents(Object parent, Object t) {
			  if (pendingHelper!=null)
			   {
				   pendingHelper.postSaveEvent((com.genexus.GxSilentTrnSdt) parent,(IGxSilentTrn) t);
			   }
		}
	
	}

}


