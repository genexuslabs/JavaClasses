package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;

public class SignatureStandardOptions extends SecurityAPIObject {

	private CertificateX509 certificate;
	private PrivateKeyManager privateKey;

	private SignatureStandard signatureStandard;

	private boolean encapsulated;

	public SignatureStandardOptions() {
		this.signatureStandard = SignatureStandard.CMS;
		this.encapsulated = false;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	public void setPrivateKey(PrivateKeyManager key) {
		this.privateKey = key;
	}

	public void setCertificate(CertificateX509 cert) {
		this.certificate = cert;
	}

	public boolean setSignatureStandard(String standard) {
		this.signatureStandard = SignatureStandard.getSignatureStandard(standard, this.error);
		return !this.hasError();
	}

	public void setEncapsulated(boolean value) {
		this.encapsulated = value;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	public PrivateKeyManager getPrivateKey() {
		return this.privateKey;
	}

	public CertificateX509 getCertificate() {
		return this.certificate;
	}

	public SignatureStandard getSignatureStandard() {
		return this.signatureStandard;
	}

	public boolean getEncapsulated() {
		return this.encapsulated;
	}
}
