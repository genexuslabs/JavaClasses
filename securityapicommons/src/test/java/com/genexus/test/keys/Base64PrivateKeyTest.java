package com.genexus.test.keys;

import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class Base64PrivateKeyTest extends  SecurityAPITestObject{

	protected static String path;
	protected static String base64string;
	protected static String base64Wrong;


	@BeforeClass
	public static void setUp() {
		path = resources.concat("/sha256d_key.pem");
		base64string = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC50LMF7psbDQsgAZdrv/q0TGWBqTeDmPQ16aWobn3zf28kOb9Pdg2Ge1+G56ViO8aD/eDUaGi7CLtIc69fuZjFZ0W6snlNvKKa/6dwKtGF/Pbrb26bphwxe3eVT1UifsAH/Yv+gpOT3f0HQgX52yoyRBQDPxwSm4RtpNBbaJ5B0sf5sTdE0xi93QblXKTuKee31qiqioldzLSqO/TsW4YoC48uSN2MTYTmxNJ+wciiA7eTuKrydQ70TE74i3xuA/1N3+1b5rnAJ8ckCXOrrqHS+F7BuYj13Y4BV/cCyA3SjwL6bjO2xGw73ahh6ZphtSX2JYQYmYDSyp6NGMorGNQ5AgMBAAECggEAEBPwKZySbINzFFCO8D0q+DF+vFAOrvhWc9kaWGSXHrFGxfT+ihBGQ1NRhLQZKf16GS79Jo6SNqd0F7ogJpbsknLJJHUPl0Zchi+GJr0jEVuTZ0kRDQQRKcbr0KON/kTGtj+eFBDrAWm9McTot3cwmNYt1R9qJ8IFHLzyD7a8WtLjA/mNK6zYkimBpga1rWmkCyyNSP+KhEeUYGmig+LiubQmIwHbYrzDEeb1DGjKyE9upuwGs+nwdLStkEC7gOfsx7FM8lpnSeKstMjTmYz4j5TMyVdiIBY4yP9B6a6haFSNIpFm0YzNf0vesUHRh64HR/HCEkul34rOOaTqbjRlcQKBgQDrDT1AFzasJCJVUehtSmFNOUU2SIJLPC/Wh4VdrXtUNEcbxkjw3PFuzWkAixs99kUiefcGjKfVPUGOK8OV+6VUC5ckmYlHW1cx1iFwxcYfasmfb/tOQeWDmggPcITmNVlOolVwn+VYCUP4mt/B2wISNDu59QHV4u8+zZ3IToPBswKBgQDKYB4zA78GIw4/8/ywFH7NMz11BYnrfqLq5ZZBZvYML0nnzApZqtate+xUid5v5T++Koy4NCx+dMda6WOhRX3io9K3nrM1KiiWpynI4qHO06+LGH33rBFnE1dbhTx0SVU9UpCagauoNREUXnXi7py7fGdhlOsG8rcttRsd5BlkYwKBgQC7t2gKLj/QfE8bGn3oAnXwyWMX9hJwaVG/H54H8UtENTfw24tXKOx70/oen/mSo4IVBZidl2lV6ETZeOQLfNxNYbBEX4X+AdmCCIPOX3RZlNwOw8zMc94LGtGDGxZYD5USMpzPhDMR+txYx78ZP4HI7gQg/6WGnmT5IBb5aJLa9wKBgQCTxJ0oaMrZg01LWy8drslrscdlI/cx0dTJqXwOI0zzVrAjJbRFBt4b7ImCrOyTTZQ+mbkIY2g9qa1K73GE90XU8APTeXinEDJ01nhHK1w0thLOgMKxzp0iY1f9Bos+6bDoxtm5R4d8mcrv0Y1IdyxQJaUi9maqOx2PrVawe7YiuwKBgQC2G1+b8j8ddu/jVplBjIVK1Bnih0PUhRKVs5SbwPQwIq+MbGhKH4cuFQUgcyaLW76twchcL8eoebwwkLYhjQkbwSHane3/4TKJmD+ZfLRrRjaPKeFVngWTaK5OiebhvhZ6UFy+6JUE6u99H+sitG3ytL6/0MFplgV+7OnOcaaqLQ==";
		base64Wrong = "--BEGINKEY--sdssf--ENDKEY—";

	}

	@Test
	public void testImport() {
		PrivateKeyManager pkm = new PrivateKeyManager();
		boolean loaded = pkm.fromBase64(base64string);
		True(loaded, pkm);
	}

	@Test
	public void testExport() {
		PrivateKeyManager pkm = new PrivateKeyManager();
		pkm.load(path);
		String base64res = pkm.toBase64();
		assertTrue(SecurityUtils.compareStrings(base64res, base64string));
		assertFalse(pkm.hasError());
	}

	@Test
	public void testWrongBase64()
	{
		PrivateKeyManager pkm = new PrivateKeyManager();
		pkm.fromBase64(base64Wrong);
		assertTrue(pkm.hasError());
	}
}
