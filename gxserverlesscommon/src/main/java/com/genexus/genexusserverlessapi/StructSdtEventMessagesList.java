package com.genexus.genexusserverlessapi;

import com.genexus.ModelContext;
import java.io.Serializable;
import java.util.Vector;

public final class StructSdtEventMessagesList implements Cloneable, Serializable {
	protected byte gxTv_SdtEventMessagesList_Items_N;
	protected byte sdtIsNull;
	protected Vector gxTv_SdtEventMessagesList_Items;

	public StructSdtEventMessagesList() {
		this(-1, new ModelContext(StructSdtEventMessagesList.class));
	}

	public StructSdtEventMessagesList(int remoteHandle, ModelContext context) {
		this.gxTv_SdtEventMessagesList_Items = null;
		this.gxTv_SdtEventMessagesList_Items_N = 1;
	}

	public Object clone() {
		Object cloned = null;

		try {
			cloned = super.clone();
		} catch (CloneNotSupportedException var3) {
		}

		return cloned;
	}

	public Vector getItems() {
		return this.gxTv_SdtEventMessagesList_Items;
	}

	public void setItems(Vector value) {
		this.gxTv_SdtEventMessagesList_Items_N = 0;
		this.sdtIsNull = 0;
		this.gxTv_SdtEventMessagesList_Items = value;
	}
}
