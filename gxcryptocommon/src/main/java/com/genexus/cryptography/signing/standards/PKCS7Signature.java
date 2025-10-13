package com.genexus.cryptography.signing.standards;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import com.genexus.cryptography.Utils;
import com.genexus.cryptography.exception.PrivateKeyNotFoundException;
import com.genexus.cryptography.exception.PublicKeyNotFoundException;
import com.genexus.cryptography.exception.SignatureException;
import com.genexus.cryptography.signing.IPkcsSign;

public class PKCS7Signature implements IPkcsSign {

	/**
	 * Signs given document with a given private key.
	 */
	private String _algorithm;
	private X509Certificate _cert;
	private PrivateKey _pKey;
	private Boolean _detached;

	public PKCS7Signature(String algorithm) {
		_algorithm = algorithm;
		initialize();
	}

	public PKCS7Signature(String algorithm, X509Certificate cert, PrivateKey key) {
		_algorithm = algorithm;
		_cert = cert;
		_pKey = key;
		initialize();
	}

	private void initialize() {
		Security.addProvider(new BouncyCastleProvider());
	}

	public String sign(byte[] data) throws GeneralSecurityException, CMSException, IOException,
			PublicKeyNotFoundException, PrivateKeyNotFoundException {
		if (_cert == null) {
			throw new PublicKeyNotFoundException();
		}
		if (_pKey == null) {
			throw new PrivateKeyNotFoundException();
		}

		ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();
		CMSTypedData msg = new CMSProcessableByteArray(data);

		certList.add(_cert);

		Store certs = new JcaCertStore(certList);

		try {
			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
			ContentSigner sha1Signer = new JcaContentSignerBuilder(_algorithm).setProvider("BC").build(_pKey);

			gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder()
					.setProvider("BC").build()).build(sha1Signer, _cert));
			gen.addCertificates(certs);

			CMSSignedData sigData = gen.generate(msg, !_detached);

			return Base64.encodeBase64String(sigData.getEncoded());
		} catch (OperatorCreationException e) {
			Utils.logError(e);
		}

		return "";
	}

	public boolean verify(byte[] data, byte[] aSignature) throws GeneralSecurityException, PublicKeyNotFoundException,
			SignatureException {
		initialize();
		CMSProcessableByteArray content = new CMSProcessableByteArray(data);
		int verified = 0;
		CMSSignedData signedData = null;

		try {
			if (_detached) {
				signedData = new CMSSignedData(content, aSignature);
			} else {
				signedData = new CMSSignedData(aSignature);
			}
		} catch (CMSException e) {
			Utils.logError(e);
			throw new SignatureException(e);
		}

		Store certStore = signedData.getCertificates();
		SignerInformationStore signers = signedData.getSignerInfos();
		Collection c = signers.getSigners();
		Iterator it = c.iterator();

		while (it.hasNext()) {
			SignerInformation signer = (SignerInformation) it.next();
			Collection certCollection = certStore.getMatches(signer.getSID());

			Iterator certIt = certCollection.iterator();
			X509CertificateHolder cert = (X509CertificateHolder) certIt.next();

			try {
				if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert))) {
					verified++;
				}
			} catch (Exception e) {
				Utils.logError(e);
				throw new SignatureException(e);
			}
		}
		return verified > 0;
	}

	public void setDetached(Boolean value) {
		_detached = value;
	}

	public void setCertificate(X509Certificate cert) {
		this._cert = cert;
	}

	public X509Certificate getCertificate() {
		return this._cert;
	}
}
