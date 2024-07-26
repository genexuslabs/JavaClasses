package com.genexus.gam.utils;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.openssl.PEMParser;

import java.io.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public enum CertificateUtil {

	crt, cer, pfx, jks, pkcs12, p12, pem, key;

	public static final ILogger logger = LogManager.getLogger(CertificateUtil.class);

	public static CertificateUtil value(String ext) {
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
			default:
				logger.error("Invalid certificate file extension");
				return null;
		}
	}

	public static X509Certificate getCertificate(String path, String alias, String password) {
		CertificateUtil ext = CertificateUtil.value(FilenameUtils.getExtension(path));
		if (ext == null) {
			logger.error("Error reading certificate path");
			return null;
		}
		switch (ext) {
			case crt:
			case cer:
				return loadFromDer(path);
			case pfx:
			case jks:
			case pkcs12:
			case p12:
				return loadFromPkcs12(path, alias, password);
			case pem:
			case key:
				return loadFromPkcs8(path);
			default:
				logger.error("Invalid certificate file extension");
				return null;
		}
	}

	private static X509Certificate loadFromPkcs8(String path) {
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
		try (FileInputStream inStream = new FileInputStream(path)) {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(inStream, password.toCharArray());
			if (alias.equals("") || alias.isEmpty()) {
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
		try (FileInputStream inStream = new FileInputStream(path)) {
			CertificateFactory cf = new CertificateFactory();
			return (X509Certificate) cf.engineGenerateCertificate(inStream);
		} catch (Exception e) {
			logger.error("loadFromDer", e);
			return null;
		}
	}
}
