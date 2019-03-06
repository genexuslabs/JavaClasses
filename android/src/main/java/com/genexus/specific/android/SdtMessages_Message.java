package com.genexus.specific.android;

import com.artech.base.services.IEntity;
import com.genexus.CommonUtil;
import com.genexus.GXutil;
import com.genexus.common.interfaces.IExtensionSdtMessages_Message;

public class SdtMessages_Message implements IExtensionSdtMessages_Message {

	@Override
	public void sdttoentity(Object sdtObj, Object entity) {
		com.genexus.SdtMessages_Message sdt = (com.genexus.SdtMessages_Message) sdtObj;
		IEntity androidEntity = (IEntity) entity;
			/*  Save BC members to entity  */
	      androidEntity.setProperty("Id", CommonUtil.trim( sdt.getgxTv_SdtMessages_Message_Id()));
	      androidEntity.setProperty("Type", CommonUtil.trim( GXutil.str( sdt.getgxTv_SdtMessages_Message_Type(), 2, 0)));
	      androidEntity.setProperty("Description", CommonUtil.trim( sdt.getgxTv_SdtMessages_Message_Description()));
	 }


	@Override
	public void entitytosdt(Object entity, Object sdtObj) {
		IEntity androidEntity = (IEntity) entity;
		com.genexus.SdtMessages_Message sdt = (com.genexus.SdtMessages_Message) sdtObj;

		/*  Copy entity values to BC  */
	      sdt.setgxTv_SdtMessages_Message_Id( (String) androidEntity.optStringProperty("Id"));
	      sdt.setgxTv_SdtMessages_Message_Type( (byte) CommonUtil.val( androidEntity.optStringProperty("Type"), "."));
	      sdt.setgxTv_SdtMessages_Message_Description( (String) androidEntity.optStringProperty("Description"));
	}

}
