package com.genexus.cryptography;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.apache.commons.lang.StringUtils;

import com.genexus.cryptography.hashing.IGXHashing;
import com.genexus.cryptography.hashing.KeyedHashProvider;
import com.genexus.cryptography.hashing.MessageDigestProvider;

public class GXHashing {

    private int _lastError;
    private String _lastErrorDescription;
    private String _algorithm;
    private IGXHashing _hash;
    private boolean isDirty;

    public GXHashing() {
        isDirty = true;
        _algorithm = Constants.DEFAULT_HASH_ALGORITHM;
        initialize();
    }

    private void initialize() {
        if (isDirty) {
            setError(0);
            try {
                if (_algorithm.toUpperCase().startsWith("HMAC"))
                {
                    _hash = new KeyedHashProvider(Utils.mapHashAlgorithm(_algorithm));
                }else{
                    _hash = new MessageDigestProvider(Utils.mapHashAlgorithm(_algorithm));
                }
                isDirty = false;
            } catch (NoSuchAlgorithmException e) {
                setError(2);
            }
        }
    }

    public String compute(String text, String key) {
        initialize();
        if (!anyError()) {
            boolean keyHashAlgorithm = _hash instanceof KeyedHashProvider;
            if (keyHashAlgorithm && StringUtils.isBlank(key))
            {
                setError(4);
            }
            else
            {
                if (!keyHashAlgorithm && !StringUtils.isBlank(key))
                {
                    setError(3);
                }
                else {
                    if (keyHashAlgorithm){
                        try {
                            return _hash.computeHashKey(text, key);
                        } catch (SignatureException ex) {
                            setError(1);
                        }
                        catch (InvalidKeyException ex) {
                            setError(5);
                        }
                    } 
                    else
                        return _hash.computeHash(text);
                }
            }
        }
        return "";
    }

    private void setError(int errorCode) {
        _lastError = errorCode;
        switch (errorCode) {
        case 0:
            _lastErrorDescription = Constants.OK;
            break;
        case 1:
            _lastErrorDescription = "Signature exception";
            break;
        case 2:
            _lastErrorDescription = Constants.ALGORITHM_NOT_SUPPORTED;
            break;
        case 3:
            _lastErrorDescription = "Algorithm does not support Hashing with Key. Please use HMAC instead or remove the Key parameter";
            break;
        case 4:
            _lastErrorDescription = "Key must be specified";
            break;
        case 5:
            _lastErrorDescription = "Invalid key";
            break;
        default:
            break;
        }
    }

    public String getAlgorithm() {
        return _algorithm;
    }

    public void setAlgorithm(String algorithm) {
        isDirty = true;
        this._algorithm = algorithm;
    }

    private boolean anyError() {
        return _lastError != 0;
    }

    public int getErrCode() {
        return _lastError;
    }

    public String getErrDescription() {
        return _lastErrorDescription;
    }
}