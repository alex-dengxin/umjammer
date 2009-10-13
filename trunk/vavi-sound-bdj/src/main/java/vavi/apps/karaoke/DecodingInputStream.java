/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * DecodingInputStream. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 081014 nsano initial version <br>
 */
public abstract class DecodingInputStream extends FilterInputStream {

    public DecodingInputStream(InputStream in) {
        super(in);
    }

    /**
     * @param b (output) PCM 16bit signed big endian stereo
     * @return read bytes
     */
    public abstract int decode(byte[] b) throws IOException;
}

/* */
