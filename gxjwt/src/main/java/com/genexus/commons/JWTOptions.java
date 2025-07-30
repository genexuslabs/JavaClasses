package com.genexus.commons;

import com.genexus.JWT.claims.HeaderParameters;
import com.genexus.JWT.claims.PublicClaims;
import com.genexus.JWT.claims.RegisteredClaims;
import com.genexus.JWT.utils.RevocationList;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import org.bouncycastle.util.encoders.Hex;


@SuppressWarnings("unused")
public class JWTOptions extends SecurityAPIObject {

	private final PublicClaims publicClaims;
	private final RegisteredClaims registeredClaims;
	private byte[] secret;
	private RevocationList revocationList;
	private CertificateX509 certificate;
	private PrivateKeyManager privateKey;
	private PublicKey publicKey;
	private final HeaderParameters parameters;

	public JWTOptions() {
		publicClaims = new PublicClaims();
		registeredClaims = new RegisteredClaims();
		revocationList = new RevocationList();
		parameters = new HeaderParameters();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public void setPrivateKey(PrivateKeyManager key) {
		this.privateKey = key;
	}

	public void setPublicKey(PublicKey key) {
		this.publicKey = key;
	}

	public void setCertificate(CertificateX509 cert) {
		this.certificate = cert;
	}

	public void setSecret(String value) {
		try {
			this.secret = Hex.decode(value);
		} catch (Exception e) {
			this.error.setError("OP001", e.getMessage());
			secret = null;
		}

	}

	public boolean addCustomTimeValidationClaim(String key, String value, String customTime) {
		this.registeredClaims.setTimeValidatingClaim(key, value, customTime, this.error);
		return !this.hasError();
	}

	public boolean addRegisteredClaim(String registeredClaimKey, String registeredClaimValue) {
		return registeredClaims.setClaim(registeredClaimKey, registeredClaimValue, this.error);
	}

	public boolean addPublicClaim(String publicClaimKey, String publicClaimValue) {
		return publicClaims.setClaim(publicClaimKey, publicClaimValue, this.error);
	}

	public void addRevocationList(RevocationList revocationList) {
		this.revocationList = revocationList;
	}

	public void deteleRevocationList() {
		this.revocationList = new RevocationList();
	}

	public void addHeaderParameter(String name, String value) {
		this.parameters.setParameter(name, value);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	public PublicKey getPublicKey() {
		return (this.certificate == null) ? this.publicKey : this.certificate;
	}

	public boolean hasPublicClaims() {
		return !publicClaims.isEmpty();
	}

	public boolean hasRegisteredClaims() {
		return !registeredClaims.isEmpty();
	}

	public RegisteredClaims getAllRegisteredClaims() {
		return this.registeredClaims;
	}

	public PublicClaims getAllPublicClaims() {
		return this.publicClaims;
	}

	public long getcustomValidationClaimValue(String key) {
		return this.registeredClaims.getClaimCustomValidationTime(key);
	}

	public boolean hasCustomTimeValidatingClaims() {
		return this.getAllRegisteredClaims().hasCustomValidationClaims();
	}


	public byte[] getSecret() {
		return this.secret;
	}

	public RevocationList getRevocationList() {
		return this.revocationList;
	}

	public PrivateKeyManager getPrivateKey() {
		return this.privateKey;
	}

	public HeaderParameters getHeaderParameters() {
		return this.parameters;
	}
}
