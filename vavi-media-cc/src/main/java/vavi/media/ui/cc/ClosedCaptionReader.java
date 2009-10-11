/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;


/**
 * Closed Caption Reader SPI.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030218 nsano initial version <br>
 */
public abstract class ClosedCaptionReader {

    protected BufferedReader reader;

    public ClosedCaptionReader(Reader reader) throws IOException {
        this.reader = new BufferedReader(reader);
    }

    public abstract ClosedCaption[] readClosedCaptions() throws IOException;
}

/* */
