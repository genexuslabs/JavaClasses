package com.genexus.security.web.jose.jwt.pem;


import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Read operations for PEM files
 */
public class PemReader {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static PrivateKey readPrivateKey(final String filePath) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {        
        final KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        final PrivateKey privateKey = readPrivateKeyFromFile(factory, filePath);
        return privateKey;
    }

    public static PublicKey readPublicKey(final String filePath) throws NoSuchProviderException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {        
        final KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        final PublicKey publicKey = readPublicKeyFromFile(factory, filePath);
        return publicKey;
    }

    private static PrivateKey readPrivateKeyFromFile(final KeyFactory factory, final String filename) throws InvalidKeySpecException, IOException {                
        final PemFileReader pemFileReader = new PemFileReader(filename);
        final byte[] content = pemFileReader.getPemObject().getContent();
        final PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
        return factory.generatePrivate(privKeySpec);
    }

    private static PublicKey readPublicKeyFromFile(final KeyFactory factory, final String filename) throws InvalidKeySpecException, IOException {        
        final File file = new File(filename);
        final byte[] data =  new byte[0];//Files.readAllBytes(file.toPath());
        final X509Certificate cert = X509CertUtils.parse(new String(data));
        if (cert != null) {
            java.security.PublicKey publicKey = cert.getPublicKey();
            return publicKey;
        } else {
            final PemFileReader pemFileReader = new PemFileReader(filename);
            final byte[] content = pemFileReader.getPemObject().getContent();
            final X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return factory.generatePublic(pubKeySpec);
        }


    }

}
