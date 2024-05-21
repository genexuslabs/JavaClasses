package com.genexus.cryptography.test.checksum;

import com.genexus.cryptography.checksum.ChecksumCreator;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ChecksumTest extends SecurityAPITestObject {

	private static String input;
	private static String b64Input;
	private static String hexaInput;
	private static String localFileInput;
	private static String[] algorithms;
	private static String[] results;
	private static String[] results0x;
	private static String[] resultsLocalFile;
	private static String[] resultsLocalFile0x;
	private static String[] inputTypes;
	private static String[] inputs;

	@Override
	protected void setUp() {
		new EncodingUtil().setEncoding("UTF8");
		input = "123456789";
		b64Input = "MTIzNDU2Nzg5";
		hexaInput = "313233343536373839";
		localFileInput = resources.concat("/bookSample.xml");

		inputs = new String[] { input, input, b64Input, hexaInput };
		inputTypes = new String[] { "ASCII", "TXT", "BASE64", "HEX" };
		algorithms = new String[] { "CRC8", "CRC8_CDMA2000", "CRC8_DARC", "CRC8_DVB_S2", "CRC8_EBU", "CRC8_I_CODE",
			"CRC8_ITU", "CRC8_MAXIM", "CRC8_ROHC", "CRC8_WCDMA", "CRC16_AUG_CCITT", "CRC16_CCITT_FALSE",
			"CRC16_ARC", "CRC16_BUYPASS", "CRC16_CDMA2000", "CRC16_DDS_110", "CRC16_DECT_R", "CRC16_DECT_X",
			"CRC16_DNP", "CRC16_EN_13757", "CRC16_GENIBUS", "CRC16_MAXIM", "CRC16_MCRF4XX", "CRC16_RIELLO",
			"CRC16_T10_DIF", "CRC16_TELEDISK", "CRC16_TMS_37157", "CRC16_USB", "CRC_A", "CRC16_KERMIT",
			"CRC16_MODBUS", "CRC16_X_25", "CRC16_XMODEM", "CRC32", "CRC32_BZIP2", "CRC32C", "CRC32D",
			"CRC32_MPEG_2", "CRC32_POSIX", "CRC32Q", "CRC32_JAMCRC", "CRC32_XFER", "MD5", "SHA1", "SHA256",
			"SHA512" };
		results = new String[] { "F4", "DA", "15", "BC", "97", "7E", "A1", "A1", "D0", "25", "E5CC", "29B1", "BB3D",
			"FEE8", "4C06", "9ECF", "007E", "007F", "EA82", "C2B7", "D64E", "44C2", "6F91", "63D0", "D0DB", "0FB3",
			"26B1", "B4C8", "BF05", "2189", "4B37", "906E", "31C3", "CBF43926", "FC891918", "E3069283", "87315576",
			"0376E6E7", "765E7680", "3010BF7F", "340BC6D9", "BD0BE338", "25F9E794323B453885F5181F1B624D0B",
			"F7C3BC1D808E04732ADF679965CCC34CA7AE3441",
			"15E2B0D3C33891EBB0F1EF609EC419420C20E320CE94C65FBC8C3312448EB225",
			"D9E6762DD1C8EAF6D61B3C6192FC408D4D6D5F1176D0C29169BC24E71C3F274AD27FCD5811B313D681F7E55EC02D73D499C95455B6B5BB503ACF574FBA8FFE85" };
		results0x = new String[] { "0xF4", "0xDA", "0x15", "0xBC", "0x97", "0x7E", "0xA1", "0xA1", "0xD0", "0x25",
			"0xE5CC", "0x29B1", "0xBB3D", "0xFEE8", "0x4C06", "0x9ECF", "0x007E", "0x007F", "0xEA82", "0xC2B7",
			"0xD64E", "0x44C2", "0x6F91", "0x63D0", "0xD0DB", "0x0FB3", "0x26B1", "0xB4C8", "0xBF05", "0x2189",
			"0x4B37", "0x906E", "0x31C3", "0xCBF43926", "0xFC891918", "0xE3069283", "0x87315576", "0x0376E6E7",
			"0x765E7680", "0x3010BF7F", "0x340BC6D9", "0xBD0BE338", "0x25F9E794323B453885F5181F1B624D0B",
			"0xF7C3BC1D808E04732ADF679965CCC34CA7AE3441",
			"0x15E2B0D3C33891EBB0F1EF609EC419420C20E320CE94C65FBC8C3312448EB225",
			"0xD9E6762DD1C8EAF6D61B3C6192FC408D4D6D5F1176D0C29169BC24E71C3F274AD27FCD5811B313D681F7E55EC02D73D499C95455B6B5BB503ACF574FBA8FFE85" };
		resultsLocalFile = new String[] { "AD", "C2", "C0", "29", "AC", "C6", "F8", "58", "E9", "BA", "E7BF", "EB02",
			"7982", "8DC7", "8023", "347E", "78ED", "78EC", "1223", "F11E", "14FD", "867D", "8690", "8656", "89CC",
			"D6A3", "FF40", "1452", "47D3", "12A7", "EBAD", "796F", "072B", "3172BC69", "18D27AE6", "8C403DED",
			"3B83323F", "E72D8519", "981F81DA", "323A5936", "CE8D4396", "ABBC544E",
			"19D987515BC23D6D8A29AD2A063A58ED", "1DCC48655437412E56A0B66E214DE0A91B0F888F",
			"5248D4A7D1A6588AB8BABDE9487D43579CA957DED6ED553AF04D506702100C9F",
			"5C7B1671D81B37FB4A1B8DF4E23091D10181F2D3559BD3B5515C23F8F042CF8A60E26EAC5A70D8E2AC7B8585CAAB7F42480213D5771FC5430E550F2D429349FA" };
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
		return new TestSuite(ChecksumTest.class);
	}

	@Override
	public void runTest() {
		testBulkChecksum();
		//testBulkChecksum_LocalFile();
		testBulkChecksum0x();
		//testBulkChecksum0x_LocalFile();*/
	}

	public void testBulkChecksum() {
		ChecksumCreator check = new ChecksumCreator();
		for (int j = 0; j < inputs.length; j++) {
			for (int i = 0; i < algorithms.length; i++) {
				String checksum = check.generateChecksum(inputs[j], inputTypes[j], algorithms[i]);
				assertTrue(SecurityUtils.compareStrings(checksum, results[i]));
				boolean verify = check.verifyChecksum(inputs[j], inputTypes[j], algorithms[i], checksum);
				True(verify, check);
			}
		}

	}

	public void testBulkChecksum0x() {
		ChecksumCreator check = new ChecksumCreator();
		for (int j = 0; j < inputs.length; j++) {
			for (int i = 0; i < algorithms.length; i++) {
				String checksum = check.generateChecksum(inputs[j], inputTypes[j], algorithms[i]);
				assertTrue(SecurityUtils.compareStrings(checksum, results[i]));
				boolean verify = check.verifyChecksum(inputs[j], inputTypes[j], algorithms[i], results0x[i].toLowerCase());
				True(verify, check);
			}
		}

	}

	/*public void testBulkChecksum_LocalFile() {
		ChecksumCreator check = new ChecksumCreator();
		for (int k = 0; k < algorithms.length; k++) {
			String checksum = check.generateChecksum(localFileInput, "LOCAL_FILE", algorithms[k]);
			assertTrue(SecurityUtils.compareStrings(checksum, resultsLocalFile[k]));
			boolean verify = check.verifyChecksum(localFileInput, "LOCAL_FILE", algorithms[k], checksum);
			True(verify, check);
		}
	}

	public void testBulkChecksum0x_LocalFile() {
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
