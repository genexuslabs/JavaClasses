package com.genexus.security.web.jose.jwt.pem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * Can read PEM from disk - depends on BouncyCastle PemReader
 */
class PemFileReader {

    private PemObject pemObject;

    public PemFileReader(final String filename) throws IOException {
        final PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(filename)));
        try {
            this.pemObject = pemReader.readPemObject();
        } finally {
            pemReader.close();
        }
    }

    public void write(final String filename) throws IOException {
        final PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)));
        try {
            pemWriter.writeObject(this.pemObject);
        } finally {
            pemWriter.close();
        }
    }

    public PemObject getPemObject() {
        return pemObject;
    }

}
