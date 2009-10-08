
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * general amplifier
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 02.06.01 first draft oli4 <br>
 *          01.09.01 perform widening in steps to avoid resampling effect oli4 <br>
 *          02.12.2001 modify ch1/2 separatly oli4 <br>
 *          29.12.2001 increase variable widening quality by not vary the dephasing but the wet/dry part oli4
 */
public class AONarrowWide extends AOperation {
    public AONarrowWide(boolean modifyCh1, boolean modifyCh2) {
        super();
        this.modifyCh1 = modifyCh1;
        this.modifyCh2 = modifyCh2;
    }

    /**
     * @param wide constant narrow/widening factor
     */
    public AONarrowWide(boolean modifyCh1, boolean modifyCh2, float wide) {
        this(modifyCh1, modifyCh2);
        this.wide = wide;
    }

    // parameters
    private float wide; // 0=narrowest mono, 1.0=normal, 2.0=widest

    protected boolean modifyCh1, modifyCh2; // applied channels

    /**
     * @param wet narrow intensity 0..1 (1=narrowest)
     */
    protected void narrowing(AChannelSelection ch1, AChannelSelection ch2, int index, float wet) {
        float f = 1.f - (wet / 2); // factor 0.5 .. 1
        float fc = 1.f - f; // complement factor 0 .. 0.5
        float s1 = ch1.getChannel().sample[index];
        float s2 = ch2.getChannel().sample[index];

        if (modifyCh1) {
            float m = s1 * f + s2 * fc;
            ch1.getChannel().sample[index] = ch1.mixIntensity(index, s1, m);
        }
        if (modifyCh2) {
            float m = s2 * f + s1 * fc;
            ch2.getChannel().sample[index] = ch2.mixIntensity(index, s2, m);
        }
    }

    /**
     * @param wet wide intensity 0..1 (1=widest)
     */
    protected void widening(AChannelSelection ch1, AChannelSelection ch2, int index, float wet) {
        int d = 1000;
        float f = 1.f - wet; // factor 0..1
        float fc = 1.f - f; // complement factor 1..0
        float s1[] = ch1.getChannel().sample;
        float s2[] = ch2.getChannel().sample;

        try {
            if (modifyCh1) {
                float m = s1[index] * f + s1[index + d] * fc;
                s1[index] = ch1.mixIntensity(index, s1[index], m);
            } else if (modifyCh2) {
                float m = s2[index] * f + s2[index + d] * fc;
                s2[index] = ch2.mixIntensity(index, s2[index], m);
            }
        } catch (Exception e) {
        }
    }

    /**
     * performs a constant widening
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        // float s1[] = ch1.getChannel().sample;
        // float s2[] = ch2.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        if (modifyCh1) {
            ch1.getChannel().changeId();
        }
        if (modifyCh2) {
            ch2.getChannel().changeId();
        }

        try {
            // each sample of operand...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                // narrowing to mono ?
                if (wide < 1) {
                    narrowing(ch1, ch2, o1 + i, 1 - wide);
                }
                // widening ?
                else {
                    widening(ch1, ch2, o1 + i, wide - 1);
                }
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

    /**
     * performs a variable widening on channel1 and channel2, taking into account only selection-range of the first channel.
     * 0=narrowest, 1=neutral, 2=widest
     */
    public void operate(AChannelSelection channel1, AChannelSelection channel2, AChannelSelection param) {
        // float s1[] = channel1.getChannel().sample;
        // float s2[] = channel2.getChannel().sample;
        float p[] = param.getChannel().sample;
        int o1 = channel1.getOffset();
        int l1 = channel1.getLength();

        // mark changed channels...
        if (modifyCh1) {
            channel1.getChannel().changeId();
        }
        if (modifyCh2) {
            channel2.getChannel().changeId();
        }

        try {
            // each sample of operand...
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                float wide = p[i + o1];
                // narrowing to mono ?
                if (wide < 1) {
                    narrowing(channel1, channel2, o1 + i, 1 - wide);
                }
                // widening ?
                else {
                    widening(channel1, channel2, o1 + i, wide - 1);
                }
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }
}
