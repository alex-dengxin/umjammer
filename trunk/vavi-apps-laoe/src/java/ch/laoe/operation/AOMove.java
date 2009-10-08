
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * move a selection to another place
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 13.04.01 first draft oli4
 */
public class AOMove extends AOperation {
    public AOMove(int newIndex) {
        super();
        this.newIndex = newIndex;
    }

    private int newIndex;

    /**
     * performs the move
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        float tmp[];
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // not really moved ?
        if ((newIndex >= o1) && (newIndex < o1 + l1)) {
            return;
        }

        // range check...
        if (newIndex < 0) {
            newIndex = 0;
        } else if (newIndex >= s1.length) {
            newIndex = s1.length - l1 - 1;
        } else if (newIndex > o1 + l1) {
            newIndex -= l1;
        }

        // perform move...
        tmp = new float[s1.length];

        try {
            // move selection to the right place
            for (int i = 0; i < l1; i++) {
                tmp[i + newIndex] = s1[i + o1];
            }

            // remove original selected
            for (int i = o1; i < s1.length - l1; i++) {
                s1[i] = s1[i + l1];
            }

            // zero cross
            AOToolkit.applyZeroCross(s1, o1);

            // copy before
            for (int i = 0; i < newIndex; i++) {
                tmp[i] = s1[i];
            }

            // copy after
            for (int i = newIndex + l1; i < tmp.length; i++) {
                tmp[i] = s1[i - l1];
            }

            // zero cross
            AOToolkit.applyZeroCross(tmp, newIndex);
            AOToolkit.applyZeroCross(tmp, newIndex + o1);
        } catch (ArrayIndexOutOfBoundsException aoobe) {
        }
        // replace original
        ch1.getChannel().sample = tmp;
    }

}
