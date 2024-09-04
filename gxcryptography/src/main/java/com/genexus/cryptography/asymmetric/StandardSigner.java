package com.genexus.cryptography.asymmetric;

import com.genexus.cryptography.asymmetric.utils.AsymmetricSigningAlgorithm;
import com.genexus.cryptography.asymmetric.utils.SignatureStandard;
import com.genexus.cryptography.asymmetric.utils.SignatureStandardOptions;
import com.genexus.cryptography.commons.StandardSignerObject;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StandardSigner extends StandardSignerObject {

	public StandardSigner() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public String sign(String plainText, SignatureStandardOptions options)
	{
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("signatureStandardOptions", options, this.error);
		SecurityUtils.validateObjectInput("private key", options.getPrivateKey(), this.error);
		SecurityUtils.validateObjectInput("certificate", options.getCertificate(), this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}

		/******* INPUT VERIFICATION - END *******/

		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}

		String result = "";
		try  {
			result = sign_internal(inputText, options.getPrivateKey(), options.getCertificate(), options.getSignatureStandard(), options.getEncapsulated());
		} catch (Exception e) {
			error.setError("SS002", e.getMessage());
			result = "";
		}

		return result;
	}

	public boolean verify(String signed, String plainText, SignatureStandardOptions options)
	{
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("signatureStandardOptions", options, this.error);
		//SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateStringInput("signed", signed, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		EncodingUtil eu = new EncodingUtil();
		byte[] plainText_bytes = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return false;
		}

		boolean result = false;
		try  {
			result = verify_internal(Base64.decode(signed), plainText_bytes, options.getEncapsulated());
		} catch (Exception e) {
			error.setError("SS002", e.getMessage());
			e.printStackTrace();
			result = false;
		}

		return result;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private String sign_internal(byte[] input, PrivateKeyManager key, CertificateX509 cert, SignatureStandard signatureStandard, boolean encapsulated) throws OperatorCreationException, CertificateEncodingException, CMSException, IOException {
		PrivateKeyManager keyMan = (PrivateKeyManager) key;
		if (keyMan.hasError()) {
			this.error = keyMan.getError();
			return "";
		}
		CertificateX509 certificate = (CertificateX509) cert;
		if (certificate.hasError()) {
			this.error = certificate.getError();
			return "";
		}
		AsymmetricSigningAlgorithm asymmetricSigningAlgorithm = AsymmetricSigningAlgorithm
			.getAsymmetricSigningAlgorithm(keyMan.getAlgorithm(), this.error);
		BcContentSignerBuilder bcContentSignerBuilder = AsymmetricSigningAlgorithm.getBcContentSignerBuilder(asymmetricSigningAlgorithm, certificate, this.error);
		if (this.hasError())
			return "";

		List<X509Certificate> certList = new ArrayList<X509Certificate>();
		certList.add(cert.Cert());
		Store certs = new JcaCertStore(certList);

		DigestCalculatorProvider digestCalculatorProvider = new BcDigestCalculatorProvider();
		JcaSignerInfoGeneratorBuilder signerInfoGeneratorBuilder = new JcaSignerInfoGeneratorBuilder(digestCalculatorProvider);
		ContentSigner signer = bcContentSignerBuilder.build(keyMan.getAsymmetricKeyParameter());

		CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

		gen.addSignerInfoGenerator(signerInfoGeneratorBuilder.build(signer, cert.Cert()));

		gen.addCertificates(certs);

		CMSTypedData msg = new CMSProcessableByteArray(input);
		byte[] encoded = gen.generate(msg,encapsulated).getEncoded();
		return Base64.toBase64String(encoded);
	}

	private boolean verify_internal(byte[] cmsSignedData, byte[] data, boolean encapsulated)
		throws GeneralSecurityException, OperatorCreationException, CMSException {

		CMSSignedData signedData = encapsulated ? new CMSSignedData(cmsSignedData): new CMSSignedData(new CMSProcessableByteArray(data), cmsSignedData);

		Store certStore = signedData.getCertificates();
		SignerInformationStore signers = signedData.getSignerInfos();

		Collection c = signers.getSigners();
		Iterator it = c.iterator();

		while (it.hasNext())
		{

			SignerInformation signer = (SignerInformation)it.next();
			Collection certCollection = certStore.getMatches(signer.getSID());

			Iterator certIt = certCollection.iterator();
			X509CertificateHolder cert1 = (X509CertificateHolder)certIt.next();

			SignerInformationVerifier signerInformationVerifier = new JcaSimpleSignerInfoVerifierBuilder()
				.build(cert1);
			boolean verifies = signer.verify(signerInformationVerifier);
			if(!verifies)
			{
				return false;
			}
		}
		return true;
	}

}
