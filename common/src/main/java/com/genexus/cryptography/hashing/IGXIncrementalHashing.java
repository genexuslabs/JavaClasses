package com.genexus.cryptography.hashing;

public interface IGXIncrementalHashing {

        void initData(String text);
        void appendRawData(String text);
        void appendData(String text);
	String getHashRaw();
	String getHash();        
}