
package ch.laoe.audio.save;

import javax.sound.sampled.AudioFormat;


/*********************************************************************************************************************************
 * 
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with LAoE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * Class: ASaveUlaw8Bit @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * ULAW 8bit file saver. this class is not tested because java sound api does not support writing .au files at the
 * time!!!!!!!
 * 
 * @version 27.11.00 new stream-technique oli4
 * 
 */
public class ASaveUlaw8Bit extends ASave {

    /**
     * constructor
     */
    public ASaveUlaw8Bit() {
        super();
        samplePtr = 0;
        channelPtr = 0;
    }

    public ASave duplicate() {
        return new ASaveUlaw8Bit();
    }

    public boolean supports(AudioFormat af) {
        if (af.getEncoding() == AudioFormat.Encoding.ULAW) {
            return true;
        } else {
            return false;
        }
    }

    private int samplePtr;

    private int channelPtr;

    public int read() {
        // calculate index
        float data = 0;

        // calculate data
        if (samplePtr < layer.getChannel(channelPtr).sample.length) {
            // get 16bit data
            data = layer.getChannel(channelPtr).sample[samplePtr];

            // perform ulaw to -1..1
            data = (float) (Math.log(1 + 255 * data / 32768) / Math.log(256));

            // map to 0..255
            data = data * 128 + 128;
            if (data > 255)
                data = 255;
            else if (data < 0)
                data = 0;
        }

        // update pointers
        channelPtr = ++channelPtr % channels;
        if (channelPtr == 0) {
            samplePtr++;
        }

        // is it the end of the layer ?
        boolean isEnd = true;
        for (int i = 0; i < channels; i++) {
            if (samplePtr < layer.getChannel(i).sample.length) {
                isEnd = false;
            }
        }

        if (isEnd)
            return -1;
        else
            return (int) data;
    }
}
