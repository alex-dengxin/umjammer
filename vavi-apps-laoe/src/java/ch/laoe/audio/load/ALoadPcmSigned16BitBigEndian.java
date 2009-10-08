
package ch.laoe.audio.load;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import ch.laoe.clip.ALayer;


/**
 * Class: ALoadPcmSigned16BitBigEndian @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * PCM signed 16bit big endian file loader
 * 
 * @version 22.11.00 new stream-technique oli4
 */
public class ALoadPcmSigned16BitBigEndian extends ALoad {
    /**
     * constructor
     */
    public ALoadPcmSigned16BitBigEndian() {
        super();
    }

    public ALoad duplicate() {
        return new ALoadPcmSigned16BitBigEndian();
    }

    public boolean supports(AudioFormat af) {
        if ((af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) && (af.getSampleSizeInBits() == 16) && af.isBigEndian()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * reads from input-stream, writes into layer, maximum length samples, from offset, returns the number of written samples.
     */
    public int read(ALayer l, int offset, int length) throws IOException {
        int channels = audioInputStream.getFormat().getChannels();
        int readLength = audioInputStream.read(buffer, 0, length * channels * 2);

        // each channel...
        for (int i = 0; i < channels; i++) {
            // each sample...
            for (int j = 0; j < readLength / channels / 2; j++) {
                // convert sample data...
                int index = j * channels * 2 + i * 2;
                int data = (buffer[index + 1] & 0x000000FF) | (buffer[index] << 8);
                l.getChannel(i).sample[offset + j] = data;
                // System.out.println("i="+index+" d="+data);
            }
        }
        if (readLength >= 0)
            return readLength / channels / 2;
        else
            return readLength;
    }
}
