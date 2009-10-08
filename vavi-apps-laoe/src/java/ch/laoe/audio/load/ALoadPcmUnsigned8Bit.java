
package ch.laoe.audio.load;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import ch.laoe.clip.ALayer;


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
 * Class: ALoadPcmUnsigned8Bit @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * PCM unsigned 8bit file loader
 * 
 * @version 22.11.00 new stream-technique oli4
 * 
 */
public class ALoadPcmUnsigned8Bit extends ALoad {
    /**
     * constructor
     */
    public ALoadPcmUnsigned8Bit() {
        super();
    }

    public ALoad duplicate() {
        return new ALoadPcmUnsigned8Bit();
    }

    public boolean supports(AudioFormat af) {
        if ((af.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) && (af.getSampleSizeInBits() == 8)) {
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
        int readLength = audioInputStream.read(buffer, 0, length * channels);

        // each channel...
        for (int i = 0; i < channels; i++) {
            // each sample...
            for (int j = 0; j < readLength / channels; j++) {
                // convert sample data...
                int index = j * channels + i;
                int data = (buffer[index] ^ 0x7F);
                l.getChannel(i).sample[offset + j] = data;
            }
        }
        if (readLength >= 0)
            return readLength / channels;
        else
            return readLength;
    }
}
