package com.genexus.cryptography.test.checksum;

import com.genexus.cryptography.checksum.ChecksumCreator;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ChecksumFileTest extends SecurityAPITestObject {

	private static String localFileInput;
	private static String[] algorithms;
	private static String[] resultsLocalFile;
	private static String[] resultsLocalFile0x;

	@Override
	protected void setUp() {
		new EncodingUtil().setEncoding("UTF8");

		localFileInput = resources.concat("/bookSample.xml");

		algorithms = new String[] { "CRC8", "CRC8_CDMA2000"/*, "CRC8_DARC", "CRC8_DVB_S2", "CRC8_EBU", "CRC8_I_CODE",
			"CRC8_ITU", "CRC8_MAXIM", "CRC8_ROHC", "CRC8_WCDMA", "CRC16_AUG_CCITT", "CRC16_CCITT_FALSE",
			"CRC16_ARC", "CRC16_BUYPASS", "CRC16_CDMA2000", "CRC16_DDS_110", "CRC16_DECT_R", "CRC16_DECT_X",
			"CRC16_DNP", "CRC16_EN_13757", "CRC16_GENIBUS", "CRC16_MAXIM", "CRC16_MCRF4XX", "CRC16_RIELLO",
			"CRC16_T10_DIF", "CRC16_TELEDISK", "CRC16_TMS_37157", "CRC16_USB", "CRC_A", "CRC16_KERMIT",
			"CRC16_MODBUS", "CRC16_X_25", "CRC16_XMODEM", "CRC32", "CRC32_BZIP2", "CRC32C", "CRC32D",
			"CRC32_MPEG_2", "CRC32_POSIX", "CRC32Q", "CRC32_JAMCRC", "CRC32_XFER", "MD5", "SHA1", "SHA256",
			"SHA512" */};
		resultsLocalFile = new String[] { "AD", "C2"/*, "C0", "29", "AC", "C6", "F8", "58", "E9", "BA", "E7BF", "EB02",
			"7982", "8DC7", "8023", "347E", "78ED", "78EC", "1223", "F11E", "14FD", "867D", "8690", "8656", "89CC",
			"D6A3", "FF40", "1452", "47D3", "12A7", "EBAD", "796F", "072B", "3172BC69", "18D27AE6", "8C403DED",
			"3B83323F", "E72D8519", "981F81DA", "323A5936", "CE8D4396", "ABBC544E",
			"19D987515BC23D6D8A29AD2A063A58ED", "1DCC48655437412E56A0B66E214DE0A91B0F888F",
			"5248D4A7D1A6588AB8BABDE9487D43579CA957DED6ED553AF04D506702100C9F",
			"5C7B1671D81B37FB4A1B8DF4E23091D10181F2D3559BD3B5515C23F8F042CF8A60E26EAC5A70D8E2AC7B8585CAAB7F42480213D5771FC5430E550F2D429349FA" */};
		resultsLocalFile0x = new String[] { "0xAD", "0xC2", "0xC0", "0x29", "0xAC", "0xC6", "0xF8", "0x58", "0xE9",
			"0xBA", "0xE7BF", "0xEB02", "0x7982", "0x8DC7", "0x8023", "0x347E", "0x78ED", "0x78EC", "0x1223",
			"0xF11E", "0x14FD", "0x867D", "0x8690", "0x8656", "0x89CC", "0xD6A3", "0xFF40", "0x1452", "0x47D3",
			"0x12A7", "0xEBAD", "0x796F", "0x072B", "0x3172BC69", "0x18D27AE6", "0x8C403DED", "0x3B83323F",
			"0xE72D8519", "0x981F81DA", "0x323A5936", "0xCE8D4396", "0xABBC544E",
			"0x19D987515BC23D6D8A29AD2A063A58ED", "0x1DCC48655437412E56A0B66E214DE0A91B0F888F",
			"0x5248D4A7D1A6588AB8BABDE9487D43579CA957DED6ED553AF04D506702100C9F",
			"0x5C7B1671D81B37FB4A1B8DF4E23091D10181F2D3559BD3B5515C23F8F042CF8A60E26EAC5A70D8E2AC7B8585CAAB7F42480213D5771FC5430E550F2D429349FA" };
	}

	public static Test suite() {
		return new TestSuite(ChecksumFileTest.class);
	}

	@Override
	public void runTest() {
		testBulkChecksum_LocalFile();
		//testBulkChecksum0x_LocalFile();
	}


	public void testBulkChecksum_LocalFile() {
		ChecksumCreator check = new ChecksumCreator();
		for (int k = 0; k < algorithms.length; k++) {
			System.out.println("localFileInput: " + localFileInput);

			String checksum = check.generateChecksum(localFileInput, "LOCAL_FILE", algorithms[k]);
			//assertTrue(SecurityUtils.compareStrings(checksum, resultsLocalFile[k]));
			System.out.println("Error generate. Code: " + check.getErrorCode() + " Desc: " + check.getErrorDescription());
			boolean verify = check.verifyChecksum(localFileInput, "LOCAL_FILE", algorithms[k], checksum);
			System.out.println("Error verify. Code: " + check.getErrorCode() + " Desc: " + check.getErrorDescription());
			System.out.println("verify: " + verify + " checksum: " + checksum + " expected: " + resultsLocalFile[k] + " algorithm: " + algorithms[k]);
			True(verify, check);
		}
	}

	/*public void testBulkChecksum0x_LocalFile() {
		ChecksumCreator check = new ChecksumCreator();
		for (int k = 0; k < algorithms.length; k++) {
			String checksum = check.generateChecksum(localFileInput, "LOCAL_FILE", algorithms[k]);
			assertTrue(SecurityUtils.compareStrings(checksum, resultsLocalFile[k]));
			boolean verify = check.verifyChecksum(localFileInput, "LOCAL_FILE", algorithms[k],
				resultsLocalFile0x[k].toLowerCase());
			True(verify, check);
		}
	}*/

}


