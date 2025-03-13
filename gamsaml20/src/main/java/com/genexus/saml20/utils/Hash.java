package com.genexus.saml20.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;

import java.text.MessageFormat;

public enum Hash {

	SHA1, SHA256, SHA512;

	private static final Logger logger = LogManager.getLogger(Hash.class);

	public static Hash getHash(String hash)
	{
		logger.trace("GetHash");
		switch(hash.toUpperCase().trim())
		{
			case "SHA1":
				return SHA1;
			case "SHA256":
				return SHA256;
			case "SHA512":
				return SHA512;
			default:
				logger.error(MessageFormat.format("GetHash - not implemented signature hash: {0}", hash));
				return null;
		}
	}

	public static String valueOf(Hash hash)
	{
		switch(hash)
		{
			case SHA1:
				return "SHA1";
			case SHA256:
				return "SHA256";
			case SHA512:
				return "SHA512";
			default:
				return "";
		}
	}

	public static String getSigAlg(Hash hash)
	{
		logger.trace("GetSigAlg");
		switch(hash)
		{
			case SHA1:
				return "http://www.w3.org/2001/04/xmldsig-more#rsa-sha1";
			case SHA256:
				return "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
			case SHA512:
				return "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
			default:
				logger.error("GetSigAlg - not implemented signature hash");
				return "";
		}
	}

	public static Digest getDigest(Hash hash)
	{
		logger.trace("getDigest");
		switch (hash)
		{
			case SHA1:
				return new SHA1Digest();
			case SHA256:
				return new SHA256Digest();
			case SHA512:
				return new SHA512Digest();
			default:
				logger.error("getDigest - unknown hash");
				return null;
		}
	}
}
