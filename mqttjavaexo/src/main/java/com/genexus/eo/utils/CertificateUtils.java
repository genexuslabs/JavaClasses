package com.genexus.eo.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;


/**
 * This is a helper class to facilitate reading certificate from the resource files.
 */
public class CertificateUtils {

    private static final Logger log = LogManager.getLogger(CertificateUtils.class);

    public static SSLSocketFactory getSSLSocketFactory(final String caCrt,
                                                       final String crt, final String privateKey, final String password) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // load CA certificate
        X509Certificate caCert = null;
        File file = new File(caCrt);
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(caCrt);
            BufferedInputStream bis = new BufferedInputStream(fis);
            while (bis.available() > 0) {
                caCert = (X509Certificate) cf.generateCertificate(bis);
            }
        } else {
            InputStream stream = new ByteArrayInputStream(caCrt.getBytes(StandardCharsets.UTF_8));
            try {
                while (stream.available() > 0) {
                    caCert = (X509Certificate) cf.generateCertificate(stream);
                }
            } catch (java.security.cert.CertificateException e) {
                // Try if cert is base64 encoded
                stream = new ByteArrayInputStream(Base64.decodeBase64(caCrt));
                while (stream.available() > 0) {
                    caCert = (X509Certificate) cf.generateCertificate(stream);
                }
            }
        }

        // load client certificate
        X509Certificate cert = null;
        file = new File(crt);
        if (file.exists()) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(crt));
            while (bis.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(bis);
            }
        } else {
            InputStream stream = new ByteArrayInputStream(crt.getBytes(StandardCharsets.UTF_8));
            try {
                while (stream.available() > 0) {
                    cert = (X509Certificate) cf.generateCertificate(stream);
                }
            } catch (java.security.cert.CertificateException e) {
                // Try if cert is base64 encoded
                stream = new ByteArrayInputStream(Base64.decodeBase64(crt));
                while (stream.available() > 0) {
                	cert = (X509Certificate) cf.generateCertificate(stream);
                }
            }
        }

        char[] passwordArray = (password==null?"":password).toCharArray();

        // load client private key
        KeyPair key;
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(passwordArray);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PEMParser pemParser;
        file = new File(privateKey);
        if (file.exists()) {
            pemParser = new PEMParser(new FileReader(privateKey));
        } else {
            pemParser = new PEMParser(new StringReader(privateKey));
        }
        Object object = pemParser.readObject();
        try {
            if (object instanceof PEMEncryptedKeyPair)
                key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
            else
                key = converter.getKeyPair((PEMKeyPair) object);

        } catch (org.bouncycastle.openssl.PEMException e) {
            // Try if key is base64 encoded
            String decoded = new String(Base64.decodeBase64(privateKey));
            pemParser = new PEMParser(new StringReader(decoded));
            object = pemParser.readObject();
            if (object instanceof PEMEncryptedKeyPair) {
                log.debug("Decrypting key with password");
                key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
            }
            else {
                log.debug("No password provided for key \n" + decoded);
                key = converter.getKeyPair((PEMKeyPair) object);
            }

        }
        pemParser.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), passwordArray,
                new java.security.cert.Certificate[] { cert });
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, passwordArray);

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

}
