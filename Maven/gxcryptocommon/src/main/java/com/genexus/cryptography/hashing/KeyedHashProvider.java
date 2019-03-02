package com.genexus.cryptography.hashing;

import com.genexus.cryptography.Utils;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class KeyedHashProvider implements IGXHashing {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private String _algorithm = HMAC_SHA256_ALGORITHM;
    //Supported Algorithms: HmacSHA1,HmacSHA256, HmacSHA384, HmacSHA512    
    
    private Mac _mac;
    public KeyedHashProvider(String algorithm) throws NoSuchAlgorithmException {
        _algorithm = algorithm;
        _mac = Mac.getInstance(_algorithm);
    }
    
    public String computeHash(String text) {
        throw new UnsupportedOperationException("Only key hashed algorithms are supported"); 
    }

    public String computeHashKey(String data, String key)
        throws SignatureException, InvalidKeyException
    {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), _algorithm);        
        _mac.init(signingKey);       
        return Utils.toHexString(_mac.doFinal(data.getBytes()));
    }
    
}
