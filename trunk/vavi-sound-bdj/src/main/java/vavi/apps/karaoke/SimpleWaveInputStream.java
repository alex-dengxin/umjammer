/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


/**
 * SimpleWaveInputStream. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 081014 nsano initial version <br>
 */
class SimpleWaveInputStream extends DecodingInputStream {

    /** */
    SimpleWaveInputStream(InputStream in) throws IOException {
        super(in);
        int length = 44; // wave file header length
        long l = 0;
        while (l < length) {
            long r = in.skip(length - l);
            if (r < 0) {
                throw new EOFException(l + "/" + length);
            }
            l += r;
        }
    }

    /* */
    public int decode(byte[] b) throws IOException {
        int l = 0;
        while (l < b.length) {
            int r = in.read(b, l, b.length - l);
            if (r < 0) {
                break;
            }
            l += r;
        }

        // little endian to big endian
        for (int i = 0; i < l / 2; i++) {
            byte b1 = b[i * 2]; 
            byte b2 = b[i * 2 + 1];
            b[i * 2] = b2; 
            b[i * 2 + 1] = b1;
        }

        return l;
    }
}

/* */
