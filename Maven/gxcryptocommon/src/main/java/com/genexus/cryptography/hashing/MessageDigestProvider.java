package com.genexus.cryptography.hashing;

import com.genexus.cryptography.Utils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestProvider implements IGXHashing {

	MessageDigest _alg;

	public MessageDigestProvider(String algorithm) throws NoSuchAlgorithmException {
		_alg = MessageDigest.getInstance(algorithm);
	}

	public String computeHash(String text) {
            try {
                _alg.update(text.getBytes("UTF8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
            return Utils.toHexString(_alg.digest());
	}
        
        public String computeHashKey(String text, String key){
            throw new UnsupportedOperationException("Not supported yet."); 
        }

}
