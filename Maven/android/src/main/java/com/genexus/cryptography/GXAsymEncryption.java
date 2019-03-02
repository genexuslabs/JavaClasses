package com.genexus.cryptography;

import com.genexus.cryptography.encryption.asymmetric.CipherAsymProvider;
import com.genexus.cryptography.encryption.asymmetric.IGXAsymEncryption;
import com.genexus.cryptography.exception.AlgorithmNotSupportedException;
import com.genexus.cryptography.exception.EncryptionException;
import com.genexus.cryptography.exception.PrivateKeyNotFoundException;
import com.genexus.cryptography.exception.PublicKeyNotFoundException;

public class GXAsymEncryption {

	private static final String DEFAULT_SYM_ALGORITHM = "RSA";
	private static final String DEFAULT_SYM_PADDING = "PKCS1Padding";
	private static final String DEFAULT_SYM_MODE = "ECB";
	
	private static final String SHA256_SYM_PADDING = "OAEPWithSHA-256AndMGF1Padding";

	private int _lastError;
	private String _lastErrorDescription;
	private String _algorithm;

	private GXCertificate _cert;
	private IGXAsymEncryption _asymAlg;
	private boolean _isDirty;

	public GXAsymEncryption() {
		_isDirty = true;
		_algorithm = String.format("%s/%s/%s", DEFAULT_SYM_ALGORITHM,
				DEFAULT_SYM_MODE, DEFAULT_SYM_PADDING);
		initialize();
	}

	private void initialize() {
		if (_isDirty) {
			// Support algorithms = RSA only for now..
			// SHA256 ? 
			setError(0);

			if (_cert != null && _cert.certLoaded() == true) {
				try {
					_asymAlg = new CipherAsymProvider(_algorithm,
							_cert.getPublicKey(), _cert.getPrivateKey());
					_isDirty = false;
				} catch (AlgorithmNotSupportedException e) {
					setError(2);
				}
			} else {
				setError(4);
			}
		}
	}

	public String encrypt(String text) {
		initialize();
		String encrypted = "";
		if (!anyError()) {
			try {
				encrypted = _asymAlg.encrypt(text);
			} catch (PublicKeyNotFoundException e) {
				setError(4);
			} catch (EncryptionException e) {
				setError(3);
				Utils.logError(e);
			}
		}
		return encrypted;
	}

	public String decrypt(String text) {
		initialize();
		String decrypted = "";
		if (!anyError()) {

			try {
				decrypted = _asymAlg.decrypt(text);
			} catch (PrivateKeyNotFoundException e) {
				setError(5);
			} catch (EncryptionException e) {
				setError(3);
				Utils.logError(e);
			}
		}
		return decrypted;
	}

	private void setError(int errorCode) {
		setError(errorCode, "");
	}

	private void setError(int errorCode, String errDsc) {
		_lastError = errorCode;
		switch (errorCode) {
		case 0:
			_lastErrorDescription = Constants.OK;
			break;
		case 1:
			break;
		case 2:
			_lastErrorDescription = Constants.ALGORITHM_NOT_SUPPORTED;
			break;
		case 3:
			_lastErrorDescription = Constants.ENCRYPTION_ERROR;
			break;
		case 4:
			_lastErrorDescription = "";
			break;
		case 5:
			_lastErrorDescription = Constants.PRIVATEKEY_NOT_PRESENT;
			break;
		default:
			break;
		}
		if (!errDsc.equals("")) {
			_lastErrorDescription = errDsc;
		}
	}

	public String getAlgorithm() {
		return _algorithm;
	}

	public void setAlgorithm(String value) 
	{
		//Android , https://developer.android.com/reference/android/security/keystore/KeyProperties.html#KEY_ALGORITHM_AES 
		// RSA == RSA
		// only support RSA https://developer.android.com/reference/javax/crypto/Cipher.html
		// change to Android KeyProperties
		//if (value.equalsIgnoreCase("SHA1")) { value = "HmacSHA1"; }
		//if (value.equalsIgnoreCase("SHA256")) { value = "HmacSHA256"; }
		//if (value.equalsIgnoreCase("SHA512")) { value = "HmacSHA512"; }
		
		// convert sha256 to RSA with sha256
		if (value.equalsIgnoreCase("SHA256"))	
		{	
			value = String.format("%s/%s/%s", DEFAULT_SYM_ALGORITHM, DEFAULT_SYM_MODE,
					SHA256_SYM_PADDING);
		}
		else
		{
			value = String.format("%s/%s/%s", value, DEFAULT_SYM_MODE,
				DEFAULT_SYM_PADDING);
		}
		_isDirty = _isDirty || !value.equals(_algorithm);
		_algorithm = value;
	}

	public GXCertificate getCertificate() {
		return _cert;

	}

	public void setCertificate(GXCertificate cert) {
		_isDirty = _isDirty || cert != _cert;
		_cert = cert;
	}

	private boolean anyError() {

		if (_cert == null || (!_cert.certLoaded() == true)) {
			setError(4); // Certificate not initialized
		}
		return _lastError != 0;

	}

	public int getErrCode() {
		return _lastError;
	}

	public String getErrDescription() {
		return _lastErrorDescription;
	}
}
