package com.genexus.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.utils.CanonicalizerWrapper;
import com.genexus.utils.KeyInfoType;
import com.genexus.utils.TransformsWrapper;

public class DSigOptions extends SecurityAPIObject {

	private String xmlSchemaPath;
	private TransformsWrapper dSigSignatureType;
	private CanonicalizerWrapper canonicalization;
	private KeyInfoType keyInfoType;
	private String identifierAttribute;

	public DSigOptions() {
		xmlSchemaPath = "";
		dSigSignatureType = TransformsWrapper.ENVELOPED;
		canonicalization = CanonicalizerWrapper.ALGO_ID_C14N_OMIT_COMMENTS;
		keyInfoType = KeyInfoType.X509Certificate;
		identifierAttribute = "";
	}

	public void setXmlSchemaPath(String schemaPath) {
		xmlSchemaPath = schemaPath;
	}

	public String getXmlSchemaPath() {
		return xmlSchemaPath;
	}

	public void setDSigSignatureType(String newDSigSignatureType) {
		dSigSignatureType = TransformsWrapper.getTransformsWrapper(newDSigSignatureType, this.error);
	}

	public String getDSigSignatureType() {
		return TransformsWrapper.valueOf(this.dSigSignatureType, this.error);
	}

	public void setCanonicalization(String newCanonicalization) {
		canonicalization = CanonicalizerWrapper.getCanonicalizerWrapper(newCanonicalization, this.error);
	}

	public String getCanonicalization() {
		return CanonicalizerWrapper.valueOf(canonicalization, this.error);
	}

	public void setKeyInfoType(String newKeyInfoType) {
		keyInfoType = KeyInfoType.getKeyInfoType(newKeyInfoType, this.error);
	}

	public String getKeyInfoType() {
		return KeyInfoType.valueOf(keyInfoType, this.error);
	}

	public void setIdentifierAttribute(String id) {
		identifierAttribute = id;
	}

	public String getIdentifierAttribute() {
		return identifierAttribute;
	}
}
