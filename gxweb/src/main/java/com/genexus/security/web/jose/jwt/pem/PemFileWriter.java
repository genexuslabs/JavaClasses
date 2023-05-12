package com.genexus.security.web.jose.jwt.pem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.Key;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * Can write PEM to disk - depends on BouncyCastle PemWriter
 */
class PemFileWriter {

    private PemObject pemObject;

    public PemFileWriter(final Key key, final String description) {
        this.pemObject = new PemObject(description, key.getEncoded());
    }

    public void write(final String filename) throws IOException {
        final PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(filename)));
        try {
            pemWriter.writeObject(this.pemObject);
        } finally {
            pemWriter.close();
        }
    }

}
