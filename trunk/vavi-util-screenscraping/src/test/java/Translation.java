/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;

import vavi.media.ui.cc.ClosedCaption;
import vavi.media.ui.cc.ClosedCaptionReader;
import vavi.media.ui.cc.ClosedCaptionSpi;
import vavi.media.ui.cc.ClosedCaptionWriter;
import vavi.media.ui.cc.SRTSpi;
import vavix.util.translation.Translator;


/**
 * Translation. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070930 nsano initial version <br>
 */
public class Translation {

    /**
     * 
     * @param args 0: language, 1: input, 2: output, 3: sequence no, 4: interval [msec]
     */
    public static void main(String[] args) throws Exception {
        String languageMode = args[0];
        File input = new File(args[1]);
        File output = new File(args[2]);
        int skip = Integer.parseInt(args[3]);
        int interval = Integer.parseInt(args[4]);
        boolean useProxy = Boolean.parseBoolean(args[5]);

        int errorCount = 0;
        final int maxErrorCount = 5;

        Translator translator = new WebserviceXTranslator(languageMode, useProxy);
//        Translator translator = new GoogleTranslator();

        ClosedCaptionReader reader = ClosedCaptionSpi.Factory.getReader(input);
        ClosedCaption[] captions = reader.readClosedCaptions();

        for (ClosedCaption caption : captions) {
            if (caption.getSequenceNo() < skip) {
                continue;
            }

            String text = caption.getText();
System.err.println("I: " + text);
            String translated = null;
            try {
                translated = translator.toLocal(text);
System.err.println("O: " + translated);
                errorCount = 0;
            } catch (Exception e) {
e.printStackTrace(System.err);
                translated = e.getMessage();
System.err.println("X: " + translated);
                errorCount++;
                if (errorCount > maxErrorCount) {
System.err.println("Maybe blocked, stop");
                    break;
                }
            }
            caption.setText(translated);
            Thread.sleep(interval);
        }

        ClosedCaptionWriter writer = ClosedCaptionSpi.Factory.getWriter(output, SRTSpi.TYPE);
        writer.writeClosedCaptions(captions);
    }
}

/* */
