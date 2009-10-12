/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

package vavix.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * BitInputStream. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080516 nsano initial version <br>
 */
public class BitInputStream extends FilterInputStream {

    /** */
    private int buffer = 0;

    /** */
    private int bitsavail = 0;

    /** */
    public BitInputStream(InputStream in) {
        super(in);
    }

    /** */
    public boolean markSupported() {
        return false;
    }

    /** read a single byte */
    public int read() throws IOException {
        buffer = (buffer << 8) | in.read();
        return (buffer >> bitsavail) & 0xFF;

    }

    /** */
    public int readBit() throws IOException {
        if (bitsavail == 0) {
            buffer = in.read();
            bitsavail = 8;
        }
        bitsavail--;
        return (buffer >> bitsavail) & 1;
    }

    /** */
    public int read(byte b[], int off, int len) throws IOException {
        bitsavail = 0;
        return in.read(b, off, len);
    }

    /** */
    public int read(int length) throws IOException {
        int l = length;
        while (l > bitsavail) {
            buffer = (buffer << 8) | in.read();
            bitsavail += 8;
        }
        bitsavail -= length;
        return (buffer >> bitsavail) & ((1 << length) - 1);
    }
}

/* */
