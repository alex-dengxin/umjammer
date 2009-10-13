package vavi.microedition.midlet;


/**
 * Signals that a requested <code>MIDlet</code> state change failed. This
 * exception is thrown by the <code>MIDlet</code> in response to
 * state change calls
 * into the application via the <code>MIDlet</code> interface
 *
 * @see MIDlet
 * @since MIDP 1.0
 */
public class MIDletStateChangeException extends Exception {

    /**
     * Constructs an exception with no specified detail message.
     */
    public MIDletStateChangeException() {}

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param s the detail message
     */
    public MIDletStateChangeException(String s) {
	super(s);
    }
}

/* */
