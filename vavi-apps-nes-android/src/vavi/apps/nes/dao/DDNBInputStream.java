/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.dao;

import java.io.IOException;
import java.io.InputStream;


/**
 * DDNB Input Stream for reading Resources with Metadata
 * 
 * @author : David de Niese
 * @version : 0.56f
 */
public class DDNBInputStream extends InputStream {
    // The InputStream
    InputStream input;

    // The XOR Value
    byte xorStart;

    /**
     * Constructor for Input Stream
     */
    public DDNBInputStream(InputStream in) throws IOException {
        // Check Header
        input = in;
        if (input.read() != 'D' || input.read() != 'D' || input.read() != 'N' || input.read() != 'B') {
            throw new IllegalArgumentException("Invalid DDNB Metadata Resource File");
        }
        // Grab Version Number
        int version = input.read() * 256 + input.read();
        if (version != 1) {
            throw new IllegalArgumentException("DDNB: Cannot understand codec version '" + version + "'");
        }
        // Grab XOR Start Value
        xorStart = (byte) input.read();
        // Check Marker
        if (input.read() != 0) {
            throw new IllegalArgumentException("DDNB: Header Marker is invalid");
        }
    }

    /**
     * Check for Available Data
     */
    public int available() throws IOException {
        return input.available();
    }

    /**
     * Close IO Stream
     */
    public void close() throws IOException {
        input.close();
    }

    /**
     * Reset InputStream
     */
    public void reset() throws IOException {
        input.reset();
    }

    /**
     * Skip Over InputStream
     */
    public long skip(long n) throws IOException {
        return input.skip(n);
    }

    /**
     * Read Byte from InputStream
     */
    public int read() throws IOException {
        return (input.read() ^ xorStart++);
    }

    /**
     * Read InputStream into Buffer
     */
    public int read(byte[] b) throws IOException {
        int retcode = input.read(b);
        for (int i = 0; i < retcode; i++)
            b[i] ^= xorStart++;
        return retcode;
    }

    /**
     * Read InputStream into Buffer
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int retcode = input.read(b, off, len);
        for (int i = 0; i < retcode; i++)
            b[i] ^= xorStart++;
        return retcode;
    }
}
