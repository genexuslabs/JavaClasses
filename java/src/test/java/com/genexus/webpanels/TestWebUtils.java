package com.genexus.webpanels;

import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestWebUtils {

	@Before
	public void setUp() throws Exception {
		Connect.init();
	}

	@Test
	public void TestContentDispositionHeaderEncoding1() {
		String contentDisposition = "attachment; filename=file.pdf";
		String expectedContentDisposition = "attachment; filename*=UTF-8''file.pdf; filename=\"file.pdf\"";
		doTest(contentDisposition, expectedContentDisposition);
	}

	@Test
	public void TestContentDispositionHeaderEncoding2() {
		String contentDisposition = "attachment; filename=file.pdf";
		String expectedContentDisposition = contentDisposition;
		doTest(contentDisposition, expectedContentDisposition, HttpContextWeb.BROWSER_SAFARI);
	}

	@Test
	public void TestContentDispositionHeaderEncoding3() {
		String contentDisposition = "attachment; filename=注文詳細.xlsx";
		String expectedContentDisposition = "attachment; filename*=UTF-8''%E6%B3%A8%E6%96%87%E8%A9%B3%E7%B4%B0.xlsx; filename=\"%E6%B3%A8%E6%96%87%E8%A9%B3%E7%B4%B0.xlsx\"";
		doTest(contentDisposition, expectedContentDisposition);
	}

	@Test
	public void TestContentDispositionHeaderEncoding4() {
		String contentDisposition = "attachment; filename=注文詳細.xlsx";
		String expectedContentDisposition = contentDisposition;
		//Safari does not support rfc5987
		doTest(contentDisposition, expectedContentDisposition, HttpContextWeb.BROWSER_SAFARI);
	}

	@Test
	public void TestContentDispositionHeaderEncoding5() {
		String contentDisposition = "form-data; filename=file.pdf";
		String expectedContentDisposition = "form-data; filename*=UTF-8''file.pdf; filename=\"file.pdf\"";
		doTest(contentDisposition, expectedContentDisposition);
	}

	@Test
	public void TestContentDispositionHeaderEncoding6() {
		String contentDisposition = "ATTACHMENT; FILEname=注文詳細.xlsx";
		String expectedContentDisposition = "ATTACHMENT; filename*=UTF-8''%E6%B3%A8%E6%96%87%E8%A9%B3%E7%B4%B0.xlsx; filename=\"%E6%B3%A8%E6%96%87%E8%A9%B3%E7%B4%B0.xlsx\"";
		doTest(contentDisposition, expectedContentDisposition);
	}


	private void doTest(String contentDisposition, String expectedContentDisposition) {
		doTest(contentDisposition, expectedContentDisposition, HttpContextWeb.BROWSER_CHROME);
	}

	private void doTest(String contentDisposition, String expectedContentDisposition, int browserType) {
		String encodedValue = WebUtils.getEncodedContentDisposition(contentDisposition, browserType);
		Assert.assertEquals(expectedContentDisposition, encodedValue);
	}
}


