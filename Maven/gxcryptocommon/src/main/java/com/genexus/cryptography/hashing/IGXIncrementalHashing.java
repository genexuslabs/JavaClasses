package com.genexus.cryptography.hashing;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;


public interface IGXIncrementalHashing {

        void initData(String text);
        void appendRawData(String text);
        void appendData(String text);
	String getHashRaw();
	String getHash();        
}