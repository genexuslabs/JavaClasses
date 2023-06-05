package com.genexus;

import com.genexus.db.UserInformation;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Assert;
import org.junit.Test;
import java.util.Date;
import java.util.UUID;


public class TestCommonUtil {

	private void initialize()
	{
		Connect.init();
		LogManager.initialize(".");
		Application.init(GXcfg.class);
	}

	@Test
	public void testSanitize() {
		initialize();

		//Test case 1: Sanitize using LogUserEntryWhiteList
		String value = "This is a string without Sanitize %@, let's see what happens ";
		String expectedResult = "This is a string without Sanitize , lets see what happens ";
		String result = CommonUtil.Sanitize(value, CommonUtil.LOG_USER_ENTRY_WHITELIST);
		Assert.assertEquals(expectedResult, result);

		//Test case 2: Sanitize using HttpHeaderWhiteList
		value = "This is a string without Sanitize %@, let's see what happens ";
		expectedResult = "ThisisastringwithoutSanitize@,letsseewhathappens";
		result = CommonUtil.Sanitize(value, CommonUtil.HTTP_HEADER_WHITELIST);
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testFormat() {
		initialize();

		// Test case 1: Pass in a string with no parameter markers
		String value = "Hello world";
		String expectedResult = "Hello world";
		String result = CommonUtil.format(value, "", "", "", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 2: Pass in a string with maximum parameter markers
		value = "This is a string with %1, %2, %3, %4, %5, %6, %7, %8, and %9";
		expectedResult = "This is a string with 1, 2, 3, 4, 5, 6, 7, 8, and 9";
		result = CommonUtil.format(value, "1", "2", "3", "4", "5", "6", "7", "8", "9");
		Assert.assertEquals(expectedResult, result);

		// Test case 3: Pass in a string with one parameter marker that's also the end of the string
		value = "Hello %1";
		expectedResult = "Hello world";
		result = CommonUtil.format(value, "world", "", "", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 4: Pass in a string with one parameter marker that's not the end of the string
		value = "Alex is %1 years old";
		expectedResult = "Alex is 26 years old";
		result = CommonUtil.format(value, "26", "", "", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 5: Pass in a string with multiple parameter markers
		value = "%1 is %2 years old";
		expectedResult = "Alex is 26 years old";
		result = CommonUtil.format(value, "Alex", "26", "", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 6: Pass in an empty string
		value = "";
		expectedResult = "";
		result = CommonUtil.format(value, "1", "2", "3", "4", "5", "6", "7", "8", "9");
		Assert.assertEquals(expectedResult, result);

		// Test case 7: Pass in a string containing the "/%" sequence
		value = "%1/%2/%3";
		expectedResult = "2022/12/31";
		result = CommonUtil.format(value, "2022","12","31", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 8: Pass in a string containing one parameter marker followed by a % sign at the end of the string
		value = "%1%";
		expectedResult = "10%";
		result = CommonUtil.format(value, "10","","", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 9: Pass in a string containing one parameter marker followed by a % sign that's not at the end of the string
		value = "The price is %1% off today";
		expectedResult = "The price is 10% off today";
		result = CommonUtil.format(value, "10","","", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 9: Pass in a string containing multiple parameter markers separated by a % sign that also are at the end of the string
		value = "Your discount code is %1%%2";
		expectedResult = "Your discount code is 10%10";
		result = CommonUtil.format(value, "10","10","", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 10: Pass in a string containing multiple parameter markers separated by a % sign that are not at the end of the string
		value = "sample text %1%%2%%3%%4%%5 sample text";
		expectedResult = "sample text 10%10%10%10%10 sample text";
		result = CommonUtil.format(value, "10","10","10", "10", "10", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 11: Pass in a string with a parameter marker that should be ignored
		value = "sample text \\%1 sample text";
		expectedResult = "sample text %1 sample text";
		result = CommonUtil.format(value, "10","","", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 12: Pass in a string with appended parameter markers at the end of the string
		value = "Your discount code is %1%2";
		expectedResult = "Your discount code is 1020";
		result = CommonUtil.format(value, "10","20","", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 13: Pass in a string with appended parameter markers at the middle of the string
		value = "Your discount code is %1%2, use it within 24 hours";
		expectedResult = "Your discount code is 1020, use it within 24 hours";
		result = CommonUtil.format(value, "10","20","", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 14: Pass in a string with appended parameter markers at the start of the string
		value = "%1%2 is your discount code";
		expectedResult = "1020 is your discount code";
		result = CommonUtil.format(value, "10","20","", "", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		// Test case 15: Pass in more parameter markers than needed
		value = "%1 is %2 years old";
		expectedResult = "Alex is 26 years old";
		result = CommonUtil.format(value, "Alex", "26", "10", "10", "", "", "", "", "");
		Assert.assertEquals(expectedResult, result);

		UserInformation ui = (UserInformation) GXObjectHelper.getUserInformation(ModelContext.getModelContext(GXcfg.class), -1);
		long decimalValue = -150;
		String picture = "$ZZZ,ZZZ,ZZ9";
		expectedResult = "-$        150";

		result = ui.getLocalUtil().format(decimalValue, picture);
		Assert.assertEquals(expectedResult, result);

		result = ui.getLocalUtil().format(DecimalUtil.doubleToDec(decimalValue), picture);
		Assert.assertEquals(expectedResult, result);

		picture = "$ ZZZ,ZZZ,ZZ9";
		expectedResult = "-$         150";

		result = ui.getLocalUtil().format(decimalValue, picture);
		Assert.assertEquals(expectedResult, result);

		result = ui.getLocalUtil().format(DecimalUtil.doubleToDec(decimalValue), picture);
		Assert.assertEquals(expectedResult, result);

		picture = "ZZZ,ZZZ,ZZ9";
		expectedResult = "        -150";

		result = ui.getLocalUtil().format(decimalValue, picture);
		Assert.assertEquals(expectedResult, result);

		result = ui.getLocalUtil().format(DecimalUtil.doubleToDec(decimalValue), picture);
		Assert.assertEquals(expectedResult, result);

		decimalValue = -123456789;
		picture = "ZZZ,ZZZ,ZZ9";
		expectedResult = "-123,456,789";

		result = ui.getLocalUtil().format(decimalValue, picture);
		Assert.assertEquals(expectedResult, result);

		result = ui.getLocalUtil().format(DecimalUtil.doubleToDec(decimalValue), picture);
		Assert.assertEquals(expectedResult, result);

	}

	@Test
	public void testConvertObjectTo() {
		initialize();

		Object obj;
		Object result;

		// Test case 1: Pass in a non-null integer
		obj = 1;
		try{
			Class integerClass = Class.forName("java.lang.Integer");
			result = CommonUtil.convertObjectTo(obj, integerClass, true);
			Assert.assertEquals(Integer.valueOf("1"), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 2: Pass in a "null" integer
		obj = "null";
		try{
			Class integerClass = Class.forName("java.lang.Integer");
			result = CommonUtil.convertObjectTo(obj, integerClass, true);
			Assert.assertEquals(Integer.valueOf("0"), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 3: Pass in a non-null string
		obj = "Hello world";
		try {
			Class stringClass = Class.forName("java.lang.String");
			result = CommonUtil.convertObjectTo(obj, stringClass, true);
			Assert.assertEquals("Hello world", result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 4: Pass in a "null" string
		obj = "null";
		try{
			Class stringClass = Class.forName("java.lang.String");
			result = CommonUtil.convertObjectTo(obj, stringClass, true);
			Assert.assertEquals("", result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 5: Pass in a non-null decimal
		obj = 1.5;
		try{
			Class decimalClass = Class.forName("java.lang.Float");
			result = CommonUtil.convertObjectTo(obj, decimalClass, true);
			Assert.assertEquals(Float.valueOf("1.5"), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 6: Pass in a "null" decimal
		obj = "null";
		try{
			Class decimalClass = Class.forName("java.lang.Float");
			result = CommonUtil.convertObjectTo(obj, decimalClass, true);
			Assert.assertEquals(Float.valueOf("0"), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 7: Pass in a non-null boolean
		obj = true;
		try{
			Class booleanClass = Class.forName("java.lang.Boolean");
			result = CommonUtil.convertObjectTo(obj, booleanClass, true);
			Assert.assertEquals(true, result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 7: Pass in a "null" boolean
		obj = "null";
		try{
			Class booleanClass = Class.forName("java.lang.Boolean");
			result = CommonUtil.convertObjectTo(obj, booleanClass, true);
			Assert.assertEquals(false, result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 8: Pass in a non-null date
		obj = new Date(0);
		try{
			Class dateClass = Class.forName("java.util.Date");
			result = CommonUtil.convertObjectTo(obj, dateClass, true);
			Assert.assertEquals(LocalUtil.getISO8601Date("0001/01/01"), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 9: Pass in a "null" date
		obj = "null";
		try{
			Class dateClass = Class.forName("java.util.Date");
			result = CommonUtil.convertObjectTo(obj, dateClass, true);
			Assert.assertEquals(CommonUtil.nullDate(), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 9: Pass in a non-null uuid
		obj = UUID.randomUUID();
		try{
			Class uudiClass = Class.forName("java.util.UUID");
			result = CommonUtil.convertObjectTo(obj, uudiClass, true);
			Assert.assertEquals(UUID.fromString(obj.toString()), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}

		// Test case 10: Pass in a "null" uuid
		obj = "null";
		try{
			Class uudiClass = Class.forName("java.util.UUID");
			result = CommonUtil.convertObjectTo(obj, uudiClass, true);
			Assert.assertEquals(new UUID(0,0), result);
		} catch (Exception e){
			Assert.fail("Test failed " + e);
		}
	}
}
