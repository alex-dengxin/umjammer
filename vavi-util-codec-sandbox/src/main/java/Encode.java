/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import vavi.util.wavelet.FourTwoWavelet;
import vavi.util.wavelet.Wavelet;


class Encode {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("No file specified");
            return;
        }
        String filename = args[0];

        Wavelet wavelet = new FourTwoWavelet();

        WAVInputStream sound = new WAVInputStream(new BufferedInputStream(new FileInputStream(filename)));
        int length = sound.length();
        int channels = sound.channels();
        int depth = sound.depth();
        int rate = sound.rate();

System.out.println("Bits: " + depth + " Channels: " + channels + " Sample Rate: " + rate + " Length: " + length);
        short s[] = new short[channels * length];
        int m[] = new int[channels * length];
        sound.readSample(s, 0, length);
        for (int i = 0; i < s.length; i++) {
            m[i] = s[i];
        }
        wavelet.doFWT(m, 8);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("test.bin"));
        int bitcounts[] = new int[8];
        bitcounts[0] = 13;
        bitcounts[1] = 11;
        bitcounts[2] = 10;
        bitcounts[3] = 10;
        bitcounts[4] = 9;
        bitcounts[5] = 9;
        bitcounts[6] = 8;
        bitcounts[7] = 8;
        WaveletCompression.compress(m, 8, bitcounts, bos);
        bos.close();
//        for (int i = 0; i < m.length; i++) {
//            m[i] = (short) m[i];
//        }

        BufferedInputStream bin = new BufferedInputStream(new FileInputStream("test.bin"));
        WaveletCompression.decompress(m, 8, bitcounts, bin);
        wavelet.doIFWT(m, 8);

        for (int i = 0; i < s.length; i++) {
            if (m[i] > 32767) {
                s[i] = 32767;
            } else {
                if (m[i] < -32768) {
                    s[i] = -32768;
                } else {
                    s[i] = (short) m[i];
                }
            }
        }
        WAVOutputStream s2 = new WAVOutputStream(new BufferedOutputStream(new FileOutputStream("out.wav")), channels, rate, depth, length);
        s2.writeSample(s, 0, length);
        s2.close();
    }
}
