/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

package vavi.util.wavelet;


/**
 * Wavelet. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080516 nsano initial version <br>
 */
public interface Wavelet {

    /** */
    void doFWT(int data[], int depth);

    /** */
    void doIFWT(int data[], int depth);

    /** */
    void doFWT_even(int data[], int skip);

    /** */
    void doFWT_odd(int data[], int skip);

    /** */
    void doIWT_even(int data[], int skip);

    /** */
    void doIWT_odd(int data[], int skip);
}

/* */
