package com.genexus.eo.mqtt;

import org.junit.Test;

import java.security.Provider;
import java.security.Security;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;

public class SignatureAlgorithmsTests {

    @Test
    public void printSignatureAlgorithms() {
        boolean RSAalgorithmFound = false;
        TreeSet<String> algorithms = new TreeSet<>();
        for (Provider provider : Security.getProviders())
            for (Provider.Service service : provider.getServices())
                if (service.getType().equals("Signature"))
                    algorithms.add(service.getAlgorithm());
        for (String algorithm : algorithms) {
            System.out.println(algorithm);
            if (algorithm.equals("RSA") || algorithm.equals("NONEwithRSA")) {
                RSAalgorithmFound = true;
                break;
            }
        }
        assertTrue(RSAalgorithmFound);
    }
}
