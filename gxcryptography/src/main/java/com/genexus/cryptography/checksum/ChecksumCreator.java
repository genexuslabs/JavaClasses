package com.genexus.cryptography.checksum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.bouncycastle.util.encoders.Hex;

import com.genexus.cryptography.checksum.utils.CRCParameters;
import com.genexus.cryptography.checksum.utils.ChecksumAlgorithm;
import com.genexus.cryptography.checksum.utils.ChecksumInputType;
import com.genexus.cryptography.commons.ChecksumObject;
import com.genexus.cryptography.hash.Hashing;
import com.genexus.cryptography.hash.utils.HashAlgorithm;
import com.genexus.securityapicommons.utils.SecurityUtils;

public class ChecksumCreator extends ChecksumObject {

	public ChecksumCreator() {
		super();
	}

	/********EXTERNAL OBJECT PUBLIC METHODS  - BEGIN ********/

	public String generateChecksum(String input, String inputType, String checksumAlgorithm) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("input", input, this.error);
		SecurityUtils.validateStringInput("inputType", inputType, this.error);
		SecurityUtils.validateStringInput("checksumAlgorithm", checksumAlgorithm, this.error);
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		ChecksumInputType chksumInputType = ChecksumInputType.getChecksumInputType(inputType, this.error);
		byte[] inputBytes = ChecksumInputType.getBytes(chksumInputType, input, this.error);
		if (this.hasError()) {
			return "";
		}
		ChecksumAlgorithm algorithm = ChecksumAlgorithm.getChecksumAlgorithm(checksumAlgorithm, this.error);
		if (this.hasError()) {
			return "";
		}
		return (ChecksumAlgorithm.isHash(algorithm)) ? calculateHash(inputBytes, algorithm)
			: calculateCRC(inputBytes, algorithm);
	}

	public boolean verifyChecksum(String input, String inputType, String checksumAlgorithm, String digest)
	{
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("input", input, this.error);
		SecurityUtils.validateStringInput("inputType", inputType, this.error);
		SecurityUtils.validateStringInput("checksumAlgorithm", checksumAlgorithm, this.error);
		SecurityUtils.validateStringInput("digest", digest, this.error);
		if(this.hasError()) { return false;};
		/*******INPUT VERIFICATION - END*******/

		String result = generateChecksum(input,  inputType,  checksumAlgorithm);
		if(SecurityUtils.compareStrings(result, "") || this.hasError())
		{
			return false;
		}
		String resCompare = digest.toUpperCase().contains("0X") ? "0X"+result : result;
		return SecurityUtils.compareStrings(resCompare, digest);
	}

	/********EXTERNAL OBJECT PUBLIC METHODS  - END ********/

	private String calculateCRC(byte[] input, ChecksumAlgorithm checksumAlgorithm) {
		CRCParameters parms = ChecksumAlgorithm.getParameters(checksumAlgorithm, this.error);
		if (this.hasError()) {
			return "";
		}
		long aux = calculateCRC(input, parms);
		if (aux == 0 || this.hasError()) {
			return "";
		}
		switch(parms.getWidth())
		{
			case 8:
				return String.format("%02X", aux);
			case 16:
				return String.format("%04X", aux);
			case 32:
				return String.format("%08X", aux);
			default:
				return Long.toHexString(aux);
		}
	}

	private String calculateHash(byte[] input, ChecksumAlgorithm checksumAlgorithm) {
		HashAlgorithm alg = getHashAlgorithm(checksumAlgorithm);
		if (this.hasError()) {
			return "";
		}
		Hashing hash = new Hashing();
		byte[] digest = null;
		try (InputStream inputStream = new ByteArrayInputStream(input)) {
			digest = hash.calculateHash(alg, inputStream);
		} catch (Exception e) {
			error.setError("CH001", e.getMessage());
			return "";
		}
		if (hash.hasError()) {
			this.error = hash.getError();
			return "";
		}
		return Hex.toHexString(digest);
	}

	private HashAlgorithm getHashAlgorithm(ChecksumAlgorithm checksumAlgorithm) {
		return HashAlgorithm.getHashAlgorithm(ChecksumAlgorithm.valueOf(checksumAlgorithm, this.error), this.error);
	}

	private long calculateCRC(byte[] input, CRCParameters parms) {

		long curValue = parms.getInit();
		long topBit = 1L << (parms.getWidth() - 1);
		long mask = (topBit << 1) - 1;

		for (int i = 0; i < input.length; i++) {
			long curByte = ((long) (input[i])) & 0x00FFL;
			if (parms.getReflectIn()) {
				curByte = reflect(curByte, 8);
			}

			for (int j = 0x80; j != 0; j >>= 1) {
				long bit = curValue & topBit;
				curValue <<= 1;

				if ((curByte & j) != 0) {
					bit ^= topBit;
				}

				if (bit != 0) {
					curValue ^= parms.getPolynomial();
				}
			}

		}

		if (parms.getReflectOut()) {
			curValue = reflect(curValue, parms.getWidth());
		}

		curValue = curValue ^ parms.getFinalXor();

		return curValue & mask;
	}

	private long reflect(long in, int count) {
		long ret = in;
		for (int idx = 0; idx < count; idx++) {
			long srcbit = 1L << idx;
			long dstbit = 1L << (count - idx - 1);
			if ((in & srcbit) != 0) {
				ret |= dstbit;
			} else {
				ret = ret & (~dstbit);
			}
		}
		return ret;
	}

}
