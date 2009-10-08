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
 * Class: AOFade @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * fade in / out
 * 
 * @version 26.04.01 first draft oli4 29.12.2001 add fill-zeroes option oli4 10.07.2002 selectable
 * low-factor (not just zero) oli4
 */
public class AOFade extends AOperation {
    /**
     * @param mode fade-mode, IN, OUT, CROSS
     * @param order order of shape of ramp
     * @param fillZeroes set all samples to zero on low-side of fade-selection
     */
    public AOFade(int mode, int order, float lowFactor, boolean continueLow) {
        super();
        this.mode = mode;
        this.order = order;
        this.lowFactor = lowFactor;
        this.variableFactor = 1 - lowFactor;
        this.continueLow = continueLow;
    }

    private int mode;

    public static final int IN = 1;

    public static final int OUT = 2;

    public static final int CROSS = 3;

    private int order;

    public static final int SQUARE_ROOT = -2;

    public static final int LINEAR = 1;

    public static final int SQUARE = 2;

    public static final int CUBIC = 3;

    private boolean continueLow;

    private float lowFactor, variableFactor;

    private float performOrder(float a) {
        switch (order) {
        case LINEAR:
            return a;

        case SQUARE:
            return a * a;

        case CUBIC:
            return a * a * a;

        case SQUARE_ROOT:
            return (float) Math.pow(a, 0.5);

        default:
            return a;
        }
    }

    /**
     * performs a constant amplification
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        float a;

        switch (mode) {
        // fade in
        case IN:
            try {
                // fade...
                progressSupport.entrySubProgress(new ProgressEvent(this));
                for (int i = 0; i < l1; i++) {
                    progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                    a = (float) i / (float) l1;
                    a = performOrder(a);
                    s1[o1 + i] *= a * variableFactor + lowFactor;
                }
                progressSupport.exitSubProgress(new ProgressEvent(this));

                // continue low before...
                if (continueLow) {
                    for (int i = 0; i < o1; i++) {
                        s1[i] *= lowFactor;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException oob) {
            }
            break;

        // fade out
        case OUT:
            try {
                // fade...
                progressSupport.entrySubProgress(new ProgressEvent(this));
                for (int i = 0; i < l1; i++) {
                    progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                    a = (float) (l1 - i) / (float) l1;
                    a = performOrder(a);
                    s1[o1 + i] *= a * variableFactor + lowFactor;
                }
                progressSupport.exitSubProgress(new ProgressEvent(this));

                // continue low after...
                if (continueLow) {
                    for (int i = o1 + l1; i < s1.length; i++) {
                        s1[i] *= lowFactor;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException oob) {
            }
            break;

        // fade cross
        case CROSS:
            int lh = l1 / 2;
            float b;
            float tmp[] = new float[s1.length - lh];
            try {

                // cross...
                progressSupport.entrySubProgress(new ProgressEvent(this));
                for (int i = 0; i < lh; i++) {
                    progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / lh));
                    a = (float) i / (float) lh;
                    a = performOrder(a);
                    b = (float) (lh - i) / (float) lh;
                    b = performOrder(b);
                    s1[o1 + i] = b * s1[o1 + i] + a * s1[o1 + lh + i];
                }
                progressSupport.exitSubProgress(new ProgressEvent(this));

                // cut half of selection...
                for (int i = 0; i < o1 + lh; i++) {
                    tmp[i] = s1[i];
                }
                for (int i = o1 + l1; i < s1.length; i++) {
                    tmp[i - lh] = s1[i];
                }
            } catch (ArrayIndexOutOfBoundsException oob) {
            }

            // replace original
            ch1.getChannel().sample = tmp;
            break;
        }
    }
}
