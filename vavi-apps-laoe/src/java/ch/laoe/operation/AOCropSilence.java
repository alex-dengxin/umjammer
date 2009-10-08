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
 * Class: AOCropSilence @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * cut the silent borders.
 * 
 * @version 28.10.00 erster Entwurf oli4 19.12.00 float audio samples oli4 26.01.01 array-based again...
 * oli4
 * 
 */
public class AOCropSilence extends AOperation {
    public AOCropSilence(float limit) {
        super();
        silenceLimit = limit;
    }

    private float silenceLimit;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        int noiseStart = o1;
        int noiseEnd = o1 + l1;

        // search first noise...
        for (int i = o1; i < o1 + l1; i++) {
            if (Math.abs(s1[i]) > silenceLimit) {
                noiseStart = i;
                break;
            }
        }

        // search last noise...
        for (int i = o1 + l1 - 1; i >= o1; i--) {
            if (Math.abs(s1[i]) > silenceLimit) {
                noiseEnd = i;
                break;
            }
        }

        // new track size
        int l = 0;
        if ((noiseEnd - noiseStart) > 0)
            l = noiseEnd - noiseStart + 1;

        if (l < 2)
            l = 2;

        float tmp[] = new float[l];

        try {
            // copy noise only
            for (int i = 0; i < l; i++)
                tmp[i] = s1[i + noiseStart];
        } catch (ArrayIndexOutOfBoundsException oob) {
        }

        // replace original
        ch1.getChannel().sample = tmp;

    }
}
