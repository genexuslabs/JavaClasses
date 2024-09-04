package com.genexus.securityapicommons.keys;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Base64;

import com.genexus.securityapicommons.utils.SecurityUtils;

public class CertificateX509 extends com.genexus.securityapicommons.commons.Certificate {

	private String algorithmWithHash;
	private boolean hasPublicKey;
	private X509Certificate cert;
	public String issuer;
	public String subject;
	public String serialNumber;
	public String thumbprint;
	public Date notAfter;
	public Date notBefore;
	public int version;
	private boolean inicialized;

	/**
	 * CertificateX509 class constructor
	 */
	public CertificateX509() {
		super();
		this.hasPublicKey = false;
		this.inicialized = false;
	}

	public boolean Inicialized() {
		return this.inicialized;
	}

	public X509Certificate Cert() {
		return this.cert;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public boolean load(String path) {
		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("path", path, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		boolean result =  loadPKCS12(path, "", "");
		if(result)
		{
			this.inicialized = true;
		}
		setAlgorithm();
		return result;
	}

	@Override
	public boolean loadPKCS12(String path, String alias, String password) {
		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("path", path, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		boolean result = false;
		try {
			result = loadPublicKeyFromFile(path, alias, password);
		} catch (Exception e) {
			this.error.setError("CE001", e.getMessage());
			return false;
		}
		if (result) {
			inicializeParameters();
			setAlgorithm();
		}
		return result;
	}

	@Override
	public boolean fromBase64(String base64Data) {
		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("base64Data", base64Data, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		boolean flag;
		try {
			byte[] dataBuffer = Base64.decode(base64Data);
			ByteArrayInputStream bI = new ByteArrayInputStream(dataBuffer);
			CertificateFactory cf = new CertificateFactory();
			this.cert = (X509Certificate) cf.engineGenerateCertificate(bI);
			inicializeParameters();
			flag = true;
		} catch (Exception e) {
			this.error.setError("CE002", e.getMessage());
			flag = false;
		}
		setAlgorithm();

		return flag;
	}

	@Override
	public String toBase64() {
		if (!this.inicialized) {
			this.error.setError("CE003", "Not loaded certificate");
			return "";
		}
		String base64Encoded = "";

		try {
			base64Encoded = new String(Base64.encode(this.cert.getEncoded()));

		} catch (Exception e) {
			this.error.setError("CE004", e.getMessage());
		}

		return base64Encoded;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	/**
	 * @return String certificate-s hash algorithm for sign verification
	 */
	public String getPublicKeyHash() {
		String[] aux = this.algorithmWithHash.toUpperCase().split("WITH");
		if (SecurityUtils.compareStrings(aux[0], "1.2.840.10045.2.1")) {
			return "ECDSA";
		}
		return aux[0];
	}

	/**
	 * stores SubjectPublicKeyInfo Data Type of public key from certificate,
	 * algorithm and digest
	 *
	 * @param path     String of the certificate file
	 * @param alias    Srting certificate's alias, required if PKCS12
	 * @param password String certificate's password, required if PKCS12
	 * @return boolean true if loaded correctly
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 */
	private boolean loadPublicKeyFromFile(String path, String alias, String password)
		throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
		boolean result = false;
		if (SecurityUtils.extensionIs(path, ".pem") || SecurityUtils.extensionIs(path, ".key")) {
			result = loadPublicKeyFromPEMFile(path);
			return result;
		}
		if (SecurityUtils.extensionIs(path, ".crt") || SecurityUtils.extensionIs(path, ".cer")) {
			result = loadPublicKeyFromDERFile(path);
			return result;
		}
		if (SecurityUtils.extensionIs(path, ".pfx") || SecurityUtils.extensionIs(path, ".p12")
			|| SecurityUtils.extensionIs(path, ".jks") || SecurityUtils.extensionIs(path, ".pkcs12")) {
			result = loadPublicKeyFromPKCS12File(path, alias, password);
			return result;
		}
		this.error.setError("CE005", "Error loading certificate");
		this.hasPublicKey = false;
		return false;

	}

	/**
	 * stores SubjectPublicKeyInfo Data Type from certificate's public key,
	 * asymmetric algorithm and digest
	 *
	 * @param path
	 *
	 *
	 * @param alias    Strting certificate's alias
	 * @param password String certificate's password
	 * @return boolean true if loaded correctly
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	private boolean loadPublicKeyFromPKCS12File(String path, String alias, String password)
		throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

		if (alias == null || password == null) {
			this.error.setError("CE006", "Alias and password are required for PKCS12 certificates");
			return false;
		}
		InputStream in;
		in = SecurityUtils.inputFileToStream(path);
		KeyStore ks;
		try {
			ks = KeyStore.getInstance("PKCS12");
			ks.load(in, password.toCharArray());
			if(SecurityUtils.compareStrings("", alias))
			{
				alias = ks.aliases().nextElement();
			}
			this.cert = (X509Certificate) ks.getCertificate(alias);
		} catch (Exception e) {
			this.error.setError("CE007", "Path not found.");
			return false;
		}
		return true;

	}

	/**
	 * stores SubjectPublicKeyInfo Data Type from certificate's public key,
	 * asymmetric algorithm and digest
	 *
	 * @param path String .pem certificate path
	 * @return boolean true if loaded correctly
	 * @throws IOException
	 * @throws CertificateException
	 */
	private boolean loadPublicKeyFromPEMFile(String path) throws IOException, CertificateException {
		boolean flag = false;

		try (FileReader privateKeyReader = new FileReader(new File(path))) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof PrivateKeyInfo) {
					this.error.setError("CE008", "The file contains a private key");
				}
				if ((obj instanceof PEMKeyPair) || (obj instanceof SubjectPublicKeyInfo)){
					this.error.setError("CE009", "It is a public key not a certificate, use PublicKey Object instead");
					flag = false;
				}
				if (obj instanceof X509CertificateHolder) {
					X509CertificateHolder x509 = (X509CertificateHolder) obj;
					CertificateFactory certFactory = new CertificateFactory();
					InputStream in;
					in = new ByteArrayInputStream(x509.getEncoded());
					this.cert = (X509Certificate) certFactory.engineGenerateCertificate(in);
					flag = true;
				}
			}
		}
		return flag;
	}

	/**
	 * stores PublicKeyInfo Data Type from the certificate's public key, asymmetric
	 * algorithm and digest
	 *
	 * @param path String .crt .cer file certificate
	 * @return boolean true if loaded correctly
	 * @throws IOException
	 * @throws CertificateException
	 */
	private boolean loadPublicKeyFromDERFile(String path) throws IOException, CertificateException {
		InputStream input;
		input = SecurityUtils.inputFileToStream(path);
		CertificateFactory cf = new CertificateFactory();
		this.cert = (X509Certificate) cf.engineGenerateCertificate(input);
		input.close();
		return true;
	}

	private void inicializeParameters() {
		this.serialNumber = this.cert.getSerialNumber().toString();
		this.subject = this.cert.getSubjectDN().getName();
		this.version = this.cert.getVersion();
		this.issuer = this.cert.getIssuerDN().getName();
		this.thumbprint = "";
		this.notAfter = this.cert.getNotAfter();
		this.notBefore = this.cert.getNotBefore();
		this.algorithmWithHash = this.cert.getSigAlgName();
		extractPublicInfo();
		this.inicialized = true;
	}

	/**
	 * Extract public key information and certificate's signing algorithm
	 *
	 * @param cert java Certificate
	 */
	private void extractPublicInfo() {
		Certificate cert1 = (Certificate) this.cert;
		PublicKey publicKey = cert1.getPublicKey();
		this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
	}
}
