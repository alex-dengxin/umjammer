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
 * Class: AODistort @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * distors samples
 * 
 * @version 29.07.00 erster Entwurf oli4 03.08.00 neuer Stil oli4 24.08.00 free amplitude distortion
 * oli4 26.01.01 array-based again... oli4 19.08.01 variable distortion oli4
 */
public class AODistort extends AOperation {
    /**
     * constructor for free amplitude distortion
     */
    public AODistort() // tmp: please implement it....
    {
        super();
    }

    /**
     * constructor for clamping and noise gate
     */
    public AODistort(float threshold, float clamping, int type) {
        super();
        this.threshold = threshold;
        this.clamping = clamping;
        this.type = type;
    }

    // parameters
    private float threshold;

    private float clamping;

    private int type;

    public final static int CLAMPING_TYPE = 1;

    public final static int NOISE_GATING_TYPE = 2;

    /**
     * clamping or noise gate
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // distortion type ?
        progressSupport.entrySubProgress(new ProgressEvent(this));
        switch (type) {
        case CLAMPING_TYPE:
            for (int i = o1; i < (o1 + l1); i++) {
                float s = s1[i];
                progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
                // positive threshold reached ?
                if (s1[i] > threshold)
                    s = clamping;
                // negative threshold reached ?
                else if (s1[i] < -threshold)
                    s = -clamping;

                s1[i] = ch1.mixIntensity(i, s1[i], s);
            }
            break;

        case NOISE_GATING_TYPE:
            for (int i = o1; i < (o1 + l1); i++) {
                float s = s1[i];
                progressSupport.setProgress(new ProgressEvent(this, (i + 1 - o1) * 100 / l1));
                // positive threshold reached ?
                if ((s1[i] < threshold) && (s1[i] > 0))
                    s = clamping;
                // positive threshold reached ?
                else if ((s1[i] > -threshold) && (s1[i] < 0))
                    s = -clamping;

                s1[i] = ch1.mixIntensity(i, s1[i], s);
            }
            break;
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));
    }

    /**
     * variable distortion, ch1 will be distorted in function of the transferfunction ch2 (x=input, unit of x=1, y=output, unit of
     * y=1) neutral curve: value[index] = index
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        float f[] = ch2.getChannel().sample;

        // mark changed channels...
        ch1.getChannel().changeId();

        // distort
        progressSupport.entrySubProgress(new ProgressEvent(this));
        float s;
        for (int i = 0; i < l1; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
            // value...
            float in = s1[i + o1];

            if (in >= 0) {
                s = AOToolkit.interpolate3(f, in);
            } else {
                s = -AOToolkit.interpolate3(f, -in);
            }

            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], s);
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));
    }

}
