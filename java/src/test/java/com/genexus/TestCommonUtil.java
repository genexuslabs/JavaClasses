package com.genexus;

import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Assert;
import org.junit.Test;

public class TestCommonUtil {

	private void initialize()
	{
		Connect.init();
		LogManager.initialize(".");
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
	}

}
