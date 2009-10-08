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

package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * cut all silent parts, which mplitude is lower than slienceLimit
 * for a longer time [samples] than tMinSilence.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 29.10.00 erster Entwurf oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          26.01.01 array-based again... oli4 <br>
 */
public class AOAutoCropSilence extends AOperation {
    public AOAutoCropSilence(float silenceLimit, int tMinSilence) {
        super();
        this.silenceLimit = silenceLimit;
        this.tMinSilence = tMinSilence;
    }

    private float silenceLimit;

    private int tMinSilence;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        int ns = o1;
        int i = o1;
        int ss = 0;

        final int NOISE = 1;
        final int BEGIN_OF_SILENCE = 2;
        final int SILENCE = 3;
        int state = NOISE;

        try {
            // compress noise...
            while (i < o1 + l1) {
                // statemachine
                switch (state) {
                case NOISE:
                    s1[ns++] = s1[i];
                    // silence ?
                    if (Math.abs(s1[i]) <= silenceLimit) {
                        ss = 1;
                        state = BEGIN_OF_SILENCE;
                    }
                    break;

                case BEGIN_OF_SILENCE:
                    s1[ns++] = s1[i];
                    // still silence ?
                    if (Math.abs(s1[i]) <= silenceLimit) {
                        // min silence time reached ?
                        if (ss++ >= tMinSilence) {
                            state = SILENCE;
                        }
                    }
                    // noise again ?
                    else {
                        state = NOISE;
                    }
                    break;

                case SILENCE:
                    // noise again ?
                    if (Math.abs(s1[i]) > silenceLimit) {
                        s1[ns++] = s1[i];
                        state = NOISE;
                    }
                    break;
                }
                i++;
            }

            float tmp[] = new float[s1.length + ns - 1 - l1];

            // copy left
            for (int j = 0; j < o1; j++)
                tmp[j] = s1[j];

            // copy compressed noise
            for (int j = o1; j < ns; j++)
                tmp[j] = s1[j];

            // copy right
            for (int j = ns; j < tmp.length; j++)
                tmp[j] = s1[j];

            // replace original
            ch1.getChannel().sample = tmp;
        } catch (ArrayIndexOutOfBoundsException oob) {
        }

    }
}
