/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.tv.xlet.XletContext;


/**
 * AdaSound. 
 * <pre>
 *   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * | B| C| L| K| 0| 2| 0| 0|s start    |e start    |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |<- reserved                              
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 *                       ->|
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * </pre>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 081014 nsano initial version <br>
 */
class AdaSound {
    private DecodingInputStream dis;
    /** ada file names */
    private String[] sounds = new String[2];

    public AdaSound(XletContext context, DecodingInputStream dis) {
        this.dis = dis;

        String adaRoot = System.getProperty("dvb.persistent.root");
        String orgId = (String) context.getXletProperty("dvb.org.id");
        String appId = (String) context.getXletProperty("dvb.app.id");
        String orgPath = adaRoot + File.separator + orgId;
        String appPath = orgPath + File.separator + appId;

        this.sounds[0] = appPath + File.separator + "sound1.bdmv";
        this.sounds[1] = appPath + File.separator + "sound2.bdmv";
    }

    /** in [sec] */
    float length = 2.5f;

    /** return in [ms] */
    public int decode() throws IOException {
        byte[] bytes = new byte[(int) (BD_J_SAMPLING_FREQUENCY * length) * 2 * 2];

        int r = dis.decode(bytes);

        writePart(bytes, 0, r);

        return (int) (length * 1000 * (r / ((BD_J_SAMPLING_FREQUENCY * length) * 2 * 2)));
    }

    /** current ada file's url */
    public URL getURL() throws IOException {
System.err.println("sound: " + sounds[no]);
        return new File(sounds[no]).toURL();
    }

    public int available() throws IOException {
        return dis.available();
    }

    public void close() throws IOException {
        dis.close();
    }

    // BD-J sampling frequency for interactive sounds
    public static final int BD_J_SAMPLING_FREQUENCY = 48000; // Hz
    // BD-J bits per sample
    static final int BD_J_SAMPLE_SIZE = 16; // bits

    // file magic for .bdmv files
    final byte[] SOUND_BDMV_TYPE_INDICATOR = "BCLK".getBytes();
    // sound.bdmv version string     
    final byte[] SOUND_BDMV_VERSION = "0200".getBytes();

    int no = 0;

    /** output bdmv in ada */
    void writePart(byte[] bytes, int offset, int length) throws IOException {
        int[] channels = { 2 };
        int[] frameLengths = { length / (2 * 2) };
        
System.err.println("sound: " + sounds[no]);
        OutputStream os = new FileOutputStream(sounds[no]);
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

        writeSoundHeader(dos, 1);
        writeSoundAttributes(dos, channels, frameLengths);

        dos.write(bytes, offset, length);

        os.flush();
        os.close();
    }

    /** write bdmv header */
    private void writeSoundHeader(DataOutputStream dos, int numInputs) throws IOException {
        // Refer to section 5.6.3 sound.bdmv - Syntax table
        dos.write(SOUND_BDMV_TYPE_INDICATOR);
        dos.write(SOUND_BDMV_VERSION);
        
        // Section 5.6.4.1 SoundIndex() - Syntax table
        final int sizeTillSoundIndex = 
                    4 + /* type indicator */
                    4 + /* version */
                    4 + /* SoundData_start_address */
                    4 + /* ExtensionData_start_address */
                    24;   /* reserved_for_future_use */

        final int perEntrySize =
                        1 + /* channel_configuration, sampling freq */
                        1 + /* bits per sample + align */
                        4 + /* sound_data_start_address */
                        4;  /* sound_data_length */

        final int sizeofSoundIndex = 
                        4 +  /* length */
                        1 +  /* reserved */
                        1 +  /* number of entries */
                        /* variable size based on number of entries */
                        (numInputs*perEntrySize); 


        // SoundData_start_address (4 bytes)
        dos.writeInt(sizeTillSoundIndex + sizeofSoundIndex);

        // ExtensionData_start_address  (4 bytes)
        dos.writeInt(0);

        // reserved (24 bytes) 
        for (int i = 0; i < 24; i++) {
            dos.write(0);
        }

        // SoundIndex() start..

        /*
         * length (4 bytes) - length is number of bytes immediately following 
         * length field and up to the end of SoundIndex() -- so, it does *not* 
         * include the size of the 'length' field itself. (section 5.6.4.2)
         */
        dos.writeInt(sizeofSoundIndex - 4);
        // reserved (1 byte)
        dos.write(0);
        // number of sound entries (1 byte)
        dos.write(numInputs);
    }

    /** write bdmv attributes */
    private void writeSoundAttributes(DataOutputStream dos, int[] channels, int[] frameLengths) 
        throws IOException {
        /*
         * Refer to table 5.6.4.1 SoundIndex() - Syntax
         */
        int totalSize = 0;
        for (int i = 0; i < channels.length; i++) {
            int outputFrameSize = channels[i] * BD_J_SAMPLE_SIZE / 8;
            int currentSize = frameLengths[i] * outputFrameSize;
            boolean isStereo = channels[i] > 1;

            // channel configuration (4 bits): 1=mono, 3=stereo
            // sampling frequency (4 bits): must be 1=48 kHz
            dos.write(isStereo ? 0x31 : 0x11);

            // bits per sample (2 bits)
            // reserved (6 bits): must be 1 (16 bits/sample)
            dos.write(0x40);

            // sound_data_start_address (4 bytes)
            dos.writeInt(totalSize);

            // sound_data_length (4 bytes)
            dos.writeInt(currentSize);

            totalSize += currentSize;
        }
    }
}

/* */
