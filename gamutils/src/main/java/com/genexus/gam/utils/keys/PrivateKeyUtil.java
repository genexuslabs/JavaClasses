package com.genexus.gam.utils.keys;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Base64;

import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public enum PrivateKeyUtil {

	pfx, jks, pkcs12, p12, pem, key, b64;

	private static Logger logger = LogManager.getLogger(PrivateKeyUtil.class);

	public static PrivateKeyUtil value(String ext) {
		switch (ext.toLowerCase().trim()) {
			case "pfx":
				return pfx;
			case "jks":
				return jks;
			case "pkcs12":
				return pkcs12;
			case "p12":
				return p12;
			case "pem":
				return pem;
			case "key":
				return key;
			case "b64":
				return b64;
			default:
				logger.error("Invalid certificate file extension");
				return null;
		}
	}

	public static RSAPrivateKey getPrivateKey(String path, String alias, String password) {
		String extension = FilenameUtils.getExtension(path);
		PrivateKeyUtil ext = extension.isEmpty() ? PrivateKeyUtil.value("b64"): PrivateKeyUtil.value(extension);
		switch (ext) {
			case pfx:
			case jks:
			case pkcs12:
			case p12:
				return loadFromPkcs12(path, alias, password);
			case pem:
			case key:
				return loadFromPkcs8(path);
			case b64:
				return loadFromBase64(path);
			default:
				logger.error("Invalid private key file extension");
				return null;
		}
	}

	private static RSAPrivateKey loadFromBase64(String base64)
	{
		logger.debug("loadFromBase64");
		try(ASN1InputStream stream = new ASN1InputStream(Base64.decode(base64))) {
			ASN1Sequence seq = (ASN1Sequence) stream.readObject();
			return castPrivateKeyInfo(PrivateKeyInfo.getInstance(seq));
		}catch (Exception e)
		{
			logger.error("loadFromBase64", e);
			return null;
		}
	}

	private static RSAPrivateKey loadFromPkcs8(String path) {
		logger.debug("loadFromPkcs8");
		try (FileReader privateKeyReader = new FileReader(path)) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof PrivateKeyInfo) {
					return castPrivateKeyInfo((PrivateKeyInfo) obj);
				} else if (obj instanceof PEMKeyPair) {
					PEMKeyPair pemKeyPair = (PEMKeyPair) obj;
					return castPrivateKeyInfo(pemKeyPair.getPrivateKeyInfo());
				} else {
					logger.error("loadFromPkcs8: Could not load private key");
					return null;
				}
			}
		} catch (Exception e) {
			logger.error("loadFromPkcs8", e);
			return null;
		}
	}

	private static RSAPrivateKey castPrivateKeyInfo(PrivateKeyInfo privateKeyInfo) {
		logger.debug("castPrivateKeyInfo");
		try {
			KeyFactory kf = KeyFactory.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().getId());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded());
			return (RSAPrivateKey) kf.generatePrivate(keySpec);
		} catch (Exception e) {
			logger.error("castPrivateKeyInfo", e);
			return null;
		}
	}

	private static RSAPrivateKey loadFromPkcs12(String path, String alias, String password) {
		logger.debug("loadFromPkcs12");
		try (InputStream targetStream = Files.newInputStream(Paths.get(path))) {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(targetStream, password.toCharArray());
			if (alias.isEmpty()) {
				return (RSAPrivateKey) ks.getKey(ks.aliases().nextElement(), password.toCharArray());
			} else {
				return (RSAPrivateKey) ks.getKey(alias, password.toCharArray());
			}
		} catch (Exception e) {
			logger.error("loadFromPkcs12", e);
			return null;
		}
	}
}
