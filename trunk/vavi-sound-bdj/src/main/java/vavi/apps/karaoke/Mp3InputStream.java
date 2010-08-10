/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import java.io.IOException;
import java.io.InputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;


/**
 * MP3 decoder using Java Layer
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 081014 nsano initial version <br>
 * @see "http://www.javazoom.net/javalayer/javalayer.html"
 */
class Mp3InputStream extends DecodingInputStream {

    /** */
    private Bitstream bitstream;

    /** */
    private Decoder decoder;

    /** */
    Mp3InputStream(InputStream in) throws IOException {
        super(in);
        bitstream = new Bitstream(in);      
        decoder = new Decoder();
    }

    /** */
    private int[] previousSampleBuffer;

    /* */
    public int decode(byte[] b) throws IOException {
        try {
            int l = 0;

            if (previousSampleBuffer != null) {
                int[] outBuffer = previousSampleBuffer;
                int length = outBuffer.length;
                for (int i = 0; i < length; i++) { // BE
                    b[i * 2] = (byte) (outBuffer[i] >>> 8);
                    b[i * 2 + 1] = (byte) outBuffer[i];
                }
                l = length * 2;
            }

            while (true) {
                Header header = bitstream.readFrame();
                if (header == null) {
                    throw new IllegalStateException("header is null");
                }
                SampleBuffer sampleBuffer = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                short[] sample = sampleBuffer.getBuffer();
                int length = sampleBuffer.getBufferLength();
                int[] outBuffer = null;

//System.err.println(sampleBuffer.getSampleFrequency() + ", " + AdaSound.BD_J_SAMPLING_FREQUENCY);
                if (sampleBuffer.getSampleFrequency() != AdaSound.BD_J_SAMPLING_FREQUENCY) {
                    Polyphase resampler = new Polyphase(sampleBuffer.getSampleFrequency(), AdaSound.BD_J_SAMPLING_FREQUENCY);
                    int[] inBuffer = new int[length];
                    for (int i = 0; i < length; i++) {
                        inBuffer[i] = sample[i];
                    }
                    outBuffer = resampler.resample(inBuffer);
                    length = outBuffer.length;
                } else {
                    outBuffer = new int[length];
                    for (int i = 0; i < length; i++) {
                        outBuffer[i] = sample[i];
                    }
                }

                bitstream.closeFrame();

                if (l + length * 2 > b.length) {
                    previousSampleBuffer = outBuffer;
                    break;
                }
                for (int i = 0; i < length; i++) { // BE
                    b[l + i * 2] = (byte) (outBuffer[i] >>> 8);
                    b[l + i * 2 + 1] = (byte) outBuffer[i];
                }
                l += length * 2;
                if (l == b.length) {
                    previousSampleBuffer = null;
                    break;
                }
            }

            return l;
        } catch (JavaLayerException e) {
            throw new IllegalStateException(e);
        }
    }
}

/* */
