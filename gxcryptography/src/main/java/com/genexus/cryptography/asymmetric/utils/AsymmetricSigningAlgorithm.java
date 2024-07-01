package com.genexus.cryptography.asymmetric.utils;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.RSADigestSigner;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.keys.CertificateX509;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;
import org.bouncycastle.operator.bc.BcECContentSignerBuilder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

/**
 * @author sgrampone
 *
 */
public enum AsymmetricSigningAlgorithm {

	RSA, ECDSA,;

	/**
	 * Mapping between String name and AsymmetricSigningAlgorithm enum
	 * representation
	 *
	 * @param asymmetricSigningAlgorithm
	 *            String
	 * @param error
	 *            Error type for error management
	 * @return AsymmetricSigningAlgorithm enum representation
	 */
	public static AsymmetricSigningAlgorithm getAsymmetricSigningAlgorithm(String asymmetricSigningAlgorithm,
																		   Error error) {
		switch (asymmetricSigningAlgorithm.toUpperCase().trim()) {
			case "RSA":
				return AsymmetricSigningAlgorithm.RSA;
			case "ECDSA":
				return AsymmetricSigningAlgorithm.ECDSA;
			default:
				error.setError("AE005", "Unrecognized AsymmetricSigningAlgorithm");
				return null;
		}
	}

	/**
	 * @param asymmetricSigningAlgorithm
	 *            AsymmetricSigningAlgorithm enum, algorithm name
	 * @param error
	 *            Error type for error management
	 * @return String value of the algorithm
	 */
	public static String valueOf(AsymmetricSigningAlgorithm asymmetricSigningAlgorithm, Error error) {
		switch (asymmetricSigningAlgorithm) {
			case RSA:
				return "RSA";
			case ECDSA:
				return "ECDSA";
			default:
				error.setError("AE006", "Unrecognized AsymmetricSigningAlgorithm");
				return "";
		}
	}

	public static Signer getSigner(AsymmetricSigningAlgorithm asymmetricSigningAlgorithm, Digest hash, Error error)
	{
		if(hash == null)
		{
			error.setError("AE008", "Hash digest is null");
			return null;
		}
		Signer sig = null;
		switch (asymmetricSigningAlgorithm) {
			case RSA:
				sig = new RSADigestSigner(hash);
				break;
			case ECDSA:
				ECDSASigner dsaSigner = new ECDSASigner();
				sig = new DSADigestSigner(dsaSigner, hash);
				break;
			default:
				error.setError("AE007", "Unrecognized AsymmetricSigningAlgorithm");
		}
		return sig;
	}

	public static BcContentSignerBuilder getBcContentSignerBuilder(AsymmetricSigningAlgorithm asymmetricSigningAlgorithm, CertificateX509 cert, Error error)
	{
		AlgorithmIdentifier signatureAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find(
			cert.Cert().getSigAlgName());
		AlgorithmIdentifier digestAlgorithm = new DefaultDigestAlgorithmIdentifierFinder().find(signatureAlgorithm);

		BcContentSignerBuilder sig = null;
		switch (asymmetricSigningAlgorithm) {
			case RSA:
				sig = new BcRSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm);
				break;
			case ECDSA:
				sig = new BcECContentSignerBuilder(signatureAlgorithm, digestAlgorithm);
				break;
			default:
				error.setError("AE007", "Unrecognized AsymmetricSigningAlgorithm");
		}
		return sig;
	}
}
