/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;


/**
 * NESCafe Exception
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class NESCafeException extends Exception {
    /**
     * Message that is associated with the current Exception
     */
    private String message;

    /**
     * Constructs a new InvalidOrUnsupportedROM Exception with the specified
     * message.
     * 
     * @param theMessage The Message to attach to the Exception.
     */
    public NESCafeException(String theMessage) {
        message = theMessage;
    }

    /**
     * Returns a String Representation of the Exception.
     * 
     * @return String representation of the Exception.
     */
    public final String toString() {
        return "NESCafeException:[" + message + "]";
    }

    /**
     * Return the Message associated with the Exception.
     * 
     * @return Message associated with the Exception.
     */
    public final String getMessage() {
        return message;
    }
}
