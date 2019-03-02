package com.genexus.cryptography;

import com.genexus.cryptography.GXSigning.PKCSStandard;

public class Constants {
	public static final String UNICODE = "UTF-8";

	/* SYMMETRIC CONSTANTS */
	public static final String DEFAULT_SYM_ALGORITHM = "DES";
	public static final String DEFAULT_SYM_PADDING = "PKCS5Padding";
	public static final String DEFAULT_SYM_MODE = "CBC";

	/* HASHING CONSTANTS */
	public static String DEFAULT_HASH_ALGORITHM = "SHA-256";
	public static String SECURITY_HASH_ALGORITHM = "SHA-256";

	
	/* SIGN Constants */ 
	public static final String DEFAULT_DIGITAL_SIGNATURE_ALGORITHM_NAME = "RSA";
	public static final String DEFAULT_DIGITAL_SIGNATURE_FORMAT = "PKCS1";
	public static final String DEFAULT_DIGITAL_SIGNATURE_HASH_ALGORITHM_NAME = "SHA256";
	public static final PKCSStandard DEFAULT_DIGITAL_SIGNATURE_STANDARD = PKCSStandard.PKCS1;
	
	
	/* MESSAGES */ 
	public static final String ALGORITHM_NOT_SUPPORTED = "Algorithm not supported";
	public static final String PRIVATEKEY_NOT_PRESENT = "Certificate does not contain private key";
	public static final String ENCRYPTION_ERROR = "Unknown encryption exception";
	public static final String OK = "";
	public static final String CERT_NOT_LOADED = "Certificate could not be loaded";
	public static final String CERT_NOT_TRUSTED = "Certificate is not trusted.";
	public static final String CERT_NOT_FOUND = "Certificate was not found";
	public static final String CERT_NOT_INITIALIZED = "Certificate not initialized";
	public static final String SIGNATURE_EXCEPTION = "Signature Exception";
	public static final String CERT_ENCODING_EXCEPTION = "Certificate Encoding exception";
	public static final String KEY_NOT_VALID = "Not valid Key";

	
	
}
