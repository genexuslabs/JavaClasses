package com.genexus.cryptography.test.hash;

import com.genexus.cryptography.hash.Hashing;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class HashingTest extends SecurityAPITestObject {

	protected static String plainText;
	protected String[] arrayHashes;
	protected String[] arrayResHashes;

	@Override
	protected void setUp() {
		new EncodingUtil().setEncoding("UTF8");
		String MD5 = "FEA80F2DB003D4EBC4536023814AA885";
		String SHA1 = "38F00F8738E241DAEA6F37F6F55AE8414D7B0219";
		String SHA224 = "B3F933DCF0CD5C02169F6FFB9E9DD156E6A378F64DD992D5768BA020";
		String SHA256 = "16ABA5393AD72C0041F5600AD3C2C52EC437A2F0C7FC08FADFC3C0FE9641D7A3";
		String SHA384 = "95A0541E1E04F0C65701902FD92329070BDC7001477B0B9B280C79BC1ADDC9D8C305E97A8BDECDC3E6024069AE97A2CD";
		String SHA512 = "B1F4AAA6B51C19FFBE4B1B6FA107BE09C8ACAFD7C768106A3FAF475B1E27A940D3C075FDA671EADF46C68F93D7EABCF604BCBF7055DA0DC4EAE6743607A2FC3F";
		String BLAKE2B_224 = "CFD4B035CD7F135A65E196FD5220FD232D499017F153D7A12AAC48EE";
		String BLAKE2B_256 = "43D3205918BBB6A0175A949BD5AA77F6AC87184F8459ED9EA66387773B4121A5";
		String BLAKE2B_384 = "44BC61A7DD4447862D2DB262338A6ABF2B3FFCC3E05CBE4C0ED279157FCCDB36BE66B67F7733ABA902CE6AF65B733B31";
		String BLAKE2B_512 = "965F6293650C11D0AFB91385AFA89F937A468E1F693C5402D369431347E4DC22800A9DCF425B27BFA884E05C80F9D2F23797CA6DB3CD357383D3C6DCA8BA68CA";
		String BLAKE2S_128 = "20CBE99525EC7A3B20BC2A5F9A6B0A01";
		String BLAKE2S_160 = "08E46034CD071C09DAAFF949AA47652E32BC4029";
		String BLAKE2S_224 = "7DF368CC11D65AF123CBDEE9ABDDEC3B09AB879AAD1BD3EBE6E512F0";
		String BLAKE2S_256 = "52FEE5030C5B61C42DB2DC09435BC6E6F357AAFA450C23F0C4DB8F2D44C7C3A6";
		String GOST3411_2012_256 = "2FB0AA06A692B00A1905BE6AF797F24C976B6746DE31D5A67A096E685EFB4489";
		String GOST3411_2012_512 = "C5DC8AF567D3183040C6CDC1F19F38FC6345B0513D02EBF9D2054690D85DD38271E38F3C0069A9C0891764E9F94E627D1245C6D718C4194AA3E594E209639453";
		String GOST3411 = "47B222D3029D84F229DB1798942A1192F5BA27D3647F12DD0F735DD0C420C160";
		String KECCAK_224 = "8AF0DB4E1481E9E8704312634537664FA634932E92236DED8B2AA348";
		String KECCAK_256 = "B53B7CA515051D49E851C07BD0FBDDDB8010A9366F5B1F5FB737BA21A4356301";
		String KECCAK_288 = "5F73ABD3095975571EBC94BAC6D9840CC888D81A9FD2BF524D1F9F37E422E736C7EE47CC";
		String KECCAK_384 = "C31C889B72BAEBEC30903190864B0035A5058C3A79A45A5E488DC4FFA7E81D881F6B85DD3493C222925B79E90B8AEDED";
		String KECCAK_512 = "21FDB61F9F82B8D20930B43D51DC7FE0E931D2E18E957F3B5316D147FE3E2E5EC567457D0894A6BF867595A1A1AA1303FB19D57D2DA5921119C8EC801FC1F68D";
		String MD2 = "854BF540A75F548906201F479526C1BA";
		String MD4 = "69740FE64C87D1DF772D8B8BAC7FCC26";
		String RIPEMD128 = "52EE689604C15591ED9EF6A0BDF6FD28";
		String RIPEMD160 = "7D0982BE59EBE828D02AA0D031AA6651644D60DA";
		String RIPEMD256 = "49A275CDAC39B746C0E0DCFBCE97144940863F83DF4EA8A0833874496BC84102";
		String RIPEMD320 = "969607288B7E788A239B91803AC0187530F16242AA314CFD06CBE780E2377CFA200A2288DB019BFD";
		String SHA3_224 = "F75F9AFF421AB1A2BF95B997144F184ACF97F2BCD4FFB854B8C7377D";
		String SHA3_256 = "AFBE560C8EA52AF055471E6AEDB33B104F2A2373CE642B251A9F5F25E669BF66";
		String SHA3_384 = "4E1389E97F01ACB13A88AF18FCA572CEFE56926612315F797C6D2CB0F5EEA3BF4FD2623D3E3A388B14225F2DEE806F2A";
		String SHA3_512 = "8571AE21DB0FE5F7EEE18B029557038B61C024773A625082FA528DBCD19737A828663CB4D51726865B959AB6961D609DD1FEEBC251A575BED0DD8661BE2B83C1";
		//String SHAKE_128 = "22E24D7A4D4CD0FFC803529449287D60";
		//String SHAKE_256 = "F4112786511DF1AD767E69121859269D4BE0C910A0E88848D8DD887D49741B5E";
		String SM3 = "2F097B534D5FC00AF295A8ECA9CC7B75DFCD1C1F9E272B406626DC98908244D7";
		String TIGER = "3217841153FBA3E5CFFC5F57E815FCE833A813ED10F78F3C";
		String WHIRLPOOL = "EA126D6304A311602F7F194E492559FBEA4D4DC7F3E6415DE369D184E19F6597171DD1303527D8EE440A049F5239385D23E58750913FC4F94A4C5E4CDE9031D1";

		plainText = "Lorem ipsum dolor sit amet";

		arrayHashes = new String[] { "MD5", "SHA1", "SHA224", "SHA256", "SHA384", "SHA512", "BLAKE2B_224",
			"BLAKE2B_256", "BLAKE2B_384", "BLAKE2B_512", "BLAKE2S_128", "BLAKE2S_160", "BLAKE2S_224", "BLAKE2S_256",
			"GOST3411_2012_256", "GOST3411_2012_512", "GOST3411", "KECCAK_224", "KECCAK_256", "KECCAK_288",
			"KECCAK_384", "KECCAK_512", "MD2", "MD4", "RIPEMD128", "RIPEMD160", "RIPEMD256", "RIPEMD320",
			"SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512", /*"SHAKE_128", "SHAKE_256",*/ "SM3", "TIGER", "WHIRLPOOL" };
		arrayResHashes = new String[] { MD5, SHA1, SHA224, SHA256, SHA384, SHA512, BLAKE2B_224, BLAKE2B_256,
			BLAKE2B_384, BLAKE2B_512, BLAKE2S_128, BLAKE2S_160, BLAKE2S_224, BLAKE2S_256, GOST3411_2012_256,
			GOST3411_2012_512, GOST3411, KECCAK_224, KECCAK_256, KECCAK_288, KECCAK_384, KECCAK_512, MD2, MD4,
			RIPEMD128, RIPEMD160, RIPEMD256, RIPEMD320, SHA3_224, SHA3_256, SHA3_384, SHA3_512, /*SHAKE_128,
				SHAKE_256,*/ SM3, TIGER, WHIRLPOOL };
	}

	public void testBulkHashes() {
		Hashing hash = new Hashing();
		for (int a = 0; a < arrayHashes.length; a++) {
			String result = hash.doHash(arrayHashes[a], plainText);
			assertTrue(SecurityUtils.compareStrings(arrayResHashes[a], result));
			True(SecurityUtils.compareStrings(arrayResHashes[a], result), hash);
		}
	}

	public static Test suite() {
		return new TestSuite(HashingTest.class);
	}

	@Override
	public void runTest() {
		testBulkHashes();
	}

}
