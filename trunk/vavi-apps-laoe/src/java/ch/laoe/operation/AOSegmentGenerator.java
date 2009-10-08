
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * generate linear segments, works without selections!!!
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 26.07.00 erster Entwurf oli4 <br>
 *          03.08.00 neuer Stil oli4 <br>
 *          17.03.01 first use in a plugin oli4 <br>
 *          27.04.01 based on Toolkit oli4 <br>
 *          12.08.01 single-point mode oli4 <br>
 *          01.12.01 add envelope operation oli4 21.01.02 selection-dependent and -independent mode oli4
 * 
 */
public class AOSegmentGenerator extends AOperation {
    /**
     * interpolation order
     */
    public static final int SINGLE_POINTS = -1;

    public static final int ORDER_0 = 0;

    public static final int ORDER_1 = 1;

    public static final int ORDER_2 = 2;

    public static final int ORDER_3 = 3;

    public static final int SPLINE = 4;

    private int order;

    /**
     * operation on samples
     */
    public static final int REPLACE_OPERATION = 0;

    public static final int ENVELOPE_OPERATION = 1;

    private int operation;

    /**
     * x,y: point definitions of the segments order: interpolation order
     */
    public AOSegmentGenerator(float[] x, float[] y, int order, int operation, boolean selectionIndependent) {
        super();
        this.x = x;
        this.y = y;
        this.order = order;
        this.operation = operation;
        this.selectionIndependent = selectionIndependent;
    }

    // segments
    private float x[];

    private float y[];

    private boolean selectionIndependent;

    /**
     * performs the segments generation
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int xStart;
        int xEnd;
        float tmp[];

        if (selectionIndependent) {
            xStart = (int) x[0];
            xEnd = (int) x[x.length - 1];
        } else {
            xStart = ch1.getOffset();
            xEnd = ch1.getLength() + ch1.getOffset();
        }
        tmp = new float[xEnd - xStart];

        // mark changed channels...
        ch1.getChannel().changeId();

        // System.out.println("si="+selectionIndependent+" start="+xStart+" end="+xEnd);

        // copy to tmp
        // System.arrayCopy(s1, xStart, tmp, 0, xEnd-xStart);

        // make segments in tmp... (except of single points)
        switch (order) {
        case SINGLE_POINTS:
            // each sample
            for (int i = 0; i < x.length; i++) {
                if (ch1.getChannel().isValidIndex((int) x[i])) {
                    if (selectionIndependent) {
                        s1[(int) x[i]] = y[i];
                    } else {
                        int ii = (int) x[i];
                        if (ch1.isSelected(ii)) {
                            s1[ii] = ch1.mixIntensity(ii, s1[ii], y[i]);
                        }
                    }
                }
            }
            break;

        case ORDER_0:
            // each sample
            for (int i = xStart; i < xEnd; i++) {
                if (ch1.getChannel().isValidIndex(i)) {
                    tmp[i - xStart] = AOToolkit.interpolate0(x, y, i);
                }
            }
            break;

        case ORDER_1:
            // each sample
            for (int i = xStart; i < xEnd; i++) {
                if (ch1.getChannel().isValidIndex(i)) {
                    tmp[i - xStart] = AOToolkit.interpolate1(x, y, i);
                }
            }
            break;

        case ORDER_2:
            // each sample
            for (int i = xStart; i < xEnd; i++) {
                if (ch1.getChannel().isValidIndex(i)) {
                    tmp[i - xStart] = AOToolkit.interpolate2(x, y, i);
                }
            }
            break;

        case ORDER_3:
            // each sample
            for (int i = xStart; i < xEnd; i++) {
                if (ch1.getChannel().isValidIndex(i)) {
                    tmp[i - xStart] = AOToolkit.interpolate3(x, y, i);
                }
            }
            break;

        case SPLINE:
            AOSpline spline = AOToolkit.createSpline();
            spline.load(x, y);
            // each sample
            for (int i = xStart; i < xEnd; i++) {
                if (ch1.getChannel().isValidIndex(i)) {
                    tmp[i - xStart] = spline.getResult(i);
                }
            }
            break;
        }

        switch (order) {
        case ORDER_0:
        case ORDER_1:
        case ORDER_2:
        case ORDER_3:
        case SPLINE:
            switch (operation) {
            case REPLACE_OPERATION:
                for (int i = 0; i < tmp.length; i++) {
                    if (ch1.getChannel().isValidIndex(i + xStart)) {
                        if (selectionIndependent) {
                            s1[i + xStart] = tmp[i];
                        } else {
                            int ii = i + xStart;
                            s1[ii] = ch1.mixIntensity(ii, s1[ii], tmp[i]);
                        }
                    }
                }
                break;

            case ENVELOPE_OPERATION:
                float factor = AOToolkit.max(s1, xStart, xEnd - xStart);
                for (int i = 0; i < tmp.length; i++) {
                    if (ch1.getChannel().isValidIndex(i + xStart)) {
                        if (selectionIndependent) {
                            s1[i + xStart] = s1[i + xStart] * tmp[i] / factor;
                        } else {
                            int ii = i + xStart;
                            float x = s1[ii] * tmp[i] / factor;
                            s1[ii] = ch1.mixIntensity(ii, s1[ii], x);
                        }
                    }
                }
                break;

            }
            break;
        }

    }
}
