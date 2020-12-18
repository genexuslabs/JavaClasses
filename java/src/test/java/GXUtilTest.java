import com.genexus.CommonUtil;
import com.genexus.DecimalUtil;
import com.genexus.GXutil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GXUtilTest {

	@Before // setup()
	public void before() throws Exception {
		com.genexus.specific.java.Connect.init();
	}

	@Test
	public void strTestTruncate() {
		String result = CommonUtil.ltrimstr( 9.99999999, 20, 2);
		assertEquals("9.99", result);
	}

	@Test
	public void strTestTruncate2() {
		String result = CommonUtil.ltrimstr( 9.99, 20, 2);
		assertEquals("9.99", result);
	}

	@Test
	public void strTestTruncate3() {
		String result = CommonUtil.ltrimstr( 9.99, 20, 0);
		assertEquals("9", result);
	}

	@Test
	public void strTestTruncate4() {
		String result = CommonUtil.ltrimstr( 9.913, 20, 2);
		assertEquals("9.91", result);
	}

	@Test
	public void strTestTruncate5() {
		String result = CommonUtil.ltrimstr( 9.909, 20, 2);
		assertEquals("9.90", result);
	}

	@Test
	public void strTestTruncate6() {
		String result = CommonUtil.ltrimstr( 0.909, 20, 2);
		assertEquals("0.90", result);
	}

	@Test
	public void strTestTruncate7() {
		String result = CommonUtil.ltrimstr( 4343.33, 20, 2);
		assertEquals("4343.33", result);
	}

	@Test
	public void strTestTruncate8() {
		String result = CommonUtil.ltrimstr( 0.1090999999, 20, 2);
		assertEquals("0.10", result);
	}

	@Test
	public void strTestTruncateBigDecimal(){
		java.math.BigDecimal AV5Val = DecimalUtil.stringToDec("999.999999999") ;
		String result = GXutil.ltrimstr( AV5Val, 20, 3);
		assertEquals("999.999", result);

	}
}
