/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import vavi.util.Debug;


/**
 * SRT Writer.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070930 nsano initial version <br>
 */
public class SRTWriter extends ClosedCaptionWriter {

    /** */
    private static String encoding = "Windows-31J";

    /** */
    public SRTWriter(File file) throws IOException {
        super(new OutputStreamWriter(new FileOutputStream(file), encoding));
    }

    /** */
    public void writeClosedCaptions(ClosedCaption[] closedCaptions) throws IOException {

        DateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        for (ClosedCaption closedCaption : closedCaptions) {

            writer.write(String.valueOf(closedCaption.getSequenceNo()));
            writer.write("\n");

            writer.write(sdf.format(new Date(closedCaption.getTimeFrom())));
            writer.write(" --> ");
            writer.write(sdf.format(new Date(closedCaption.getTimeTo())));
            writer.write("\n");

            writer.write(String.valueOf(closedCaption.getText()));
            writer.write("\n");

            writer.write("\n");
        }

        writer.flush();
    }

    /** */
    static {

        final Class<?> c = SRTWriter.class;

        try {
            Properties props = new Properties();

            props.load(c.getResourceAsStream("SRT.properties"));

            String value = props.getProperty("writer.encoding");
            if (value != null) {
                encoding = value;
            }
        } catch (IOException e) {
Debug.println(e);
        }
    }
}

/* */
