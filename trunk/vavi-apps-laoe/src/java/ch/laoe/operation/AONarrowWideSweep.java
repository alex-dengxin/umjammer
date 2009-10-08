
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * general amplifier
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.4
 * 
 * @version 30.04.01 first draft oli4
 */
public class AONarrowWideSweep extends AONarrowWide {
    public AONarrowWideSweep(boolean modifyCh1, boolean modifyCh2, float wideBegin, float wideEnd, boolean continueBefore, boolean continueAfter) {
        super(modifyCh1, modifyCh2);

        this.wideBegin = wideBegin;
        this.wideEnd = wideEnd;
        this.continueBefore = continueBefore;
        this.continueAfter = continueAfter;
    }

    private boolean continueBefore, continueAfter;

    private float wideBegin, wideEnd;

    /**
     * performs a constant widening
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
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

            // before...
            if (continueBefore) {
                for (int i = 0; i < o1; i++) {
                    float wide = wideBegin;
                    // narrowing to mono ?
                    if (wide < 1) {
                        narrowing(ch1, ch2, i, 1 - wide);
                    }
                    // widening ?
                    else {
                        widening(ch1, ch2, i, wide - 1);
                    }
                }
            }

            // sweep...
            for (int i = 0; i < l1; i++) {
                progressSupport.setProgress(new ProgressEvent(this, (i + 1) * 100 / l1));
                float wide = wideBegin + ((wideEnd - wideBegin) * i / l1);
                // narrowing to mono ?
                if (wide < 1) {
                    narrowing(ch1, ch2, o1 + i, 1 - wide);
                }
                // widening ?
                else {
                    widening(ch1, ch2, o1 + i, wide - 1);
                }
            }

            // after...
            if (continueAfter) {
                int length = ch1.getChannel().sample.length;
                for (int i = o1 + l1; i < length; i++) {
                    float wide = wideEnd;
                    // narrowing to mono ?
                    if (wide < 1) {
                        narrowing(ch1, ch2, i, 1 - wide);
                    }
                    // widening ?
                    else {
                        widening(ch1, ch2, i, wide - 1);
                    }
                }
            }

            progressSupport.exitSubProgress(new ProgressEvent(this));
        } catch (ArrayIndexOutOfBoundsException oob) {
        }
    }

}
