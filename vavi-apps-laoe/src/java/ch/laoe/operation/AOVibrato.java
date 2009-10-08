
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * vibrato effect, a kind of sinus-resampling.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 25.12.2001 first draft oli4
 */
public class AOVibrato extends AOperation {
    public AOVibrato(int modulationDelay, int modulationPeriod, int modulationShape) {
        super();
        this.modulationDelay = modulationDelay;
        this.modulationPeriod = modulationPeriod;
        this.modulationShape = modulationShape;
    }

    // parameters
    private float modulationDelay, modulationPeriod;

    private int modulationShape;

    // shapes
    public static final int SINUS = 1;

    public static final int TRIANGLE = 2;

    public static final int SAW = 3;

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float tmp[] = new float[l1];

        // mark changed channels...
        ch1.getChannel().changeId();

        // all points...
        progressSupport.entrySubProgress(new ProgressEvent(this));
        for (int i = 0; i < l1; i++) {
            progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));

            // generate actual modulation...
            float iMod = 0;
            switch (modulationShape) {
            case SINUS:
                iMod += (float) Math.sin((i % (int) modulationPeriod) / modulationPeriod * 2 * Math.PI) * modulationDelay;
                break;

            case TRIANGLE:
                iMod += (Math.abs(((i % (int) modulationPeriod) / modulationPeriod * 4) - 2) - 1) * modulationDelay;
                break;

            case SAW:
                iMod += (((i % (int) modulationPeriod) / modulationPeriod * 2) - 1) * modulationDelay;
                break;

            }

            // range limitation...
            double m = o1 + i + iMod;
            if (m < 0)
                m = 0;
            else if (m >= s1.length)
                m = s1.length - 1;

            // modulation
            tmp[i] = AOToolkit.interpolate0(s1, (float) m);
            // tmp[i] = (float)m;
        }
        progressSupport.exitSubProgress(new ProgressEvent(this));

        // write back...
        for (int i = 0; i < tmp.length; i++) {
            s1[i + o1] = ch1.mixIntensity(i + o1, s1[i + o1], tmp[i]);
        }

    }
}
