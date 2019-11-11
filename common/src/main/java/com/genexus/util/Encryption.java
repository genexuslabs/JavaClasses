package com.genexus.util;

import java.security.InvalidKeyException;
import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

public class Encryption
{
    public static String AJAX_ENCRYPTION_KEY = "GX_AJAX_KEY";
	public static String AJAX_SECURITY_TOKEN = "AJAX_SECURITY_TOKEN";
	public static String GX_AJAX_PRIVATE_KEY = "595D54FF4A612E69FF4F3FFFFF0B01FF";

	static public class InvalidGXKeyException extends RuntimeException
	{
		public InvalidGXKeyException() 
		{
			super("Invalid key");
		}

		public InvalidGXKeyException( String c)
		{
			super("Invalid key " + c);
		}

		public InvalidGXKeyException( char c)
		{
			super("Invalid key " + c);
		}
	}

	public static String uriencrypt64(String value, String key)
	{
		return encrypt64(value, key).replace('/', '_');
	}
	public static String uridecrypt64(String value, String key)
	{
		return decrypt64(value.replace('_', '/'), key);
	}
	public static String encrypt64(String value, String key)
	{
		int indexOf = key.lastIndexOf('.');
		if	(indexOf > 0)
			key=  key.substring(0, indexOf);		
		
		if	(key.length() != 32)
			throw new InvalidGXKeyException();
		try
		{
	    	return new String(Codecs.base64Encode(encrypt(value.getBytes("UTF8"), SpecificImplementation.Algorithms.twoFish_makeKey(convertKey(key)))));
		}
		catch(UnsupportedEncodingException e)
		{
			System.err.println(e);
			throw new RuntimeException(e.getMessage());
		}
		catch (InvalidKeyException e)
 		{
			System.err.println(e);
			throw new InvalidGXKeyException(e.getMessage());
		}
	}

	private static byte[] convertKey(String a)
	{
		byte[] out = new byte[a.length() / 2];

		int i = 0;
		int j = 0;
		for (; i < a.length(); i+=2, j++)
		{
			out[j] = (byte) (toHexa(a.charAt(i)) * 16 + toHexa(a.charAt(i+ 1))) ;
		}
		return out;
	}

	private static byte toHexa(char c)
	{
		byte b;

		if ((c >= '0') && (c <= '9'))
			b = (byte) (c-'0');
		else if ((c >= 'a') && (c <= 'f'))
			b = (byte) (c-'a'+10);
		else if ((c >= 'A') && (c <= 'F'))
			b = (byte) (c-'A'+10);
		else
			throw new InvalidGXKeyException(c);
		
		return b;
	}

	public static String encrypt16(String value, String key)
	{
		return "";
	}

	public static String decrypt16(String value, String key)
	{
		return "";
	}
        
        public static String decrypt64(String value){
            value= decrypt64(value,  SpecificImplementation.Application.getModelContext().getServerKey());
            return value.substring(0, value.length()-CHECKSUM_LENGTH);
        }

	public static String decrypt64(String value, String key)
	{
		int indexOf = key.lastIndexOf('.');
		if	(indexOf > 0)
			key=  key.substring(0, indexOf);		
		
		if	(key.length() != 32)
			throw new InvalidGXKeyException();

		value = CommonUtil.rtrim(value);

		try
		{
	    	return CommonUtil.rtrim(new String(decrypt(Codecs.base64Decode(value.getBytes()), SpecificImplementation.Algorithms.twoFish_makeKey(convertKey(key))), "UTF8"));
		}
		catch (InvalidKeyException e)
 		{
			System.err.println(e);
			throw new InvalidGXKeyException(e.getMessage());
		}
		catch(UnsupportedEncodingException e)
		{
			System.err.println(e);
			throw new RuntimeException(e.getMessage());
		}
		catch (ArrayIndexOutOfBoundsException e)
 		{
			return "";
		}
	}
	
	private static final int CHECKSUM_LENGTH = 6;

	public static int getCheckSumLength()
	{
		return CHECKSUM_LENGTH;	
	}	

	public static String calcChecksum(String value, int start, int end, int length)
	{
		int ret = 0;

		for (int i = start; i < end; i++)
		{
			ret += value.charAt(i);
		}

		return CommonUtil.padl(CommonUtil.upper(Integer.toHexString(ret)), length, "0");
	}


	public static String checksum(String value, int length)
	{
		return calcChecksum(value, 0, value.length(), length);
	}

	public static String addchecksum(String value, int length)
	{
		return value + calcChecksum(value, 0, value.length(), length);
	}

   	private static final char[] HEX_DIGITS = 
   	{
      '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
   	};

	static SecureRandom random = new SecureRandom();

	public static String getNewKey()
	{

      	char[] buf = new char[32];
		byte[] ba  = new byte[16];
		
		random.nextBytes(ba);

      	for (int i = 0, j = 0, k; i < 16; ) 
      	{
        	k = ba[i++];
         	buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
         	buf[j++] = HEX_DIGITS[ k      & 0x0F];
      	}
  
      	return new String(buf);
/*
		byte[] out = new byte[32];
		random.nextBytes2(out);

		return new String(out);
*/
		//return Twofish_Algorithm.makeKey(out);
	}

	public static byte[] encrypt(byte[] input, Object key)
	{
		int rest = 0;
		
		if	(input.length % 16 != 0)
		 	rest = 16 - (input.length % 16);

		byte[] input_copy 	= new byte[input.length + rest];
		byte[] output 		= new byte[input_copy.length];

		System.arraycopy(input, 0, input_copy, 0, input.length);

		for (int i = 0; i < rest; i++)
		{
			input_copy[input.length + i] = 32;
		}

		int count = input_copy.length / 16;

		for (int idx = 0; idx < count; idx++)
		{
			System.arraycopy(SpecificImplementation.Algorithms.twoFish_BlockEncrypt(input_copy, idx * 16, key), 0, output, idx * 16, 16);
		}

		return output;
	}

   private static String toString (byte[] ba) {
      return toString(ba, 0, ba.length);
   }
   private static String toString (byte[] ba, int offset, int length) {
      char[] buf = new char[length * 2];
      for (int i = offset, j = 0, k; i < offset+length; ) {
         k = ba[i++];
         buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
         buf[j++] = HEX_DIGITS[ k      & 0x0F];
      }
      return new String(buf);
   }

	private static void printBytes(byte[] a, int from, int to)
	{
		for (int i = from; i < to; i++)
		{
			System.out.print( a[i] + " ");
		}
		System.out.println("");
	}

	public static byte[] decrypt(byte[] input, Object key)
	{
		byte[] output 		= new byte[input.length];

		int count = input.length / 16;
		for (int idx = 0; idx < count; idx++)
		{
			System.arraycopy(SpecificImplementation.Algorithms.twoFish_BlockDecrypt(input, idx * 16, key), 0, output, idx * 16, 16);
		}

		return output;
	}

	static class RandomGenerator extends java.security.SecureRandom
	{
		public void nextBytes2(byte[] out)
		{
			for (int i = 0; i < out.length; i++)
			{
				out[i] = (byte) next(4);
			}
		}
	}
        
        public static String getRijndaelKey()
        {
			SecureRandom rdm = new SecureRandom();
            byte[] bytes = new byte[16];
            rdm.nextBytes(bytes);
            StringBuffer buffer = new StringBuffer(32);
            for (int i = 0; i < 16; i++)
            {
                buffer.append(CommonUtil.padl(Integer.toHexString((int)bytes[i]), 2, "0"));
            }
            return buffer.toString().toUpperCase();
        }
        public static String decryptRijndael(String encrypted, String key, boolean[] candecrypt)
        {
        	try{
        		candecrypt[0]=false;
	            byte[] encryBytes = HexUtil.hexToBytes(encrypted);
	            String decrypted = "";
	            if (encryBytes.length>0){
		            byte[] keyBytes = HexUtil.hexToBytes(key);
		            Object objKey =  SpecificImplementation.Algorithms.rijndael_makeKey(keyBytes);
		            int blocks = encryBytes.length / SpecificImplementation.Algorithms.getRijndael_AlgorithmBLOCK_SIZE();
		            if((encryBytes.length % SpecificImplementation.Algorithms.getRijndael_AlgorithmBLOCK_SIZE()) > 0)
		            {
		             blocks++;
		            }
		            for(int i=0; i<blocks; i++)
		            {
		                int blockStart = SpecificImplementation.Algorithms.getRijndael_AlgorithmBLOCK_SIZE()*i;
		                byte[] decryBytes = new byte[SpecificImplementation.Algorithms.getRijndael_AlgorithmBLOCK_SIZE()];
		                SpecificImplementation.Algorithms.rijndael_BlockDecrypt(encryBytes, decryBytes, blockStart, objKey);
		                decrypted += new String(decryBytes);
		            }
		            int endIdx = decrypted.indexOf('\u0000');
		            if(endIdx != -1)
		            {
		                decrypted = decrypted.substring(0, endIdx);
		            }
		            candecrypt[0]=true;
	            }
	            return decrypted;
        	}catch(Exception ex)
        	{
        		return encrypted;
        	}
        }
        
        public static String encryptRijndael(String decrypted, String key) throws Exception
        {
            byte[] textBytes = decrypted.getBytes();
            byte[] keyBytes = HexUtil.hexToBytes(key);
            Object objKey = SpecificImplementation.Algorithms.rijndael_makeKey(keyBytes);
            String encrypted = "";
            int blockSize = SpecificImplementation.Algorithms.getRijndael_AlgorithmBLOCK_SIZE();
            int blocks = textBytes.length/blockSize;
            if((textBytes.length%blockSize) > 0)
            {
                blocks++;
            }
            byte[] decryBytes = new byte[blocks*blockSize];
            for (int i=0; i<blocks*blockSize; i++)
            {
                if (i < textBytes.length)
                    decryBytes[i] = textBytes[i];
                else
                    decryBytes[i] = 0;
            }
            for(int i=0; i<blocks; i++)
            {
                int blockStart = blockSize*i;
                byte[] encryBytes = new byte[blockSize];
                SpecificImplementation.Algorithms.rijndael_BlockEncrypt(decryBytes, encryBytes, blockStart, objKey);
                encrypted += HexUtil.bytesToHex(encryBytes);
            }
            return encrypted.trim();
        }
}