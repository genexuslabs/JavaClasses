package com.genexus.cryptography.test.checksum;

import com.genexus.cryptography.checksum.ChecksumCreator;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChecksumTest extends SecurityAPITestObject {

	private static String[] algorithms;
	private static String[] results;
	private static String[] results0x;
	private static String[] inputTypes;
	private static String[] inputs;

	@BeforeClass
	public static void setUp() {
		new EncodingUtil().setEncoding("UTF8");
		String input = "123456789";
		String b64Input = "MTIzNDU2Nzg5";
		String hexaInput = "313233343536373839";

		inputs = new String[]{input, input, b64Input, hexaInput};
		inputTypes = new String[]{"ASCII", "TXT", "BASE64", "HEX"};
		algorithms = new String[]{"CRC8", "CRC8_CDMA2000", "CRC8_DARC", "CRC8_DVB_S2", "CRC8_EBU", "CRC8_I_CODE",
			"CRC8_ITU", "CRC8_MAXIM", "CRC8_ROHC", "CRC8_WCDMA", "CRC16_AUG_CCITT", "CRC16_CCITT_FALSE",
			"CRC16_ARC", "CRC16_BUYPASS", "CRC16_CDMA2000", "CRC16_DDS_110", "CRC16_DECT_R", "CRC16_DECT_X",
			"CRC16_DNP", "CRC16_EN_13757", "CRC16_GENIBUS", "CRC16_MAXIM", "CRC16_MCRF4XX", "CRC16_RIELLO",
			"CRC16_T10_DIF", "CRC16_TELEDISK", "CRC16_TMS_37157", "CRC16_USB", "CRC_A", "CRC16_KERMIT",
			"CRC16_MODBUS", "CRC16_X_25", "CRC16_XMODEM", "CRC32", "CRC32_BZIP2", "CRC32C", "CRC32D",
			"CRC32_MPEG_2", "CRC32_POSIX", "CRC32Q", "CRC32_JAMCRC", "CRC32_XFER", "MD5", "SHA1", "SHA256",
			"SHA512"};
		results = new String[]{"F4", "DA", "15", "BC", "97", "7E", "A1", "A1", "D0", "25", "E5CC", "29B1", "BB3D",
			"FEE8", "4C06", "9ECF", "007E", "007F", "EA82", "C2B7", "D64E", "44C2", "6F91", "63D0", "D0DB", "0FB3",
			"26B1", "B4C8", "BF05", "2189", "4B37", "906E", "31C3", "CBF43926", "FC891918", "E3069283", "87315576",
			"0376E6E7", "765E7680", "3010BF7F", "340BC6D9", "BD0BE338", "25F9E794323B453885F5181F1B624D0B",
			"F7C3BC1D808E04732ADF679965CCC34CA7AE3441",
			"15E2B0D3C33891EBB0F1EF609EC419420C20E320CE94C65FBC8C3312448EB225",
			"D9E6762DD1C8EAF6D61B3C6192FC408D4D6D5F1176D0C29169BC24E71C3F274AD27FCD5811B313D681F7E55EC02D73D499C95455B6B5BB503ACF574FBA8FFE85"};
		results0x = new String[]{"0xF4", "0xDA", "0x15", "0xBC", "0x97", "0x7E", "0xA1", "0xA1", "0xD0", "0x25",
			"0xE5CC", "0x29B1", "0xBB3D", "0xFEE8", "0x4C06", "0x9ECF", "0x007E", "0x007F", "0xEA82", "0xC2B7",
			"0xD64E", "0x44C2", "0x6F91", "0x63D0", "0xD0DB", "0x0FB3", "0x26B1", "0xB4C8", "0xBF05", "0x2189",
			"0x4B37", "0x906E", "0x31C3", "0xCBF43926", "0xFC891918", "0xE3069283", "0x87315576", "0x0376E6E7",
			"0x765E7680", "0x3010BF7F", "0x340BC6D9", "0xBD0BE338", "0x25F9E794323B453885F5181F1B624D0B",
			"0xF7C3BC1D808E04732ADF679965CCC34CA7AE3441",
			"0x15E2B0D3C33891EBB0F1EF609EC419420C20E320CE94C65FBC8C3312448EB225",
			"0xD9E6762DD1C8EAF6D61B3C6192FC408D4D6D5F1176D0C29169BC24E71C3F274AD27FCD5811B313D681F7E55EC02D73D499C95455B6B5BB503ACF574FBA8FFE85"};
	}

	@Test
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

	@Test
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

}
