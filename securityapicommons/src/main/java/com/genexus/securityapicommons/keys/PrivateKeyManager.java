package com.genexus.securityapicommons.keys;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.EncryptedPrivateKeyInfo;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

import com.genexus.securityapicommons.utils.SecurityUtils;

/**
 * @author sgrampone
 *
 */
public class PrivateKeyManager extends com.genexus.securityapicommons.commons.PrivateKey {

	private PrivateKeyInfo privateKeyInfo;
	private boolean hasPrivateKey;
	private String encryptionPassword;

	/**
	 * KeyManager class constructor
	 */
	public PrivateKeyManager() {
		super();
		this.hasPrivateKey = false;
		this.encryptionPassword = null;
		// Security.addProvider(new BouncyCastleProvider());
	}


	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public boolean load(String privateKeyPath) {

		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("path", privateKeyPath, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		boolean result =  loadPKCS12(privateKeyPath, "", "");
		setAlgorithm();
		return result;
	}

	@Override
	public boolean loadEncrypted(String privateKeyPath, String encryptionPassword) {

		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("path", privateKeyPath, this.error);
		SecurityUtils.validateStringInput("password", encryptionPassword, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		this.encryptionPassword = encryptionPassword;
		boolean result = loadPKCS12(privateKeyPath, "", "");
		setAlgorithm();
		return result;

	}

	@Override
	public boolean loadPKCS12(String privateKeyPath, String alias, String password) {

		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("path", privateKeyPath, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		boolean result = false;
		try {
			result = loadKeyFromFile(privateKeyPath, alias, password);
		} catch (Exception e) {
			this.error.setError("PK001", e.getMessage());
			return false;
		}
		if (this.hasError()) {
			return false;
		}
		setAlgorithm();
		return result;
	}

	@Override
	public boolean fromBase64(String base64) {

		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("base64", base64, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		boolean res;
		try {
			res = readBase64(base64);
		} catch (Exception e) {
			this.error.setError("PK002", e.getMessage());
			return false;
		}
		this.hasPrivateKey = res;
		setAlgorithm();
		return res;
	}

	@Override
	public String toBase64() {
		if (this.hasPrivateKey) {
			String encoded = "";
			try {
				encoded = Base64.toBase64String(this.privateKeyInfo.getEncoded());
			} catch (Exception e) {
				this.error.setError("PK003", e.getMessage());
				return "";
			}
			return encoded;
		}
		this.error.setError("PK0016", "No private key loaded");
		return "";
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private boolean readBase64(String base64) throws IOException {
		byte[] keybytes = Base64.decode(base64);
		ASN1InputStream istream = new ASN1InputStream(keybytes);
		ASN1Sequence seq = (ASN1Sequence) istream.readObject();
		this.privateKeyInfo = PrivateKeyInfo.getInstance(seq);
		istream.close();
		if (this.privateKeyInfo == null) {
			this.error.setError("PK004", "Could not read private key from base64 string");
			return false;
		}
		return true;
	}

	/**
	 * @return PrivateKey type for the key type
	 */
	public PrivateKey getPrivateKey() {
		PrivateKey pk = null;
		try {
			KeyFactory kf = SecurityUtils.getKeyFactory(this.getAlgorithm());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(this.privateKeyInfo.getEncoded());
			pk = kf.generatePrivate(keySpec);
		} catch (Exception e) {
			this.error.setError("PK005", e.getMessage());
			return null;
		}
		return pk;

	}

	@Override
	public AsymmetricKeyParameter getAsymmetricKeyParameter() {
		AsymmetricKeyParameter akp = null;
		try {
			akp = PrivateKeyFactory.createKey(this.privateKeyInfo);
		} catch (Exception e) {
			this.error.setError("PK006", e.getMessage());
			return null;
		}
		return akp;
	}

	/**
	 * @return boolean true if private key is stored
	 */
	public boolean hasPrivateKey() {
		return this.hasPrivateKey;
	}

	/**
	 * Stores structure of public or private key from any type of certificate
	 *
	 * @param path     String of the certificate file
	 * @param alias    Srting certificate's alias, required if PKCS12
	 * @param password String certificate's password, required if PKCS12
	 * @return boolean true if loaded correctly
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws PKCSException
	 */
	private boolean loadKeyFromFile(String path, String alias, String password) throws CertificateException,
		IOException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, PKCSException {
		return loadPrivateKeyFromFile(path, alias, password);
	}

	/**
	 * Stores PrivateKeyInfo Data Type from certificate's private key, algorithm and
	 * digest
	 *
	 * @param path     String of the certificate file
	 * @param alias    Srting certificate's alias, required if PKCS12
	 * @param password String certificate's password, required if PKCS12
	 * @return boolean true if loaded correctly
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws PKCSException
	 */
	private boolean loadPrivateKeyFromFile(String path, String alias, String password) throws CertificateException,
		IOException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, PKCSException {
		if (SecurityUtils.extensionIs(path, ".pem") || SecurityUtils.extensionIs(path, ".key")) {
			return this.hasPrivateKey = loadPrivateKeyFromPEMFile(path);
		}
		if (SecurityUtils.extensionIs(path, ".pfx") || SecurityUtils.extensionIs(path, ".p12")
			|| SecurityUtils.extensionIs(path, ".jks") || SecurityUtils.extensionIs(path, ".pkcs12")) {
			return this.hasPrivateKey = loadPrivateKeyFromPKCS12File(path, alias, password);
		}
		this.error.setError("PK007", "Error loading private key");
		this.hasPrivateKey = false;
		return false;
	}

	/**
	 * Stores PrivateKeyInfo Data Type from the certificate's private key, algorithm
	 * and digest
	 *
	 * @param path     String .ps12, pfx or .jks (PKCS12 fromat) certificate path
	 * @param alias    String certificate's alias
	 * @param password String certificate's password
	 * @return boolean true if loaded correctly
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 */
	private boolean loadPrivateKeyFromPKCS12File(String path, String alias, String password) throws IOException,
		NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyStoreException {

		if (alias == null || password == null) {
			this.error.setError("PK008", "Alias and Password are required for PKCS12 keys");
			return false;
		}
		InputStream in = SecurityUtils.inputFileToStream(path);
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(in, password.toCharArray());
		if(SecurityUtils.compareStrings("", alias))
		{
			alias = ks.aliases().nextElement();
		}
		if (ks.getKey(alias, password.toCharArray()) != null) {
			PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
			this.privateKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
			return true;
		}

		return false;

	}

	/**
	 * stores PrivateKeyInfo Data Type from certificate's private key
	 *
	 * @param path String .pem certificate path
	 * @return boolean true if loaded correctly
	 * @throws IOException
	 * @throws CertificateException
	 * @throws PKCSException
	 */
	private boolean loadPrivateKeyFromPEMFile(String path) throws IOException, CertificateException, PKCSException {
		boolean flag = false;
		try (FileReader privateKeyReader = new FileReader(new File(path))) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof EncryptedPrivateKeyInfo || obj instanceof PKCS8EncryptedPrivateKeyInfo) {
					Security.addProvider(new BouncyCastleProvider());
					PKCS8EncryptedPrivateKeyInfo encPrivKeyInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
					InputDecryptorProvider pkcs8Prov = new JcePKCSPBEInputDecryptorProviderBuilder().setProvider("BC")
						.build(this.encryptionPassword.toCharArray());
					this.privateKeyInfo = encPrivKeyInfo.decryptPrivateKeyInfo(pkcs8Prov);
					flag = true;
				}
				if (obj instanceof PrivateKeyInfo) {
					this.privateKeyInfo = (PrivateKeyInfo) obj;
					flag = true;
				}
				if (obj instanceof PEMKeyPair) {
					PEMKeyPair pemKeyPair = (PEMKeyPair) obj;
					this.privateKeyInfo = pemKeyPair.getPrivateKeyInfo();
					flag = true;
				}
				if (obj instanceof SubjectPublicKeyInfo) {
					this.error.setError("PK009", "The file contains a public key");
					flag = false;
				}
				if (obj instanceof X509CertificateHolder) {
					this.error.setError("PK010", "The file contains a public key");
					flag = false;
				}
			}
		}
		return flag;
	}

	@Override
	protected void setAlgorithm() {
		if(this.privateKeyInfo == null) {
			return;
		}
		String alg = this.privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().getId();
		switch(alg)
		{
			case "1.2.840.113549.1.1.1":
				this.algorithm = "RSA";
				break;
			case "1.2.840.10045.2.1":
				this.algorithm = "ECDSA";
				break;
		}

	}

}
