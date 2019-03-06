package com.genexus.cryptography.hashing;

import com.genexus.cryptography.Utils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IncrementalDigestProvider implements IGXIncrementalHashing {

	MessageDigest _alg;

    final String OPEN_BRACE = "[";
	final String CLOSE_BRACE = "]";
	final String SEPARATOR = ",";

	public  IncrementalDigestProvider(String algorithm) {
		try 
        {
        _alg = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException ex)
		{
			System.err.println("error - " + algorithm + " - " +  ex.toString());
		}
	}

    public void appendRawData(String text) {
        try {
             byte[] bytedata = text.getBytes("UTF8");
            _alg.update( bytedata, 0, bytedata.length);
        } 
        catch (UnsupportedEncodingException e) {
              e.printStackTrace();
        }
	}

	public void initData(String text) {
        text = OPEN_BRACE + text;
        try {
            byte[] bytedata = text.getBytes("UTF8");
            _alg.update( bytedata, 0, bytedata.length);
        } 
        catch (UnsupportedEncodingException e) {
              e.printStackTrace();
        }	
	}

	public void appendData(String text)
	{        
        text = SEPARATOR + text;
        try {
            byte[] bytedata = text.getBytes("UTF8");
            _alg.update( bytedata, 0, bytedata.length);
        } 
        catch (UnsupportedEncodingException e) {
              e.printStackTrace();
        }	
	}

	public String getHashRaw() 
    {
		return Utils.toHexString(_alg.digest());
	}

	public String getHash() 
    {
        String text = CLOSE_BRACE;
        try {
            byte[] bytedata = text.getBytes("UTF8");
            return Utils.toHexString(_alg.digest(bytedata));
        } 
        catch (UnsupportedEncodingException e) {
              e.printStackTrace();
              return null;
        } 
	}
}
