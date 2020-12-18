import com.genexus.CommonUtil;
import com.genexus.DecimalUtil;
import com.genexus.GXutil;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

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
		compareBigDecimal(DecimalUtil.stringToDec("999.999999999"), "999.999", 20, 3);
		compareBigDecimal(DecimalUtil.doubleToDec(9999999999999991L), "9999999999999991", 16, 0);


		compareLong(9999999999999991L, "9999999999999991", 20, 0);
		compareLong(Long.MAX_VALUE, Long.toString(Long.MAX_VALUE, 10), 20, 0);
		compareDouble(9999999999991L, "9999999999991", 20, 0);
	}

	private void compareBigDecimal(BigDecimal value, String expectedValue, int digits, int decimals){
		String result = GXutil.ltrimstr( value, digits, decimals);
		assertEquals(expectedValue, result);
	}

	private void compareLong(long value, String expectedValue, int digits, int decimals){
		String result = CommonUtil.str(value, digits, decimals);
		assertEquals(expectedValue, result.trim());
	}

	private void compareDouble(double value, String expectedValue, int digits, int decimals){
		String result = CommonUtil.str(value, digits, decimals);
		assertEquals(expectedValue, result.trim());
	}

}
