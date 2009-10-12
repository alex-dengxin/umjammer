/*
 * Wavelet Audio Compression
 * 
 * http://www.toblave.org/soundcompression/
 */

package vavi.util.wavelet;


/**
 * FourTwoWavelet. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080516 nsano initial version <br>
 */
public class FourTwoWavelet implements Wavelet {

    /* */
    public void doFWT(int data[], int depth) {
        for (int skip = 2; skip <= (2 << (depth - 1)); skip <<= 1) {
            doFWT_odd(data, skip);
            doFWT_even(data, skip);
        }
    }

    /* */
    public void doIFWT(int data[], int depth) {
        for (int skip = (2 << (depth - 1)); skip >= 2; skip >>= 1) {
            doIWT_even(data, skip);
            doIWT_odd(data, skip);
        }
    }

    /* (4,2) Wavelet from Calderbank, Daubechies, Sweldens, and Yeo */
    public void doFWT_even(int data[], int skip) {
        for (int el = skip * 2; el < data.length - skip * 2; el += skip) {
            data[el] += (data[el - skip / 2] + data[el + skip / 2] + 2) / 4;
        }
    }

    /* */
    public void doFWT_odd(int data[], int skip) {
        for (int el = skip / 2 * 5; el < data.length - skip * 3 / 2; el += skip) {
            data[el] -= (9 * (data[el - skip / 2] + data[el + skip / 2]) - (data[el - skip / 2 * 3] + data[el + skip / 2 * 3]) + 8) / 16;
        }
    }

    /* */
    public void doIWT_even(int data[], int skip) {
        for (int el = skip * 2; el < data.length - skip * 2; el += skip) {
            data[el] -= (data[el - skip / 2] + data[el + skip / 2] + 2) / 4;
        }
    }

    /* */
    public void doIWT_odd(int data[], int skip) {
        for (int el = skip / 2 * 5; el < data.length - skip * 3 / 2; el += skip) {
            data[el] += (9 * (data[el - skip / 2] + data[el + skip / 2]) - (data[el - skip / 2 * 3] + data[el + skip / 2 * 3]) + 8) / 16;
        }
    }
}

/* */
