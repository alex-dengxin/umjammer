
package ch.laoe.audio;

/**
 * common audio exceptions
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 08.07.01 first draft oli4
 */
public class AudioException extends Exception {
    private AudioException() {
    }

    public AudioException(String s) {
        super(s);
    }
}
