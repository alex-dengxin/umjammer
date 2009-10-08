
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;
import ch.laoe.ui.Debug;


/**
 * pitch shift operation
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 10.06.01 first draft (f-domain method) oli4 <br>
 *          01.08.01 time-domain block-resampling method oli4 <br>
 *          23.12.01 introduce smooth transition curve oli4 <br>
 *          18.06.02 reduce volume-flicker oli4 <br>
 *          06.05.2003 variable pitch shift added oli4
 * 
 */
public class AOPitchShift extends AOperation {
    /**
     */
    public AOPitchShift(int bufferLength, float transitionFactor) {
        this(1.f, bufferLength, transitionFactor);
    }

    /**
     * @param shiftFactor 1:neutral, <1:lowrer pitch, >1:higher pitch
     * @param bufferLength lehgth of processing blocks
     * @param transitionFactor length of transition from one block to the other 1=bufferlength
     */
    public AOPitchShift(float shiftFactor, int bufferLength, float transitionFactor) {
        this.shiftFactor = shiftFactor;
        buffer = new float[bufferLength];
        transition = new float[(int) (bufferLength * transitionFactor)];

        // prepare smooth transition
        for (int i = 0; i < transition.length; i++) {
            if (i >= transition.length / 2) {
                transition[i] = 1;
            } else {
                transition[i] = 0;
            }
        }
        AOToolkit.smooth(transition, 0, transition.length, transition.length / 8);
    }

    private float shiftFactor;

    private float buffer[];

    private float transition[];

    /**
     * performs a variable pitch shift, where the variable is channel2
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        performPitchShift(ch1, ch2);
    }

    /**
     * performs a constant amplification
     */
    public final void operate(AChannelSelection ch1) {
        performPitchShift(ch1, null);
    }

    /**
     * performs a constant amplification
     */
    private void performPitchShift(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();
        // float oldRms = AOToolkit.rmsAverage(s1, o1, l1);

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            // copy data...
            float tmp[] = new float[l1];
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = s1[o1 + i];
            }

            // geometry...
            int stepWidth = buffer.length - transition.length;
            int crossWidth = transition.length;
            int stepBegin = o1;

            // System.out.println("step width="+stepWidth);
            // System.out.println("cross width="+crossWidth);

            // each step...
            while (stepBegin < o1 + l1) {
                // resample buffer...
                for (int i = 0; i < buffer.length; i++) {
                    float index;
                    // variable ?
                    if (ch2 != null) {
                        index = stepBegin - o1 + i * ch2.getChannel().sample[stepBegin];
                    }
                    // constant ?
                    else {
                        index = stepBegin - o1 + i * shiftFactor;
                    }

                    if (index < l1) {
                        buffer[i] = AOToolkit.interpolate3(tmp, index);
                    } else {
                        buffer[i] = AOToolkit.interpolate3(tmp, i); // put original chunk...
                    }
                }
                // append buffers...
                for (int i = 0; i < buffer.length; i++) {
                    if (stepBegin + i < o1 + l1) {
                        float s;

                        // cross phase ?
                        if (i < crossWidth) {
                            // the two factors below use the smooth transition curve in a shifted way
                            // to avoid volume-flicker...
                            float f1 = transition[i];
                            float f2;
                            int dt = transition.length / 5;
                            if (i < dt) {
                                f2 = 0;
                            } else {
                                f2 = transition[i - dt];
                            }

                            s = (s1[stepBegin + i] * (1.f - f2)) + (buffer[i] * f1);
                            // s1[stepBegin+i] = crossFactor; //visualize steps...
                        }
                        // normal append phase ?
                        else {
                            s = buffer[i];
                        }
                        s1[stepBegin + i] = ch1.mixIntensity(stepBegin + i, s1[stepBegin + i], s);
                    }
                }
                stepBegin += stepWidth;
            }

            // RMS-calibration
            // float newRms = AOToolkit.rmsAverage(s1, o1, l1);
            // AOToolkit.multiply(s1, o1, l1, (float)(oldRms/newRms));

            // zero cross
            AOToolkit.applyZeroCross(s1, o1);
            AOToolkit.applyZeroCross(s1, o1 + l1);
        } catch (ArrayIndexOutOfBoundsException oob) {
            Debug.printStackTrace(5, oob);
        }
    }
}
