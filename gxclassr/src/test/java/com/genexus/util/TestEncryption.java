package com.genexus.util;

import com.genexus.GXutil;
import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Test;
import com.genexus.CommonUtil;
import static org.junit.Assert.assertEquals;

public class TestEncryption {

	@Test
	public void testUriEncrypt(){

		Connect.init();
		String GXKey="395CA6376B75526C8B6FF4010307A47D";
		String pgmName = "protocolo.wpconfirmardados";
		String encryptionTmp = pgmName + GXutil.URLEncode(CommonUtil.ltrimstr(1, 9, 0)) + "," + GXutil.URLEncode(CommonUtil.ltrimstr(0, 9, 0)) + "," + GXutil.URLEncode(CommonUtil.rtrim("jhon@4rtecnology.com")) + "," + GXutil.URLEncode(CommonUtil.rtrim("John Paul")) + "," + GXutil.URLEncode(CommonUtil.rtrim("424b25")) + "," + GXutil.URLEncode(CommonUtil.rtrim("Request"));
		String encrypted = Encryption.encrypt64(encryptionTmp + Encryption.checksum(encryptionTmp, 6), GXKey);
		System.out.println("encrypted:" + encrypted);
		Assert.assertTrue(encrypted.indexOf("//")>0);
		String uriEncrypted = Encryption.uriencrypt64(encryptionTmp + Encryption.checksum(encryptionTmp, 6), GXKey);
		System.out.println("uriEncrypted:" + uriEncrypted);
		Assert.assertTrue(uriEncrypted.indexOf("//")<0);
		String GXDecQS =Encryption.uridecrypt64( uriEncrypted, GXKey) ;
		boolean isMatch = ( GXutil.strcmp(GXutil.right( GXDecQS, 6), com.genexus.util.Encryption.checksum( GXutil.left( GXDecQS, GXutil.len( GXDecQS)-6), 6)) == 0 ) && ( GXutil.strcmp(GXutil.substring( GXDecQS, 1, GXutil.len( pgmName)), pgmName) == 0 );
		Assert.assertTrue(isMatch);
	}
}
