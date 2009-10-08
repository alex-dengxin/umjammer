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

package ch.laoe.audio.capture;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;

import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;


/**
 * parentclass of all capture classes.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 23.11.00 new stream-technique oli4
 */
public abstract class ACapture {
    /**
     * constructor
     */
    protected ACapture() {
        buffer = new byte[bufferLength];
    }

    public abstract ACapture duplicate();

    public abstract boolean supports(AudioFormat af);

    protected static AClip clip;

    public void setClip(AClip c) {
        clip = c;
    }

    protected static TargetDataLine line;

    public void setLine(TargetDataLine l) {
        line = l;
    }

    // buffer
    protected byte buffer[];

    private static final int bufferLength = 4000;

    /**
     * reads from input-stream, writes into layer, maximum length samples, from offset, returns the number of written samples.
     */
    public abstract int read(ALayer l, int offset, int length) throws IOException;

    public void stop() {
        // line.flush();
        line.stop();
        line.close();
    }

}
