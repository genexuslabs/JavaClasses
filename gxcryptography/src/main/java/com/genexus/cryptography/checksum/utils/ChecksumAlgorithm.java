package com.genexus.cryptography.checksum.utils;

import com.genexus.securityapicommons.commons.Error;

public enum ChecksumAlgorithm {

	CRC8, CRC8_CDMA2000, CRC8_DARC, CRC8_DVB_S2, CRC8_EBU, CRC8_I_CODE, CRC8_ITU, CRC8_MAXIM, CRC8_ROHC, CRC8_WCDMA,
	CRC16_AUG_CCITT, CRC16_CCITT_FALSE, CRC16_ARC, CRC16_BUYPASS, CRC16_CDMA2000, CRC16_DDS_110, CRC16_DECT_R,
	CRC16_DECT_X, CRC16_DNP, CRC16_EN_13757, CRC16_GENIBUS, CRC16_MAXIM, CRC16_MCRF4XX, CRC16_RIELLO, CRC16_T10_DIF,
	CRC16_TELEDISK, CRC16_TMS_37157, CRC16_USB, CRC_A, CRC16_KERMIT, CRC16_MODBUS, CRC16_X_25, CRC16_XMODEM, CRC32,
	CRC32_BZIP2, CRC32C, CRC32D, CRC32_MPEG_2, CRC32_POSIX, CRC32Q, CRC32_JAMCRC, CRC32_XFER, MD5, SHA1, SHA256,
	SHA512,NONE;

	public static ChecksumAlgorithm getChecksumAlgorithm(String checksumAlgorithm, Error error) {
		if(error == null) return ChecksumAlgorithm.NONE;
		if (checksumAlgorithm == null)
		{
			error.setError("CHA04", "Unrecognized checksum algorithm");
			return ChecksumAlgorithm.NONE;
		}
		switch (checksumAlgorithm.toUpperCase().trim()) {
			case "CRC8":
				return ChecksumAlgorithm.CRC8;
			case "CRC8_CDMA2000":
				return ChecksumAlgorithm.CRC8_CDMA2000;
			case "CRC8_DARC":
				return ChecksumAlgorithm.CRC8_DARC;
			case "CRC8_DVB_S2":
				return ChecksumAlgorithm.CRC8_DVB_S2;
			case "CRC8_EBU":
				return ChecksumAlgorithm.CRC8_EBU;
			case "CRC8_I_CODE":
				return ChecksumAlgorithm.CRC8_I_CODE;
			case "CRC8_ITU":
				return ChecksumAlgorithm.CRC8_ITU;
			case "CRC8_MAXIM":
				return ChecksumAlgorithm.CRC8_MAXIM;
			case "CRC8_ROHC":
				return ChecksumAlgorithm.CRC8_ROHC;
			case "CRC8_WCDMA":
				return ChecksumAlgorithm.CRC8_WCDMA;
			case "CRC16_AUG_CCITT":
				return ChecksumAlgorithm.CRC16_AUG_CCITT;
			case "CRC16_CCITT_FALSE":
				return ChecksumAlgorithm.CRC16_CCITT_FALSE;
			case "CRC16_ARC":
				return ChecksumAlgorithm.CRC16_ARC;
			case "CRC16_BUYPASS":
				return ChecksumAlgorithm.CRC16_BUYPASS;
			case "CRC16_CDMA2000":
				return ChecksumAlgorithm.CRC16_CDMA2000;
			case "CRC16_DDS_110":
				return ChecksumAlgorithm.CRC16_DDS_110;
			case "CRC16_DECT_R":
				return ChecksumAlgorithm.CRC16_DECT_R;
			case "CRC16_DECT_X":
				return ChecksumAlgorithm.CRC16_DECT_X;
			case "CRC16_DNP":
				return ChecksumAlgorithm.CRC16_DNP;
			case "CRC16_EN_13757":
				return ChecksumAlgorithm.CRC16_EN_13757;
			case "CRC16_GENIBUS":
				return ChecksumAlgorithm.CRC16_GENIBUS;
			case "CRC16_MAXIM":
				return ChecksumAlgorithm.CRC16_MAXIM;
			case "CRC16_MCRF4XX":
				return ChecksumAlgorithm.CRC16_MCRF4XX;
			case "CRC16_RIELLO":
				return ChecksumAlgorithm.CRC16_RIELLO;
			case "CRC16_T10_DIF":
				return ChecksumAlgorithm.CRC16_T10_DIF;
			case "CRC16_TELEDISK":
				return ChecksumAlgorithm.CRC16_TELEDISK;
			case "CRC16_TMS_37157":
				return ChecksumAlgorithm.CRC16_TMS_37157;
			case "CRC16_USB":
				return ChecksumAlgorithm.CRC16_USB;
			case "CRC_A":
				return ChecksumAlgorithm.CRC_A;
			case "CRC16_KERMIT":
				return ChecksumAlgorithm.CRC16_KERMIT;
			case "CRC16_MODBUS":
				return ChecksumAlgorithm.CRC16_MODBUS;
			case "CRC16_X_25":
				return ChecksumAlgorithm.CRC16_X_25;
			case "CRC16_XMODEM":
				return ChecksumAlgorithm.CRC16_XMODEM;
			case "CRC32":
				return ChecksumAlgorithm.CRC32;
			case "CRC32_BZIP2":
				return ChecksumAlgorithm.CRC32_BZIP2;
			case "CRC32C":
				return ChecksumAlgorithm.CRC32C;
			case "CRC32D":
				return ChecksumAlgorithm.CRC32D;
			case "CRC32_MPEG_2":
				return ChecksumAlgorithm.CRC32_MPEG_2;
			case "CRC32_POSIX":
				return ChecksumAlgorithm.CRC32_POSIX;
			case "CRC32Q":
				return ChecksumAlgorithm.CRC32Q;
			case "CRC32_JAMCRC":
				return ChecksumAlgorithm.CRC32_JAMCRC;
			case "CRC32_XFER":
				return ChecksumAlgorithm.CRC32_XFER;
			case "MD5":
				return ChecksumAlgorithm.MD5;
			case "SHA1":
				return ChecksumAlgorithm.SHA1;
			case "SHA256":
				return ChecksumAlgorithm.SHA256;
			case "SHA512":
				return ChecksumAlgorithm.SHA512;
			default:
				error.setError("CHA01", "Unrecognized checksum algorithm");
				return null;
		}
	}

	public static String valueOf(ChecksumAlgorithm checksumAlgorithm, Error error) {
		if (error == null) return null;
		switch (checksumAlgorithm) {
			case CRC8:
				return "CRC8";
			case CRC8_CDMA2000:
				return "CRC8_CDMA2000";
			case CRC8_DARC:
				return "CRC8_DARC";
			case CRC8_DVB_S2:
				return "CRC8_DVB_S2";
			case CRC8_EBU:
				return "CRC8_EBU";
			case CRC8_I_CODE:
				return "CRC8_I_CODE";
			case CRC8_ITU:
				return "CRC8_ITU";
			case CRC8_MAXIM:
				return "CRC8_MAXIM";
			case CRC8_ROHC:
				return "CRC8_ROHC";
			case CRC8_WCDMA:
				return "CRC8_WCDMA";
			case CRC16_AUG_CCITT:
				return "CRC16_AUG_CCITT";
			case CRC16_CCITT_FALSE:
				return "CRC16_CCITT_FALSE";
			case CRC16_ARC:
				return "CRC16_ARC";
			case CRC16_BUYPASS:
				return "CRC16_BUYPASS";
			case CRC16_CDMA2000:
				return "CRC16_CDMA2000";
			case CRC16_DDS_110:
				return "CRC16_DDS_110";
			case CRC16_DECT_R:
				return "CRC16_DECT_R";
			case CRC16_DECT_X:
				return "CRC16_DECT_X";
			case CRC16_DNP:
				return "CRC16_DNP";
			case CRC16_EN_13757:
				return "CRC16_EN_13757";
			case CRC16_GENIBUS:
				return "CRC16_GENIBUS";
			case CRC16_MAXIM:
				return "CRC16_MAXIM";
			case CRC16_MCRF4XX:
				return "CRC16_MCRF4XX";
			case CRC16_RIELLO:
				return "CRC16_RIELLO";
			case CRC16_T10_DIF:
				return "CRC16_T10_DIF";
			case CRC16_TELEDISK:
				return "CRC16_TELEDISK";
			case CRC16_TMS_37157:
				return "CRC16_TMS_37157";
			case CRC16_USB:
				return "CRC16_USB";
			case CRC_A:
				return "CRC_A";
			case CRC16_KERMIT:
				return "CRC16_KERMIT";
			case CRC16_MODBUS:
				return "CRC16_MODBUS";
			case CRC16_X_25:
				return "CRC16_X_25";
			case CRC16_XMODEM:
				return "CRC16_XMODEM";
			case CRC32:
				return "CRC32";
			case CRC32_BZIP2:
				return "CRC32_BZIP2";
			case CRC32C:
				return "CRC32C";
			case CRC32D:
				return "CRC32D";
			case CRC32_MPEG_2:
				return "CRC32_MPEG_2";
			case CRC32_POSIX:
				return "CRC32_POSIX";
			case CRC32Q:
				return "CRC32Q";
			case CRC32_JAMCRC:
				return "CRC32_JAMCRC";
			case CRC32_XFER:
				return "CRC32_XFER";
			case MD5:
				return "MD5";
			case SHA1:
				return "SHA1";
			case SHA256:
				return "SHA256";
			case SHA512:
				return "SHA512";
			default:
				error.setError("CHA02", "Unrecognized checksum algorithm");
				return null;
		}
	}

	public static boolean isHash(ChecksumAlgorithm checksumAlgorithm)
	{
		switch(checksumAlgorithm)
		{
			case MD5:
			case SHA1:
			case SHA256:
			case SHA512:
				return true;
			default:
				return false;
		}
	}

	public static CRCParameters getParameters(ChecksumAlgorithm checksumAlgorithm, Error error)
	{
		if (error == null) return new CRCParameters(0, 0x00, 0x00, false, false, 0x00);
		switch (checksumAlgorithm) {
			case CRC8:
				return new CRCParameters(8, 0x07, 0x00, false, false, 0x00);
			case CRC8_CDMA2000:
				return new CRCParameters(8, 0x9B, 0xFF, false, false, 0x00);
			case CRC8_DARC:
				return new CRCParameters(8, 0x39, 0x00, true, true, 0x00);
			case CRC8_DVB_S2:
				return new CRCParameters(8, 0xD5, 0x00, false, false, 0x00);
			case CRC8_EBU:
				return new CRCParameters(8, 0x1D, 0xFF, true, true, 0x00);
			case CRC8_I_CODE:
				return new CRCParameters(8, 0x1D, 0xFD, false, false, 0x00);
			case CRC8_ITU:
				return new CRCParameters(8, 0x07, 0x00, false, false, 0x55);
			case CRC8_MAXIM:
				return new CRCParameters(8, 0x31, 0x00, true, true, 0x00);
			case CRC8_ROHC:
				return new CRCParameters(8, 0x07, 0xFF, true, true, 0x00);
			case CRC8_WCDMA:
				return new CRCParameters(8, 0x9B, 0x00, true, true, 0x00);
			case CRC16_AUG_CCITT:
				return new CRCParameters(16, 0x1021, 0x1D0F, false, false, 0x0000);
			case CRC16_CCITT_FALSE:
				return new CRCParameters(16, 0x1021, 0xFFFF, false, false, 0x0000);
			case CRC16_ARC:
				return new CRCParameters(16, 0x8005, 0x0000, true, true, 0x0000);
			case CRC16_BUYPASS:
				return new CRCParameters(16, 0x8005, 0x0000, false, false, 0x0000);
			case CRC16_CDMA2000:
				return new CRCParameters(16, 0xC867, 0xFFFF, false, false, 0x0000);
			case CRC16_DDS_110:
				return new CRCParameters(16, 0x8005, 0x800D, false, false, 0x0000);
			case CRC16_DECT_R:
				return new CRCParameters(16,0x0589, 0x0000, false, false, 0x0001);
			case CRC16_DECT_X:
				return new CRCParameters(16, 0x0589, 0x0000, false, false, 0x0000);
			case CRC16_DNP:
				return new CRCParameters(16, 0x3D65, 0x0000, true, true, 0xFFFF);
			case CRC16_EN_13757:
				return new CRCParameters(16, 0x3D65, 0x0000, false, false, 0xFFFF);
			case CRC16_GENIBUS:
				return new CRCParameters(16, 0x1021, 0xFFFF, false, false, 0xFFFF);
			case CRC16_MAXIM:
				return new CRCParameters(16, 0x8005, 0x0000, true, true, 0xFFFF);
			case CRC16_MCRF4XX:
				return new CRCParameters(16, 0x1021, 0xFFFF, true, true, 0x0000);
			case CRC16_RIELLO:
				return new CRCParameters(16, 0x1021, 0xB2AA, true, true, 0x0000);
			case CRC16_T10_DIF:
				return new CRCParameters(16, 0x8BB7, 0x0000, false, false, 0x0000);
			case CRC16_TELEDISK:
				return new CRCParameters(16, 0xA097, 0x0000, false, false, 0x0000);
			case CRC16_TMS_37157:
				return new CRCParameters(16, 0x1021, 0x89EC, true, true, 0x0000);
			case CRC16_USB:
				return new CRCParameters(16, 0x8005, 0xFFFF, true, true, 0xFFFF);
			case CRC_A:
				return new CRCParameters(16, 0x1021, 0xC6C6, true, true, 0x0000);
			case CRC16_KERMIT:
				return new CRCParameters(16, 0x1021, 0x0000, true, true, 0x0000);
			case CRC16_MODBUS:
				return new CRCParameters(16, 0x8005, 0xFFFF, true, true, 0x0000);
			case CRC16_X_25:
				return new CRCParameters(16, 0x1021, 0xFFFF, true, true, 0xFFFF);
			case CRC16_XMODEM:
				return new CRCParameters(16, 0x1021, 0x0000, false, false, 0x0000);
			case CRC32:
				return new CRCParameters(32, 0x04C11DB7, 0xFFFFFFFF, true, true, 0xFFFFFFFF);
			case CRC32_BZIP2:
				return new CRCParameters(32, 0x04C11DB7, 0xFFFFFFFF, false, false, 0xFFFFFFFF);
			case CRC32C:
				return new CRCParameters(32, 0x1EDC6F41, 0xFFFFFFFF, true, true, 0xFFFFFFFF);
			case CRC32D:
				return new CRCParameters(32, 0xA833982B, 0xFFFFFFFF, true, true, 0xFFFFFFFF);
			case CRC32_MPEG_2:
				return new CRCParameters(32, 0x04C11DB7	, 0xFFFFFFFF, false, false, 0x00000000);
			case CRC32_POSIX:
				return new CRCParameters(32, 0x04C11DB7, 0x00000000, false, false, 0xFFFFFFFF);
			case CRC32Q:
				return new CRCParameters(32, 0x814141AB, 0x000000000, false, false, 0x00000000);
			case CRC32_JAMCRC:
				return new CRCParameters(32,0x04C11DB7, 0xFFFFFFFF, true, true, 0x00000000);
			case CRC32_XFER:
				return new CRCParameters(32, 0x000000AF, 0x00000000, false, false, 0x0000000);
			default:
				error.setError("CHA03", "Unrecognized checksum algorithm");
				return null;
		}
	}


}
