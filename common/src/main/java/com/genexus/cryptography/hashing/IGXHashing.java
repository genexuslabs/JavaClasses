package com.genexus.cryptography.hashing;

import java.security.InvalidKeyException;
import java.security.SignatureException;


public interface IGXHashing {
	String computeHash(String text);
        String computeHashKey(String text, String key) throws SignatureException, InvalidKeyException;
}