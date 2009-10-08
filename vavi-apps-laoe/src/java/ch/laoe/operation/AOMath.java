
package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


/**
 * divers math operations on samples
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 11.06.01 first draft oli4 <br>
 *          20.08.01 add two-channel-operations oli4
 */
public class AOMath extends AOperation {
    public AOMath(int operation, float[] operands) {
        this(operation);
        this.operands = operands;
    }

    public AOMath(int operation) {
        this.operation = operation;
    }

    private int operation;

    public static final int ADD = 1;

    public static final int SUBTRACT = 2;

    public static final int MULTIPLY = 3;

    public static final int DIVIDE = 4;

    public static final int INVERS = 5;

    public static final int NEG = 6;

    public static final int POW = 7;

    public static final int SQRT = 8;

    public static final int DERIVATE = 9;

    public static final int INTEGRATE = 10;

    public static final int EXP = 11;

    public static final int LOG = 12;

    public static final int TO_dB = 13;

    public static final int FROM_dB = 14;

    public static final int MEAN = 15;

    public static final int RMS = 16;

    private float operands[];

    /**
     * performs one-sample-operations
     */
    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        switch (operation) {
        case ADD:
            AOToolkit.add(s1, o1, l1, operands[0]);
            break;

        case SUBTRACT:
            AOToolkit.subtract(s1, o1, l1, operands[0]);
            break;

        case MULTIPLY:
            AOToolkit.multiply(s1, o1, l1, operands[0]);
            break;

        case DIVIDE:
            AOToolkit.divide(s1, o1, l1, operands[0]);
            break;

        case DERIVATE:
            AOToolkit.derivate(s1, o1, l1);
            break;

        case INTEGRATE:
            AOToolkit.integrate(s1, o1, l1);
            break;

        case INVERS:
            AOToolkit.invers(s1, o1, l1);
            break;

        case NEG:
            AOToolkit.neg(s1, o1, l1);
            break;

        case POW:
            AOToolkit.pow(s1, o1, l1, operands[0]);
            break;

        case SQRT:
            AOToolkit.sqrt(s1, o1, l1);
            break;

        case EXP:
            AOToolkit.exp(s1, o1, l1);
            break;

        case LOG:
            AOToolkit.log(s1, o1, l1);
            break;

        case TO_dB:
            AOToolkit.todB(s1, o1, l1);
            break;

        case FROM_dB:
            AOToolkit.fromdB(s1, o1, l1);
            break;

        case MEAN:
            AOToolkit.smooth(s1, o1, l1, (int) operands[0]);
            break;

        case RMS:
            AOToolkit.smoothRms(s1, o1, l1, (int) operands[0]);
            break;

        }

    }

    /**
     * performs two-channel-operations
     */
    public void operate(AChannelSelection ch1, AChannelSelection ch2) {
        float s1[] = ch1.getChannel().sample;
        float s2[] = ch2.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        switch (operation) {
        case ADD:
            AOToolkit.add(s1, s2, o1, l1);
            break;

        case SUBTRACT:
            AOToolkit.subtract(s1, s2, o1, l1);
            break;

        case MULTIPLY:
            AOToolkit.multiply(s1, s2, o1, l1);
            break;

        case DIVIDE:
            AOToolkit.divide(s1, s2, o1, l1);
            break;
        }

    }

}
