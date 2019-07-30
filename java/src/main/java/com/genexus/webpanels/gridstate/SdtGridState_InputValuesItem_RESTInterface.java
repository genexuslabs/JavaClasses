package com.genexus.webpanels.gridstate ;
import javax.xml.bind.annotation.*;
import com.fasterxml.jackson.annotation.*;
import com.genexus.GXutil;
import com.genexus.GxGenericCollectionItem;

@XmlType(name =  "GridState.InputValuesItem" , namespace = "GeneXus" , propOrder={ "name", "value" })
@JsonPropertyOrder(alphabetic=true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE, getterVisibility=JsonAutoDetect.Visibility.NONE, isGetterVisibility=JsonAutoDetect.Visibility.NONE)
public final  class SdtGridState_InputValuesItem_RESTInterface extends GxGenericCollectionItem<SdtGridState_InputValuesItem> {
	public SdtGridState_InputValuesItem_RESTInterface() {
		super(new SdtGridState_InputValuesItem());
	}

	public SdtGridState_InputValuesItem_RESTInterface(SdtGridState_InputValuesItem psdt) {
		super(psdt);
	}

	@XmlElement(name = "Name")
	@JsonProperty("Name")
	public String getgxTv_SdtGridState_InputValuesItem_Name() {
		return GXutil.rtrim((getSdt()).getgxTv_SdtGridState_InputValuesItem_Name());
	}

	@JsonProperty("Name")
	public void setgxTv_SdtGridState_InputValuesItem_Name(String Value) {
		getSdt().setgxTv_SdtGridState_InputValuesItem_Name(Value);
	}


	@XmlElement(name = "Value")
	@JsonProperty("Value")
	public String getgxTv_SdtGridState_InputValuesItem_Value() {
		return GXutil.rtrim((getSdt()).getgxTv_SdtGridState_InputValuesItem_Value());
	}

	@JsonProperty("Value")
	public void setgxTv_SdtGridState_InputValuesItem_Value(String Value) {
		getSdt().setgxTv_SdtGridState_InputValuesItem_Value(Value);
	}


	int remoteHandle = -1;
}

