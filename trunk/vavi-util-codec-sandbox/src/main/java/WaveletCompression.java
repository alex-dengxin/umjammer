/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import vavix.io.HuffmanInputStream;
import vavix.io.HuffmanOutputStream;


class WaveletCompression {

    /** quantize and compress are combined. */
    public static void compress(int data[], int levels, int bits[], OutputStream out) throws IOException {
        int i;
        int level = 0;
        int skip = 2 << (levels - 1);

        DataOutputStream dos = new DataOutputStream(out);
        for (i = 0; i < data.length; i += skip) {
            dos.writeShort(data[i]);
        }
        dos.flush();

        for (; skip >= 2; skip >>= 1) {
            int nz = 0;
            // an extra bit so that we can rle 0's
            HuffmanOutputStream hos = new HuffmanOutputStream(out, 16);
            i = skip / 2;
            int escbits = 8; // 10 bits[level];
            while (i < data.length) {
                // want round, not floor
                int v = data[i] + (32768 >> bits[level]);
                i += skip;
                // if(v > 32767)
                // v = 32767;
                // if(v < -32768)
                // v = -32768;
                v &= ~((1 << (16 - bits[level])) - 1);
                if (v == 0) {
                    while ((i < data.length) && (((data[i] + (32768 >> bits[level])) & ~((1 << (16 - bits[level])) - 1)) == 0) && (nz < (1 << escbits))) {
                        nz++;
                        i += skip;
                    }
                    if (nz > 3) {
                        hos.writeEscape(nz - 4, escbits);
                    } else {
                        while (nz >= 0) {
                            hos.writeInt(32768);
                            nz--;
                        }
                    }
                    nz = 0;
                } else {
                    hos.writeInt(v + 32768);
                }
            }
            hos.flush();
            level++;
        }
    }

    /** */
    public static void decompress(int data[], int levels, int bits[], InputStream in) throws IOException {
        int level = 0;
        int skip = 2 << (levels - 1);
        int i;

        DataInputStream dis = new DataInputStream(in);
        for (i = 0; i < data.length; i += skip) {
            data[i] = dis.readShort();
        }

        for (; skip >= 2; skip >>= 1) {
            int escbits = 8; // 10 bits[level];
            // an extra bit so that we can rle 0's
            HuffmanInputStream his = new HuffmanInputStream(in, 16);
            i = skip / 2;
            while (i < data.length) {
                int v = his.readInt();
                if (v == -1) {
                    int nz = his.readEscape(escbits) + 5;
                    for (; nz > 0; nz--) {
                        data[i] = 0;
                        i += skip;
                    }
                } else {
                    data[i] = v - 32768;
                    i += skip;
                }
            }
            level++;
        }
    }
}

/* */
