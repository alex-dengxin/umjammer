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
 * auto volume adapts the volume to a equal value.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * @version 08.05.01 first draft oli4 <br>
 *          28.01.02 add backward-mode and calibrate mean before starting oli4
 */
public class AOAutoVolume extends AOperation {
    public AOAutoVolume(int sampleRate, int tAttack, int tRelease, boolean backward) {
        super();
        this.sampleRate = sampleRate;
        this.tAttack = tAttack;
        this.tRelease = tRelease;
        this.backward = backward;
    }

    private int sampleRate;

    private int tReaction;

    private int tAttack;

    private int tRelease;

    private boolean backward;

    // actual amplitude

    private float meanAmplitude;

    private void updateAmplitude(float sample) {
        // attack ?
        sample = Math.abs(sample);
        if (sample > meanAmplitude) {
            meanAmplitude = AOToolkit.movingRmsAverage(meanAmplitude, sample, tAttack);
        }
        // release ?
        else {
            meanAmplitude = AOToolkit.movingRmsAverage(meanAmplitude, sample, tRelease);
        }
    }

    /**
     * performs a variable amplification, where the variable is channel2
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float oldRms = AOToolkit.rmsAverage(s1, o1, l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        // go through a first small range of samples to have an idea of mean amplitude...
        meanAmplitude = 1;
        if (backward) {
            for (int i = l1 - 1; i >= Math.max(0, l1 - tAttack - tRelease); i--) {
                updateAmplitude(s1[i + o1]);
            }
        } else {
            for (int i = 0; i < Math.min(l1, tAttack + tRelease); i++) {
                updateAmplitude(s1[i + o1]);
            }
        }

        // now perform autovolume...
        try {
            progressSupport.entrySubProgress(new ProgressEvent(this));

            if (backward) {
                for (int i = l1 - 1; i >= 0; i--) {
                    progressSupport.setProgress(new ProgressEvent(this, (l1 - i) * 100 / l1));
                    updateAmplitude(s1[i + o1]);
                    // correct
                    s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], 1000 / meanAmplitude * s1[i + o1]);
                }
            } else {
                for (int i = 0; i < l1; i++) {
                    progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                    updateAmplitude(s1[i + o1]);
                    // correct
                    s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], 1000 / meanAmplitude * s1[i + o1]);
                }
            }

            // RMS-calibration
            float newRms = AOToolkit.rmsAverage(s1, o1, l1);
            AOToolkit.multiply(s1, o1, l1, oldRms / newRms);

            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }
}
