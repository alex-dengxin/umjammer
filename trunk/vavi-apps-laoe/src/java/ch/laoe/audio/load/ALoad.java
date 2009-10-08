/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.audio.load;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import ch.laoe.clip.ALayer;
import ch.laoe.ui.Debug;


/**
 * parentclass of all load-classes.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * @version 22.11.00 new stream-technique oli4
 */
public abstract class ALoad {
    /**
     * constructor
     */
    protected ALoad() {
        buffer = new byte[bufferLength];
    }

    public abstract ALoad duplicate();

    protected void setAudioInputStream(AudioInputStream ais, int sampleLength) {
        audioInputStream = ais;
        this.sampleLength = sampleLength;
    }

//    public static final ALoad createLoad(File f) {
//        file = f;
//
//        // try to find out audio file format
//        try {
//            audioInputStream = AudioSystem.getAudioInputStream(file);
//            AudioFormat af = audioInputStream.getFormat();
//
//            if (af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) { // PCM signed ?
//                if (af.getSampleSizeInBits() == 8) { // 8 bit ?
//                    return new ALoadPcmSigned8Bit();
//                } else if (af.getSampleSizeInBits() == 16) { // 16 bit ?
//                    if (af.isBigEndian()) { // big endian ?
//                        return new ALoadPcmSigned16BitBigEndian();
//                    } else { // little endian ?
//                        return new ALoadPcmSigned16BitLittleEndian();
//                    }
//                }
//            } else if (af.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) { // PCM unsigned ?
//                if (af.getSampleSizeInBits() == 8) { // 8 bit ?
//                    return new ALoadPcmUnsigned8Bit();
//                } else if (af.getSampleSizeInBits() == 16) { // 16 bit ?
//                    if (af.isBigEndian()) { // big endian ?
//                        return new ALoadPcmUnsigned16BitBigEndian();
//                    } else { // little endian ?
//                        return new ALoadPcmUnsigned16BitLittleEndian();
//                    }
//                }
//            } else if (af.getEncoding() == AudioFormat.Encoding.ALAW) { // ALAW ?
//            } else if (af.getEncoding() == AudioFormat.Encoding.ULAW) { // ULAW ?
//            }
//            return null;
//        } catch (UnsupportedAudioFileException uafe) {
//            return null;
//        } catch (IOException ioe) {
//            return null;
//        }
//    }
     
    protected static File file;

    protected static AudioInputStream audioInputStream;

    protected int sampleLength;

    public void setFile(File f) {
        file = f;
    }

    // buffer
    protected byte buffer[];

    private static final int bufferLength = 16000;

    /**
     * reads from input-stream, writes into layer, maximum length samples, from offset, returns the number of written samples.
     */
    public abstract int read(ALayer l, int offset, int length) throws IOException;

    public void close() {
        try {
            audioInputStream.close();
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
        }
    }

    public abstract boolean supports(AudioFormat af);

    public int getSampleWidth() {
        return audioInputStream.getFormat().getSampleSizeInBits();
    }

    public float getSampleRate() {
        return audioInputStream.getFormat().getSampleRate();
    }

    public int getChannels() {
        return audioInputStream.getFormat().getChannels();
    }

    public int getSampleLength() {
        // AudioFormat af = audioInputStream.getFormat();

        /*
         * Debug.println(3, "audioformaaat = "+af.toString()); Debug.println( 3, "framelength =
         * "+audioInputStream.getFrameLength()+ " framesize = "+af.getFrameSize()+ " channels = "+af.getChannels()+ " samplesize =
         * "+(af.getSampleSizeInBits() >> 3));
         */

        /*
         * return (int)(audioInputStream.getFrameLength() * af.getFrameSize() / af.getChannels() / (af.getSampleSizeInBits() >>
         * 3));
         */
        // Debug.println(3, "samplelength = "+sampleLength);
        return sampleLength;
    }
}
