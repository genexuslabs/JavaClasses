package com.genexus.common.interfaces;

public interface IGXWSSignature
{
	String getAlias();
	void setAlias(String alias);
	IGXWSSecurityKeyStore getKeystore();
	void setKeystore(IGXWSSecurityKeyStore keystore);
	int getKeyIdentifierType();
	void setKeyIdentifierType(int keyIdentifierType);
	String getCanonicalizationalgorithm();
	void setCanonicalizationalgorithm(String algorithm);
	String getDigest();
	void setDigest(String digest);
	String getSignaturealgorithm();
	void setSignaturealgorithm(String algorithm);
}

