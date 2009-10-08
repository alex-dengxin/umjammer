
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * pan operation
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 02.06.01 first draft oli4 <br>
 *          17.12.01 add different modes, order and constant pan oli4
 */
public class AOPan extends AOperation {
    public AOPan(int mode, int shape) {
        super();
        this.mode = mode;
        this.shape = shape;
    }

    public AOPan(int mode, int shape, float pan) {
        this(mode, shape);
        this.pan = pan;
    }

    public static final int HALF_MODE = 1;

    public static final int MIX_ENDS_MODE = 2;

    public static final int FULL_MODE = 3;

    private int mode;

    public static final int SQUARE_ROOT_SHAPE = -1;

    public static final int LINEAR_SHAPE = 1;

    public static final int SQUARE_SHAPE = 2;

    private int shape;

    private float pan;

    // matrix...
    private float f11, f22, f12, f21;

    private void calculateMatrix(float pan) {
        switch (mode) {
        case MIX_ENDS_MODE:
            f11 = Math.min(1, pan * 2 - 2);
            f22 = Math.min(1, -pan * 2 + 4);
            f12 = 1 - f11;
            f21 = 1 - f22;
            break;

        case FULL_MODE:
            f11 = pan - 1;
            f22 = -pan + 2;
            f12 = 0;
            f21 = 0;
            break;

        // case HALF_MODE:
        default:
            f11 = Math.min(1, pan * 2 - 2);
            f22 = Math.min(1, -pan * 2 + 4);
            f12 = 0;
            f21 = 0;
            break;
        }

        switch (shape) {
        case SQUARE_ROOT_SHAPE:
            f11 = (float) Math.sqrt(f11);
            f22 = (float) Math.sqrt(f22);
            f12 = (float) Math.sqrt(f12);
            f21 = (float) Math.sqrt(f21);
            break;

        case SQUARE_SHAPE:
            f11 *= f11;
            f22 *= f22;
            f12 *= f12;
            f21 *= f21;
            break;

        default:
            break;
        }
    }

    /**
     * performs constant pan on channel1 and channel2, taking into account only selection-range of channel1. value: 1 = channel1
     * 1.5= center 2 = channel2
     */
    public void operate(AChannelSelection channel1, AChannelSelection channel2) {
        float s1[] = channel1.getChannel().sample;
        float s2[] = channel2.getChannel().sample;
        int o1 = channel1.getOffset();
        int l1 = channel1.getLength();
        float ch1, ch2;

        // mark changed channels...
        channel1.getChannel().changeId();
        channel2.getChannel().changeId();

        try {
            calculateMatrix(pan);

            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                ch1 = f11 * s1[o1 + i] + f21 * s2[o1 + i];
                ch2 = f22 * s2[o1 + i] + f12 * s1[o1 + i];
                s1[i + o1] = channel1.mixIntensity(i + o1, s1[i + o1], ch1);
                s2[i + o1] = channel1.mixIntensity(i + o1, s2[i + o1], ch2);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

    /**
     * performs variable pan on channel1 and channel2, taking into account only selection-range of channel1. value: 1 = channel1
     * 1.5= center 2 = channel2
     */
    public void operate(AChannelSelection channel1, AChannelSelection channel2, AChannelSelection pan) {
        float s1[] = channel1.getChannel().sample;
        float s2[] = channel2.getChannel().sample;
        float p[] = pan.getChannel().sample;
        int o1 = channel1.getOffset();
        int l1 = channel1.getLength();
        float ch1, ch2;

        // mark changed channels...
        channel1.getChannel().changeId();
        channel2.getChannel().changeId();

        try {
            progressSupport.entrySubProgress(new ProgressEvent(this));
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                calculateMatrix(p[o1 + i]);
                ch1 = f11 * s1[o1 + i] + f21 * s2[o1 + i];
                ch2 = f22 * s2[o1 + i] + f12 * s1[o1 + i];
                s1[i + o1] = channel1.mixIntensity(i + o1, s1[i + o1], ch1);
                s2[i + o1] = channel1.mixIntensity(i + o1, s2[i + o1], ch2);
            }
            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }
}
