
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * reverse or mirrors.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.10.00 erster Entwurf oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          26.01.01 array-based again... oli4 <br>
 *          01.12.01 change classname from AOMirror to AOReverse oli4
 * 
 */
public class AOReverse extends AOperation {
    public AOReverse(int mode) {
        super();
        this.mode = mode;
    }

    private int mode;

    public static final int MIRROR_LEFT_SIDE = 1;

    public static final int MIRROR_RIGHT_SIDE = 2;

    public static final int REVERSE = 3;

    public void operate(AChannelSelection ch1) {
        // mark changed channels...
        ch1.getChannel().changeId();

        switch (mode) {
        case MIRROR_LEFT_SIDE:
            mirrorLeft(ch1);
            break;

        case MIRROR_RIGHT_SIDE:
            mirrorRight(ch1);
            break;

        case REVERSE:
            reverse(ch1);
            break;
        }
    }

    private void mirrorLeft(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // new track
        float tmp[] = new float[s1.length + l1];

        try {
            // copy left
            for (int i = 0; i < o1; i++)
                tmp[i] = s1[i];

            // build mirror on the left
            for (int i = 0; i < l1; i++)
                tmp[i + o1] = s1[o1 + l1 - i - 1];

            // copy selection and right
            for (int i = o1; i < s1.length; i++)
                tmp[i + l1] = s1[i];
        } catch (ArrayIndexOutOfBoundsException oob) {
        }

        // replace original
        ch1.getChannel().sample = tmp;
    }

    private void mirrorRight(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // new track size
        float tmp[] = new float[s1.length + l1];

        try {
            // copy left
            for (int i = 0; i < o1 + l1; i++)
                tmp[i] = s1[i];

            // build selection and mirror on the right
            for (int i = 0; i < l1; i++)
                tmp[i + o1 + l1 - 1] = s1[o1 + l1 - i - 1];

            // copy right
            for (int i = o1 + l1; i < s1.length; i++)
                tmp[i + l1] = s1[i];
        } catch (ArrayIndexOutOfBoundsException oob) {
        }

        // replace original
        ch1.getChannel().sample = tmp;
    }

    private void reverse(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float tmp[] = new float[l1];

        try {
            // copy selection
            for (int i = 0; i < l1; i++)
                tmp[i] = s1[i + o1];

            // build mirror replacing selection
            for (int i = 0; i < l1; i++)
                s1[i + o1] = tmp[l1 - i - 1];
        } catch (ArrayIndexOutOfBoundsException oob) {
        }

    }

}
