/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * Closed Caption Writer SPI.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070930 nsano initial version <br>
 */
public abstract class ClosedCaptionWriter {

    protected BufferedWriter writer;

    public ClosedCaptionWriter(Writer writer) throws IOException {
        this.writer = new BufferedWriter(writer);
    }

    public abstract void writeClosedCaptions(ClosedCaption[] closedCaptions) throws IOException;
}

/* */
