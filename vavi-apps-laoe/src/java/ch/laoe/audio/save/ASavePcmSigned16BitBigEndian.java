
package ch.laoe.audio.save;

import javax.sound.sampled.AudioFormat;


/**
 * PCM signed 8bit file saver
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.11.00 new stream-technique oli4 <br>
 */
public class ASavePcmSigned16BitBigEndian extends ASave {
    /**
     * constructor
     */
    public ASavePcmSigned16BitBigEndian() {
        samplePtr = 0;
        channelPtr = 0;
        bytePtr = 0;
    }

    public ASave duplicate() {
        return new ASavePcmSigned16BitBigEndian();
    }

    public boolean supports(AudioFormat af) {
        if ((af.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) && (af.getSampleSizeInBits() == 16) && af.isBigEndian()) {
            return true;
        } else {
            return false;
        }
    }

    private int samplePtr;

    private int channelPtr;

    private int bytePtr;

    public int read() {
        // calculate index
        int data = 0;

        // calculate data
        if (samplePtr < layer.getChannel(channelPtr).sample.length) {
            switch (bytePtr) {
            case 1:
                data = (int) layer.getChannel(channelPtr).sample[samplePtr] & 0x000000FF;
                break;

            case 0:
                data = ((int) layer.getChannel(channelPtr).sample[samplePtr] >> 8) & 0x000000FF;
                break;
            }
        }

        // update pointers
        bytePtr = ++bytePtr % 2;
        if (bytePtr == 0) {
            channelPtr = ++channelPtr % channels;
            if (channelPtr == 0) {
                samplePtr++;
            }
        }

        // is it the end of the layer ?
        boolean isEnd = true;
        for (int i = 0; i < channels; i++) {
            if (samplePtr < layer.getChannel(i).sample.length) {
                isEnd = false;
            }
        }

        if (isEnd) {
            return -1;
        } else {
            return data;
        }
    }
}
