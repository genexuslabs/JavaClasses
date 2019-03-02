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

	public void setAlgorithm(String value) {
		value = String.format("%s/%s/%s", value, DEFAULT_SYM_MODE,
				DEFAULT_SYM_PADDING);
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
