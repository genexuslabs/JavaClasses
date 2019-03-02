package com.genexus.cryptography;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.Logger;

import com.genexus.cryptography.encryption.symmetric.CipherSymProvider;
import com.genexus.cryptography.encryption.symmetric.IGXSymEncryption;
import com.genexus.cryptography.exception.AlgorithmNotSupportedException;
import com.genexus.cryptography.exception.EncryptionException;
import com.genexus.cryptography.exception.InvalidKeyLengthException;

import com.genexus.webpanels.HttpContextWeb;

public class GXSymEncryption {

	private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(HttpContextWeb.class);
	
	private int _lastError;
	private String _lastErrorDescription;
	private IGXSymEncryption _symAlg; // Algorithm instance
	private String _algorithm;
	private String _key = ""; // key
	private String _iv = ""; // initialization vector
	private boolean isDirty;
	private int _keySize;
	private int _blockSize;

	public GXSymEncryption() {
		isDirty = true;
		_algorithm = String.format("%s/%s/%s", Constants.DEFAULT_SYM_ALGORITHM, Constants.DEFAULT_SYM_MODE,
				Constants.DEFAULT_SYM_PADDING);		
	}

	private void Initialize() {
		if (isDirty) {
			// Supported algorithms = {Rijndael, DES, RC2, TripleDES}
			setError(0);

			try {
				_symAlg = new CipherSymProvider(_algorithm);
				if (validPropertyValue(_key)) {
					_symAlg.setKey(_key);
				}
				if (validPropertyValue(_iv)) {
					_symAlg.setIV(_iv);
				}
				if (_blockSize > 0) {
					_symAlg.setBlockSize(_blockSize);
				}
				if (_keySize > 0) {
					_symAlg.setKeySize(_keySize);
				}
				isDirty = false;
			} catch (NoSuchAlgorithmException e) {
				setError(2);
				Utils.logError(logger, e);
			} catch (NoSuchPaddingException e) {
				setError(3);
				Utils.logError(logger, e);
			} catch (InvalidKeyLengthException e) {
				setError(4, e.getMessage());
				Utils.logError(logger, e);
			} catch (AlgorithmNotSupportedException e) {
				setError(2);
				Utils.logError(logger, e);
			}

		}
	}

	private boolean validPropertyValue(String value) {
		return value != null && !value.equals("");
	}

	public String encrypt(String text) {
		Initialize();
		String encrypted = "";
		if (!anyError()) {
			try {
				encrypted = _symAlg.encrypt(text);
			} catch (EncryptionException e) {
				setError(1);
				Utils.logError(logger, e);
			}
		}
		return encrypted;
	}

	public String decrypt(String text) {
		Initialize();
		String decrypted = "";
		if (!anyError()) {
			try {
				if (getIV().equals("")){			
					setError(5);
					return "";
				}
				decrypted = _symAlg.decrypt(text);
			} catch (EncryptionException e) {
				setError(1);
				Utils.logError(logger, e);
			}
		}
		return decrypted;
	}

	public String getAlgorithm() {
		return _algorithm;
	}

	public void setAlgorithm(String algorithm) {
		algorithm = String.format("%s/%s/%s", algorithm, Constants.DEFAULT_SYM_MODE, Constants.DEFAULT_SYM_PADDING);
		isDirty = isDirty || !this._algorithm.equals(algorithm);
		this._algorithm = algorithm;
	}

	public String getKey() {
		if (!anyError() && _symAlg != null)
			return _symAlg.getKey();
		return _key;
	}

	public void setKey(String key) {
		isDirty = isDirty || !this._key.equals(key);
		this._key = key;
	}

	public String getIV() {
		if (!anyError() && _symAlg != null)
			return _symAlg.getIV();
		return _iv;
	}

	public void setIV(String iv) {
		isDirty = isDirty || !this._iv.equals(iv);
		this._iv = iv;
	}

	public int getKeySize() {
		if (!anyError() && _symAlg != null)
			return _symAlg.getKeySize();
		return _keySize;
	}

	public void setKeySize(int keySize) {
		isDirty = isDirty || this._keySize != keySize;
		this._keySize = keySize * 8;

	}

	public int getBlockSize() {
		if (!anyError() && _symAlg != null)
			return _symAlg.getBlockSize();
		return _blockSize;
	}

	public void setBlockSize(int blockSize) {
		isDirty = isDirty || this._blockSize != blockSize;
		this._blockSize = blockSize;
	}

	private void setError(int errorCode) {
		setError(errorCode, "");
	}

	private void setError(int errorCode, String errDsc) {
		_lastError = errorCode;
		switch (errorCode) {
		case 0:
			_lastErrorDescription = "";
			break;
		case 1:
			_lastErrorDescription = Constants.ENCRYPTION_ERROR;
			break;
		case 2:
			_lastErrorDescription = Constants.ALGORITHM_NOT_SUPPORTED;
			break;
		case 3:
			_lastErrorDescription = Constants.ENCRYPTION_ERROR;
			break;
		case 4:
			_lastErrorDescription = Constants.KEY_NOT_VALID;
			break;
		case 5:
			_lastErrorDescription = "IV must be set for Decryption";
			break;	
		default:
			break;
		}
		if (!errDsc.equals("")) {
			_lastErrorDescription = errDsc;
		}
	}

	private boolean anyError() {
		return _lastError != 0;
	}

	public int getErrCode() {
		return _lastError;
	}

	public String getErrDescription() {
		return _lastErrorDescription;
	}
}