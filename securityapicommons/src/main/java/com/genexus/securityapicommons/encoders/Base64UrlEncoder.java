package com.genexus.securityapicommons.encoders;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.config.EncodingUtil;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.UrlBase64;

public class Base64UrlEncoder extends SecurityAPIObject {

	public Base64UrlEncoder() {
		super();
	}

	public String toBase64(String text) {
		this.error.cleanError();
		EncodingUtil eu = new EncodingUtil();
		byte[] textBytes = eu.getBytes(text);
		if(eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		String result = "";
		try {
			byte[] resultBytes =  UrlBase64.encode(textBytes);
			result = new String(resultBytes, "UTF-8").replaceAll("[\ufffd]", "");
		}catch(Exception e) {
			this.error.setError("BS001", e.getMessage());
			return "";
		}
		return result;
	}

	public String toPlainText(String base64Text) {
		this.error.cleanError();
		byte[] bytes;
		try {
			bytes = UrlBase64.decode(base64Text);
		}catch(Exception e)
		{
			this.error.setError("BS002", e.getMessage());
			return "";
		}
		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(bytes);
		if(eu.hasError())
		{
			this.error = eu.getError();
			return "";
		}
		return result;
	}

	public String toStringHexa(String base64Text) {
		this.error.cleanError();
		byte[] bytes;
		try {
			bytes = UrlBase64.decode(base64Text);
		}catch(Exception e)
		{
			this.error.setError("BS003", e.getMessage());
			return "";
		}
		String result = "";
		try {
			result = Hex.toHexString(bytes).toUpperCase();
		}catch(Exception e)
		{
			this.error.setError("BS004", e.getMessage());
			return "";
		}
		return result;
	}

	public String fromStringHexaToBase64(String stringHexa) {
		this.error.cleanError();
		byte[] stringBytes;
		try {
			stringBytes = Hex.decode(stringHexa);
		}catch(Exception e)
		{
			this.error.setError("BS005", e.getMessage());
			return "";
		}
		String result = "";
		try {
			byte[] resultBytes = UrlBase64.encode(stringBytes);
			result = new String(resultBytes, "UTF-8").replaceAll("[\ufffd]", "");
		}catch(Exception e)
		{
			this.error.setError("BS006", e.getMessage());
			return "";
		}
		return result;
	}

	public String base64ToBase64Url(String base64Text)
	{
		this.error.cleanError();
		String result= "";
		try {
			byte[] b64bytes = Base64.decode(base64Text);
			byte[] bytes = UrlBase64.encode(b64bytes);
			result = new String(bytes, "UTF-8").replaceAll("[\ufffd]", "");
		}catch(Exception e)
		{
			this.error.setError("BS007", e.getMessage());
			return "";
		}
		return result;
	}

	public String base64UrlToBase64(String base64UrlText)
	{
		this.error.cleanError();
		String result= "";
		try {
			byte[] b64bytes = UrlBase64.decode(base64UrlText);
			byte[] bytes = Base64.encode(b64bytes);
			result = new String(bytes, "UTF-8").replaceAll("[\ufffd]", "");
		}catch(Exception e)
		{
			this.error.setError("BS008", e.getMessage());
			return "";
		}
		return result;
	}
}
