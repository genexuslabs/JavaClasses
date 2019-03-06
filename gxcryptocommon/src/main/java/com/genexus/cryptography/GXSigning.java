package com.genexus.cryptography;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cms.CMSException;

import com.genexus.cryptography.exception.PrivateKeyNotFoundException;
import com.genexus.cryptography.exception.PublicKeyNotFoundException;
import com.genexus.cryptography.exception.SignatureException;
import com.genexus.cryptography.signing.IPkcsSign;
import com.genexus.cryptography.signing.standards.PKCS1Signature;
import com.genexus.cryptography.signing.standards.PKCS7Signature;

public class GXSigning {

	private GXCertificate _cert;
	private String _alg;
	private String _hashAlgorithm;
	private String _signAlgorithm;
	private IPkcsSign _sign;
	private int _lastError;
	private String _lastErrorDescription;
	private Boolean isDirty;
	private Boolean _validateCertificates;
	private PKCSStandard _standard;

	public enum PKCSStandard {
		PKCS1, PKCS7
	};

	public GXSigning() {
		isDirty = true;
		_validateCertificates = true;
		_standard = Constants.DEFAULT_DIGITAL_SIGNATURE_STANDARD;
				
		_signAlgorithm = Constants.DEFAULT_DIGITAL_SIGNATURE_ALGORITHM_NAME;
		_hashAlgorithm = Constants.DEFAULT_DIGITAL_SIGNATURE_HASH_ALGORITHM_NAME;
	}

	public String sign(String text, Boolean detached) {
		Initialize();
		String signed = "";
		if (!anyError()) {
			try {
				_sign.setCertificate(_cert.getCertificate());
				if (_standard == PKCSStandard.PKCS7) {
					((PKCS7Signature) _sign).setDetached(detached);
				}
				signed = _sign.sign(text.getBytes(Constants.UNICODE));
			} catch (PrivateKeyNotFoundException e) {
				setError(5);
			} catch (UnsupportedEncodingException e) {
				Utils.logError(e);
				setError(6, e.getMessage());
			} catch (GeneralSecurityException e) {
				Utils.logError(e);
				setError(6, e.getMessage());
			} catch (CMSException e) {
				Utils.logError(e);
				setError(6, e.getMessage());
			} catch (IOException e) {
				Utils.logError(e);
				setError(6, e.getMessage());
			} catch (PublicKeyNotFoundException e) {
				setError(4);
			}
		}
		return signed;

	}

	public Boolean verify(String signature, String text, Boolean detached) {

		Initialize();
		Boolean ok = false;
		if (!anyError()) {
			try {
				_sign.setCertificate(_cert.getCertificate());
				if (_standard == PKCSStandard.PKCS7) {
					((PKCS7Signature) _sign).setDetached(detached);
				}
				ok = _sign.verify(text.getBytes(Constants.UNICODE), Base64.decodeBase64(signature));
			} catch (UnsupportedEncodingException e) {
				Utils.logError(e);
				setError(6);
			} catch (GeneralSecurityException e) {
				Utils.logError(e);
				setError(6);
			} catch (PublicKeyNotFoundException e) {
				setError(4);
			} catch (SignatureException e) {
				Utils.logError(e);
				setError(6, e.getMessage());
			}
		}
		return ok;

	}

	private void Initialize() {
		if (isDirty) {
			String algorithm = String.format("%swith%s", _hashAlgorithm, _signAlgorithm);
			switch (_standard) {
			case PKCS1:
				_sign = new PKCS1Signature(algorithm, _cert.getCertificate(), _cert.getPrivateKey());
				break;
			case PKCS7:
				_sign = new PKCS7Signature(algorithm, _cert.getCertificate(), _cert.getPrivateKey());
				break;
			default:
				break;
			}

			isDirty = false;
		}
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
			break;
		case 2:
			_lastErrorDescription = Constants.ALGORITHM_NOT_SUPPORTED;
			break;
		case 3:
			_lastErrorDescription = "Invalid Algorithm format";
			break;
		case 4:
			_lastErrorDescription = Constants.CERT_NOT_INITIALIZED;
			break;
		case 5:
			_lastErrorDescription = Constants.PRIVATEKEY_NOT_PRESENT;
			break;
		case 6:
			_lastErrorDescription = Constants.SIGNATURE_EXCEPTION;
			break;
		default:
			break;
		}
		if (!errDsc.equals("")) {
			if (!_lastErrorDescription.equals("")) {
				_lastErrorDescription = String.format("%s - %s", _lastErrorDescription, errDsc);
			} else {
				_lastErrorDescription = errDsc;
			}
		}
	}

	public void setValidateCertificate(Boolean validate) {
		this._validateCertificates = validate;
	}

	public Boolean getValidateCertificate() {
		return this._validateCertificates;
	}

	public void setStandard(String std) {
		PKCSStandard oldV = _standard;
		if (std.equals("PKCS7")) {
			_standard = PKCSStandard.PKCS7;
		} else if (std.equals("PKCS1")) {
			_standard = PKCSStandard.PKCS1;
		} else {
			setError(2); // Algorithm not supported
		}

		isDirty = isDirty || oldV != _standard;
	}

	public String getStandard() {
		return _standard.toString();
	}

	public void setAlgorithm(String value) {

		isDirty = isDirty || !value.equals(_alg);
		_alg = value;
		String[] parts = _alg.split(" ");
		if (parts.length == 2) // Format Example: MD5 RSA.
		{
			String hash = parts[0];
			String sign = parts[1];

			_hashAlgorithm = hash;
			_signAlgorithm = sign;
		} else {
			setError(3);
			// invalid format algorithm.
		}

	}

	public GXCertificate getCertificate() {
		return _cert;
	}

	public void setCertificate(GXCertificate cert) {
		this._cert = cert;
	}

	private Boolean anyError() {

		if (_cert == null || (_cert != null && !_cert.certLoaded())) {
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
