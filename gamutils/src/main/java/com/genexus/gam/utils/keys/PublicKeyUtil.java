package com.genexus.gam.utils.keys;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Objects;

public enum PublicKeyUtil {

	crt, cer, pfx, jks, pkcs12, p12, pem, key, b64, json;

	private static final Logger logger = LogManager.getLogger(PublicKeyUtil.class);


	public static RSAPublicKey getPublicKey(String path, String alias, String password, String token) throws NullPointerException{
		logger.debug("getPublicKey");
		PublicKeyUtil ext = PublicKeyUtil.value(fixType(path));
			switch (Objects.requireNonNull(ext)) {
				case crt:
				case cer:
					return (RSAPublicKey) Objects.requireNonNull(loadFromDer(path)).getPublicKey();
				case pfx:
				case jks:
				case pkcs12:
				case p12:
					return (RSAPublicKey) Objects.requireNonNull(loadFromPkcs12(path, alias, password)).getPublicKey();
				case pem:
				case key:
					return (RSAPublicKey) Objects.requireNonNull(loadFromPkcs8(path)).getPublicKey();
				case b64:
					return (RSAPublicKey) Objects.requireNonNull(loadFromBase64(path)).getPublicKey();
				case json:
					return loadFromJson(path, token);
				default:
					logger.error("Invalid public key file extension");
					return null;
			}
	}

	private static PublicKeyUtil value(String ext) {
		switch (ext.toLowerCase().trim()) {
			case "crt":
				return crt;
			case "cer":
				return cer;
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
			case "json":
				return json;
			default:
				logger.error("Invalid certificate file extension");
				return null;
		}
	}

	private static RSAPublicKey loadFromJson(String json, String token) {
		logger.debug("loadFromJson");
		try {
			JWK jwk = JWK.parse(json);
			return (RSAPublicKey) jwk.toRSAKey().toPublicKey();
		} catch (ParseException e) {
			return loadFromJwks(json, token);
		} catch (Exception e) {
			logger.error("loadFromJson", e);
			return null;
		}
	}

	private static RSAPublicKey loadFromJwks(String json, String token) {
		logger.debug("loadFromJwks");
		try {
			com.nimbusds.jose.JWSHeader header = SignedJWT.parse(token).getHeader();
			JWKSet set = JWKSet.parse(json);
			JWK jwk = set.getKeyByKeyId(header.getKeyID());
			return (RSAPublicKey) jwk.toRSAKey().toPublicKey();
		} catch (Exception e) {
			logger.error("loadFromJwks", e);
			return null;
		}
	}

	private static String fixType(String input) {
		try {
			String extension = FilenameUtils.getExtension(input);
			return extension.isEmpty() ? "b64" : extension;
		} catch (IllegalArgumentException e) {
			return "json";
		}
	}


	private static X509Certificate loadFromBase64(String base64) {
		logger.debug("loadBase64");
		try {
			ByteArrayInputStream byteArray = new ByteArrayInputStream(Base64.decode(base64));
			CertificateFactory factory = new CertificateFactory();
			return (X509Certificate) factory.engineGenerateCertificate(byteArray);
		} catch (Exception e) {
			logger.error("loadBase64", e);
			return null;
		}
	}

	private static X509Certificate loadFromPkcs8(String path) {
		logger.debug("loadFromPkcs8");
		try (FileReader privateKeyReader = new FileReader(new File(path))) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj = parser.readObject();
				if (obj instanceof X509CertificateHolder) {
					X509CertificateHolder x509 = (X509CertificateHolder) obj;
					try (InputStream in = new ByteArrayInputStream(x509.getEncoded())) {
						CertificateFactory certFactory = new CertificateFactory();
						return (X509Certificate) certFactory.engineGenerateCertificate(in);
					}
				} else {
					logger.error("Error reading certificate");
					return null;
				}
			}
		} catch (Exception e) {
			logger.error("loadFromPem", e);
			return null;
		}
	}

	private static X509Certificate loadFromPkcs12(String path, String alias, String password) {
		logger.debug("loadFromPkcs12");
		try (FileInputStream inStream = new FileInputStream(path)) {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(inStream, password.toCharArray());
			if (alias.isEmpty()) {
				return (X509Certificate) ks.getCertificate(ks.aliases().nextElement());
			} else {
				return (X509Certificate) ks.getCertificate(alias);
			}
		} catch (Exception e) {
			logger.error("loadFromPkcs12", e);
			return null;
		}
	}

	private static X509Certificate loadFromDer(String path) {
		logger.debug("loadFromDer");
		try (FileInputStream inStream = new FileInputStream(path)) {
			CertificateFactory cf = new CertificateFactory();
			return (X509Certificate) cf.engineGenerateCertificate(inStream);
		} catch (Exception e) {
			logger.error("loadFromDer", e);
			return null;
		}
	}
}
