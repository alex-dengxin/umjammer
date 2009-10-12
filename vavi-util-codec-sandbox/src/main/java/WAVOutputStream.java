/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Writes a WAV file.
 * 
 * @see "http://www.mediatel.lu/workshop/audio/fileformat"
 */
public class WAVOutputStream extends FilterOutputStream {

    private static final int RIFFid = ('R' << 24) | ('I' << 16) + ('F' << 8) + 'F';

    private static final int WAVEid = ('W' << 24) | ('A' << 16) + ('V' << 8) + 'E';

    private static final int fmtid = ('f' << 24) | ('m' << 16) + ('t' << 8) + ' ';

    private static final int dataid = ('d' << 24) | ('a' << 16) + ('t' << 8) + 'a';

    private int numChannels;

    @SuppressWarnings("unused")
    private int sampleDepth;

    @SuppressWarnings("unused")
    private int sampleRate;

    @SuppressWarnings("unused")
    private int numSamples;

    // for intel byte order. YAY!
    private void writeShort(short val) throws IOException {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
    }

    private void writeInt(int val) throws IOException {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
        out.write((val >> 16) & 0xFF);
        out.write((val >> 24) & 0xFF);
    }

    // this allows us to write binary data to the stream
    private DataOutputStream dos;

    public WAVOutputStream(OutputStream _out, int channels, int rate, int depth, int length) throws IOException,
                                                                                            Exception {
        super(_out);

        numChannels = channels;
        sampleRate = rate;
        sampleDepth = depth;
        numSamples = length;

        dos = new DataOutputStream(out);

        // write "RIFF"
        dos.writeInt(RIFFid);
        // write chunk size
        writeInt(4 + 4 + 4 + 24 + 4 + 4 + channels * depth * length / 8);
        // read "WAVE"
        dos.writeInt(WAVEid);
        // write format chunk
        dos.writeInt(fmtid);
        writeInt(16);
        writeShort((short) 1);
        writeShort((short) channels);
        writeInt(rate);
        writeInt(rate * channels * depth / 8);
        writeShort((short) (channels * depth / 8));
        writeShort((short) depth);
        dos.writeInt(dataid);
        writeInt(channels * depth * length / 8);
    }

    public void writeSample(byte b[], int start, int length) throws IOException {
        out.write(b, start * numChannels, length * numChannels);
    }

    public void writeSample(short b[], int start, int length) throws IOException {
        for (int off = 0; off < length; off++) {
            for (int channel = 0; channel < numChannels; channel++) {
                writeShort(b[channel + (start + off) * numChannels]);
            }
        }
    }

    public void readSample(int b[], int start, int length) throws IOException {
        for (int off = 0; off < length; off++) {
            for (int channel = 0; channel < numChannels; channel++) {
                writeInt(b[channel + (start + off) * numChannels]);
            }
        }
    }
}
