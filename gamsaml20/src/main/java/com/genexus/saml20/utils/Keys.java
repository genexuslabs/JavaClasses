package com.genexus.saml20.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

public class Keys {

	private static final Logger logger = LogManager.getLogger(Keys.class);

	public static AsymmetricKeyParameter loadPrivateKey(String path, String alias, String password)
	{
		return isBase64(path) ? privateKeyFromBase64(path): loadPrivateKeyFromJKS(path, alias, password);
	}

	public static X509Certificate loadCertificate(String path, String alias, String password)
	{
		return isBase64(path) ? loadCertificateFromBase64(path): loadCertificateFromJKS(path, alias, password);
	}

	private static String getCertPath(String path)
	{
		//boolean isAbsolute = new File(path).isAbsolute();

		logger.debug("cuurent dir: " +new File(".").toPath().toAbsolutePath());

		return System.getProperty("user.dir");
	}

	private static AsymmetricKeyParameter privateKeyFromBase64(String b64)
	{
		logger.trace("privateKeyFromBase64");
		try {
			byte[] keyBytes = Base64.decode(b64);
			try (ASN1InputStream istream = new ASN1InputStream(keyBytes)) {
				ASN1Sequence seq = (ASN1Sequence) istream.readObject();
				return PrivateKeyFactory.createKey(PrivateKeyInfo.getInstance(seq));
			}
		} catch (Exception e) {
			logger.error("privateKeyFromBase64", e);
			return null;
		}
	}

	private static X509Certificate loadCertificateFromBase64(String b64)
	{
		logger.trace("loadCertificateFromBase64");
		try {
			byte[] dataBuffer = Base64.decode(b64);
			ByteArrayInputStream bI = new ByteArrayInputStream(dataBuffer);
			CertificateFactory cf = new CertificateFactory();
			return (X509Certificate) cf.engineGenerateCertificate(bI);
		} catch (Exception e) {
			logger.error("loadCertificateFromBase64", e);
			return null;
		}
	}

	private static AsymmetricKeyParameter loadPrivateKeyFromJKS(String path, String alias, String password)
	{
		logger.trace("loadPrivateKeyFromJKS");
		logger.debug(MessageFormat.format("Path: {0} , alias: {1}", path, alias));
		getCertPath(path);
		try (InputStream in = new DataInputStream(Files.newInputStream(new File(path).toPath()))) {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(in, password.toCharArray());
			if (alias.isEmpty()) {
				alias = ks.aliases().nextElement();
			}
			if (ks.getKey(alias, password.toCharArray()) != null) {
				PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
				PrivateKeyInfo keyinfo =  PrivateKeyInfo.getInstance(privateKey.getEncoded());
				return PrivateKeyFactory.createKey(keyinfo);
			}
		} catch (Exception e) {
			logger.error("loadPrivateKeyFromJKS", e);
			return null;
		}
		return null;
	}

	private static X509Certificate loadCertificateFromJKS(String path, String alias, String password) {
		logger.trace("loadCertificateFromJKS");
		logger.debug("alias: " + alias);
		logger.debug("pasword: " + password);
		logger.debug(MessageFormat.format("path: {0}, alias: {1}", path, alias));
		Path p = new File(path).toPath();
		logger.debug("Res path: "+ p.toAbsolutePath());
		System.out.println("Res path: "+ p.toAbsolutePath());
		try (InputStream in = new DataInputStream(Files.newInputStream(p))) {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(in, password.toCharArray());
			if (alias.isEmpty()) {
				alias = ks.aliases().nextElement();
			}
			return (X509Certificate) ks.getCertificate(alias);

		} catch (Exception e) {
			logger.error("loadCertificateFromJKS", e);
			return null;
		}

	}

	public static boolean isBase64(String path)
	{
		try
		{
			Base64.decode(path);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	public static String getHash(String path, String alias, String password) {
		String algorithmWithHash = loadCertificateFromJKS(path, alias, password).getSigAlgName();
		String[] aux = algorithmWithHash.toUpperCase().split("WITH");
		return aux[0];
	}
}