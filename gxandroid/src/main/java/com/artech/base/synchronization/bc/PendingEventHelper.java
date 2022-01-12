package com.artech.base.synchronization.bc;

import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import com.artech.base.services.AndroidContext;
import com.artech.base.services.IGxBusinessComponent;
import com.genexus.GxSilentTrnSdt;
import com.genexus.IGxSilentTrn;

import json.org.json.JSONArray;

public class PendingEventHelper {

	public String mBCName = "";
	public String mAction = "";
	public String mData = "";
	
	private String mDataOlds = "";
	private JSONArray mArrayFilesToSend = new JSONArray();
	private TreeMap<String, String> mMapFilesToSend = new TreeMap<String, String>();
	   


	
	public void preSaveEvent(GxSilentTrnSdt parent, IGxSilentTrn trn)
	{
		if (parent instanceof IGxBusinessComponent)
		{
			mBCName = ((IGxBusinessComponent)parent).getbcname();
		}
		else
		{
			mBCName = parent.getClass().getName();
		}
		mAction = trn.GetMode();
		mData = parent.toJSonString(false);
		mDataOlds = parent.toJSonString(true);
		
		if (!mAction.equalsIgnoreCase("dlt"))
			AndroidContext.ApplicationContext.getSynchronizationHelper().processBCBlobsBeforeSaved(mBCName, mData, mDataOlds, mMapFilesToSend);
		
	}
	
	public void postSaveEvent(GxSilentTrnSdt parent, IGxSilentTrn trn)
	{
		if (parent.Success())
		{
			// copy the data again , because save could change it.
			mData = parent.toJSonString(false);
			mDataOlds = parent.toJSonString(true);
			
			if (!mAction.equalsIgnoreCase("dlt"))
				mData = AndroidContext.ApplicationContext.getSynchronizationHelper().replaceBCBlobsAfterSave(mBCName, mAction, mData, mDataOlds, mMapFilesToSend, mArrayFilesToSend);

			saveEvent(mBCName, mAction, mData, mArrayFilesToSend);
		}
	}
	
	public void saveEvent(String bcName, String action, String data, JSONArray arrayFilesToSend )
	{
		//Only Save if Save Pending Events is enable.
		if (AndroidContext.ApplicationContext.getSynchronizerSavePendingEvents())
		{
		
			SdtGxPendingEvent sdtTrn = new SdtGxPendingEvent(AndroidContext.ApplicationContext.getRemoteHandle());
				
			// set a new GUID for the table id
			sdtTrn.setgxTv_SdtGxPendingEvent_Pendingeventid(java.util.UUID.randomUUID());
			
			//now in UTC
			Date nowDate = new Date();
			long offset = TimeZone.getDefault().getOffset(nowDate.getTime());
			nowDate.setTime(nowDate.getTime() - offset );

			sdtTrn.setgxTv_SdtGxPendingEvent_Pendingeventtimestamp(nowDate);
			//	Bc name
			sdtTrn.setgxTv_SdtGxPendingEvent_Pendingeventbc(bcName);
			//action - Ins, Upd, Dlt
			short mode = 1;
			if (action.equalsIgnoreCase("upd"))
				mode = 2;
			else if (action.equalsIgnoreCase("dlt"))
				mode = 3;
			sdtTrn.setgxTv_SdtGxPendingEvent_Pendingeventaction(mode);

			sdtTrn.setgxTv_SdtGxPendingEvent_Pendingeventdata(data); //json
			sdtTrn.setgxTv_SdtGxPendingEvent_Pendingeventstatus((short)1); //pendiente
			sdtTrn.setgxTv_SdtGxPendingEvent_Pendingeventfiles(arrayFilesToSend.toString()); // array of files	
			
			if (sdtTrn.getTransaction()!=null)
			{
				try
				{
					// save event
					sdtTrn.getTransaction().Save();
				}
				catch (Exception ex)
				{
					// if fail just print stack trace for now.
					ex.printStackTrace();
				}
			}
		}
	}
}
