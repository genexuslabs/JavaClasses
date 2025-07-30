package com.genexus.cryptography.asymmetric.utils;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.keys.CertificateX509;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;
import org.bouncycastle.operator.bc.BcECContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

@SuppressWarnings("LoggingSimilarMessage")
public enum AsymmetricSigningAlgorithm {

	RSA, ECDSA,
	;

	private static final Logger logger = LogManager.getLogger(AsymmetricSigningAlgorithm.class);

	public static AsymmetricSigningAlgorithm getAsymmetricSigningAlgorithm(String asymmetricSigningAlgorithm,
																		   Error error) {
		switch (asymmetricSigningAlgorithm.toUpperCase().trim()) {
			case "RSA":
				return AsymmetricSigningAlgorithm.RSA;
			case "ECDSA":
				return AsymmetricSigningAlgorithm.ECDSA;
			default:
				error.setError("AE005", "Unrecognized AsymmetricSigningAlgorithm");
				logger.error("Unrecognized AsymmetricSigningAlgorithm");
				return null;
		}
	}

	public static String valueOf(AsymmetricSigningAlgorithm asymmetricSigningAlgorithm, Error error) {
		switch (asymmetricSigningAlgorithm) {
			case RSA:
				return "RSA";
			case ECDSA:
				return "ECDSA";
			default:
				error.setError("AE006", "Unrecognized AsymmetricSigningAlgorithm");
				logger.error("Unrecognized AsymmetricSigningAlgorithm");
				return "";
		}
	}

	public static Signer getSigner(AsymmetricSigningAlgorithm asymmetricSigningAlgorithm, Digest hash, Error error) {
		logger.debug("getSigner");
		if (hash == null) {
			error.setError("AE008", "Hash digest is null");
			logger.error("Hash digest is null");
			return null;
		}
		switch (asymmetricSigningAlgorithm) {
			case RSA:
				return new RSADigestSigner(hash);
			case ECDSA:
				ECDSASigner dsaSigner = new ECDSASigner();
				return new DSADigestSigner(dsaSigner, hash);
			default:
				error.setError("AE007", "Unrecognized AsymmetricSigningAlgorithm");
				logger.error("Unrecognized AsymmetricSigningAlgorithm");
				return null;
		}
	}

	public static BcContentSignerBuilder getBcContentSignerBuilder(AsymmetricSigningAlgorithm asymmetricSigningAlgorithm, CertificateX509 cert, Error error) {
		logger.debug("getBcContentSignerBuilder");
		AlgorithmIdentifier signatureAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find(
			cert.Cert().getSigAlgName());
		AlgorithmIdentifier digestAlgorithm = new DefaultDigestAlgorithmIdentifierFinder().find(signatureAlgorithm);

		switch (asymmetricSigningAlgorithm) {
			case RSA:
				return new BcRSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm);
			case ECDSA:
				return new BcECContentSignerBuilder(signatureAlgorithm, digestAlgorithm);
			default:
				error.setError("AE007", "Unrecognized AsymmetricSigningAlgorithm");
				logger.error("Unrecognized AsymmetricSigningAlgorithm");
				return null;
		}
	}
}
