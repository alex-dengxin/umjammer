/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

package vavix.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * BitOutputStream. 
 *
 * @author <a href="mailto:sano-n@klab.org">Naohide Sano</a> (nsano)
 * @version 0.00 080516 nsano initial version <br>
 */
public class BitOutputStream extends FilterOutputStream {

    /** */
    private int buffer = 0;

    /** */
    private int bitsfree = 8;

    /** */
    public BitOutputStream(OutputStream _out) {
        super(_out);
    }

    /** */
    public void flush() throws IOException {
        // if there's something in the buffer, just write it.
        if (bitsfree != 8) {
            out.write((buffer << bitsfree) & 0xFF);
            bitsfree = 8;
        }
        out.flush();
    }

    /** write a single byte - assume they want this byte-aligned... */
    public void write(int b) throws IOException {
        // if there's something in the buffer, just write it.
        if (bitsfree != 8) {
            out.write((buffer << bitsfree) & 0xFF);
            bitsfree = 8;
        }
        out.write(b);
    }

    /** write an array of bytes - assume they want this byte-aligned... */
    public void write(byte b[], int off, int len) throws IOException {
        // if there's something in the buffer, just write it.
        if (bitsfree != 8) {
            out.write((buffer << bitsfree) & 0xFF);
            bitsfree = 8;
        }
        out.write(b, off, len);
    }

    /** max we can write here is 24 bit value. */
    public void write(int b, int length) throws IOException {
        bitsfree -= length;
        buffer = (buffer << length) | b;
        while (bitsfree <= 0) {
            out.write((buffer >> (-bitsfree)) & 0xFF);
            bitsfree += 8;
        }
    }
}

/* */