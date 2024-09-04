package com.genexus.cryptography;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import com.genexus.CommonUtil;
import com.genexus.util.Base64;

public class GXCertificate {
	// static readonly ILog log =
	// LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);

	private X509Certificate _cert;
	private int _lastError;
	private String _lastErrorDescription;
	private String _alias;
	private PrivateKey _privateKey;
	private PublicKey _publicKey;

	public GXCertificate() {

	}

	public GXCertificate(String certPath, String storePassword) {
		load(certPath, storePassword);
	}
	public GXCertificate(String certPath, String storePassword, String pKeyPassword) {
		load(certPath, storePassword, pKeyPassword);
	}

	public int fromBase64(String base64Data) {
		try {
			byte[] dataBuffer = Base64.decode(base64Data);
			ByteArrayInputStream bI = new ByteArrayInputStream(dataBuffer);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			_cert = (X509Certificate) cf.generateCertificate(bI);
		} catch (CertificateException e) {
			setError(1);
			Utils.logError(e);
		}
		return _lastError;
	}

	public int load(String certPath, String storePassword) {
		return load(certPath, storePassword, storePassword);
	}
	public int load(String certPath, String storePassword, String pKeyPassword) {
		setError(0);
		try (FileInputStream inStream = new FileInputStream(certPath)){
			String lowerCertPath = certPath.toLowerCase();
			if (lowerCertPath.endsWith(".pfx") || lowerCertPath.endsWith(".jks") || lowerCertPath.endsWith(".bks") || lowerCertPath.endsWith(".p12")) {
				KeyStore ks = null;
				if (lowerCertPath.endsWith(".pfx") || lowerCertPath.endsWith(".p12")) {
					ks = KeyStore.getInstance("PKCS12");

				} else if (lowerCertPath.endsWith(".bks")) {
					ks = KeyStore.getInstance("BKS");

				} else {
					ks = KeyStore.getInstance("JKS");
				}
				ks.load(inStream, storePassword.toCharArray());
				_alias = ks.aliases().nextElement();
				_cert = (X509Certificate) ks.getCertificate(_alias);
				_publicKey = _cert.getPublicKey(); 
				try {
					Key key = ks.getKey(_alias, pKeyPassword.toCharArray());
					PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key.getEncoded());
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					_privateKey = keyFactory.generatePrivate(keySpec);
				} catch (UnrecoverableKeyException e) {
					setError(5);
				}
			} else if (certPath.endsWith(".cer")) { // No private key
				CertificateFactory factory2 = CertificateFactory.getInstance("X.509");
				_cert = (X509Certificate) factory2.generateCertificate(inStream);
				_publicKey = _cert.getPublicKey(); 
			} else {
				setError(1);
			}
		} catch (FileNotFoundException e) {
			// Certificate Path is not valid.
			setError(3);
		} catch (KeyStoreException e) {
			setError(1);
			Utils.logError(e);
		} catch (NoSuchAlgorithmException e) {
			Utils.logError(e);
		} catch (CertificateException e) {
			setError(1);
			Utils.logError(e);
		} catch (IOException e) {
			setError(1);
			Utils.logError(e);
		} catch (InvalidKeySpecException e) {
			setError(1);
			Utils.logError(e);
		}
		return 0;
	}

	public String getSerialNumber() {
		String value = "";
		if (certLoaded()) {
			value = _cert.getSerialNumber().toString();			
            value = new BigInteger(value).toString(16);
		}
		return value;
	}

	public String getSubject() {

		String value = "";
		if (certLoaded()) {
			value = _cert.getSubjectDN().getName();
		}
		return value;

	}

	public int getVersion() {
		int value = 0;
		if (certLoaded()) {
			value = _cert.getVersion();
		}
		return value;

	}

	public String getIssuer() {

		String value = "";
		if (certLoaded()) {
			value = _cert.getIssuerDN().getName();
		}
		return value;

	}

	public String getThumbprint() {
		return "";
	}

	public Date getNotAfter() {

		Date value = CommonUtil.resetTime(CommonUtil.nullDate());
		if (certLoaded()) {
			value = _cert.getNotAfter();
		}
		return value;

	}

	public Date getNotBefore() {

		Date value = CommonUtil.resetTime(CommonUtil.nullDate());
		if (certLoaded()) {
			value = _cert.getNotBefore();
		}
		return value;

	}

	public PrivateKey getPrivateKey() {
		return _privateKey;

	}

	public PublicKey getPublicKey() {

		return _publicKey;
	}

	public String toBase64() {
		String base64Encoded = "";
		if (certLoaded()) {
			try {
				base64Encoded = Base64.encodeBytes(_cert.getEncoded());
				setError(0);
			} catch (CertificateEncodingException e) {
				setError(6);
				Utils.logError(e);
			}
		} else {
			setError(1);
		}
		return base64Encoded;
	}

	public boolean hasPrivateKey() {
		if (certLoaded()) {
			return _privateKey != null;
		}
		return false;
	}

	/**
	 * Verifies a certificate. Checks its validity period and tries to find a
	 * trusted certificate from given list of trusted certificates that is
	 * directly signed given certificate. The certificate is valid if no
	 * exception is thrown.
	 * 
	 * @param aCertificate
	 *            the certificate to be verified.
	 * @param aTrustedCertificates
	 *            a list of trusted certificates to be used in the verification
	 *            process.
	 * 
	 * @throws CertificateExpiredException
	 *             if the certificate validity period is expired.
	 * @throws CertificateNotYetValidException
	 *             if the certificate validity period is not yet started.
	 * @throws CertificateValidationException
	 *             if the certificate is invalid (can not be validated using the
	 *             given set of trusted certificates.
	 */

	public void check() {
		try {
			// To check the validity of the dates
			_cert.checkValidity();
			// Check the chain
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			List<X509Certificate> mylist = new ArrayList<X509Certificate>();
			mylist.add(_cert);
			CertPath cp = cf.generateCertPath(mylist);
			PKIXParameters params = new PKIXParameters(getTrustStore());
			params.setRevocationEnabled(false);
			CertPathValidator cpv = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
			cpv.validate(cp, params);
		} catch (Exception e) {

		}
	}

	private static KeyStore trustStore;

	public KeyStore getTrustStore() {
		if (trustStore == null) {
			FileInputStream is = null;
			try {
				String filename = System.getProperty("java.home")
						+ "/lib/security/cacerts".replace('/', File.separatorChar);
				is = new FileInputStream(filename);
				KeyStore keyStore = KeyStore.getInstance("JKS");
				keyStore.load(is, "changeit".toCharArray());
				is.close();
				trustStore = keyStore;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				try {if (is != null) is.close();} catch (IOException e) {e.printStackTrace();}
			}
		}
		return trustStore;
	}

	public boolean verify() {
		if (certLoaded()) {
			return verifyCertificateFromCaCerts();
		}
		return false;
	}

	private boolean verifyCertificateFromCaCerts() {
		String filename = System.getProperty("java.home")
			+ "/lib/security/cacerts".replace('/', File.separatorChar);
		try (FileInputStream is = new FileInputStream(filename);){
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			String password = "changeit";
			keystore.load(is, password.toCharArray());

			Enumeration<String> aliases = keystore.aliases();
			List<X509Certificate> certs = new ArrayList<X509Certificate>();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				certs.add((X509Certificate) keystore.getCertificate(alias));
			}

			X509Certificate[] certsArray = certs.toArray(new X509Certificate[certs.size()]);

			return GXCertificate.verifyCertificate(_cert, certsArray);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean verifyCertificate(X509Certificate aCertificate, X509Certificate[] aTrustedCertificates)
			throws GeneralSecurityException {
		// First check certificate validity period
		aCertificate.checkValidity();

		// Check if the certificate is signed by some of the given trusted certs
		for (int i = 0; i < aTrustedCertificates.length; i++) {
			X509Certificate trustedCert = aTrustedCertificates[i];
			try {
				aCertificate.verify(trustedCert.getPublicKey());
				// Found parent certificate. Certificate is verified to be valid
				return true;
			} catch (GeneralSecurityException ex) {
				// Certificate is not signed by current trustedCert. Try the
				// next
			}
		}

		// Certificate is not signed by any of the trusted certs --> it is
		// invalid
		return false;
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
			_lastErrorDescription = Constants.CERT_NOT_LOADED;
			break;
		case 2:
			_lastErrorDescription = Constants.CERT_NOT_TRUSTED;
			break;
		case 3:
			_lastErrorDescription = Constants.CERT_NOT_FOUND;
			break;
		case 4:
			_lastErrorDescription = Constants.CERT_NOT_INITIALIZED;
			break;
		case 5:
			_lastErrorDescription = Constants.PRIVATEKEY_NOT_PRESENT;
			break;
		case 6:
			_lastErrorDescription = Constants.CERT_ENCODING_EXCEPTION;
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

	public X509Certificate getCertificate() {
		return _cert;
	}

	public boolean certLoaded() {
		return _cert != null;
	}

	public int getErrCode() {

		return _lastError;

	}

	public String getErrDescription() {

		return _lastErrorDescription;

	}
}
