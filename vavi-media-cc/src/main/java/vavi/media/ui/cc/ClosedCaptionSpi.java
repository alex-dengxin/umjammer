/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Properties;

import vavi.util.Debug;


/**
 * Closed Caption Loader の Service Provider Interface です．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030228 nsano initial version <br>
 */
public interface ClosedCaptionSpi {

    /** ロードできるかどうか調べます． */
    boolean canReadInput(File file) throws IOException;

    /** リーダのインスタンスを作成します。 */
    ClosedCaptionReader createReaderInstance(File file) throws IOException;

    /** */
    boolean canWriteType(String type);

    /** リーダのインスタンスを作成します。 */
    ClosedCaptionWriter createWriterInstance(File file) throws IOException;

    /** */
    class Factory {

        /** */
        public static ClosedCaptionReader getReader(File file) throws IOException {
            for (int i = 0; i < closedCaptionSpis.length; i++) {
Debug.println("closedCaptionSpi: " + closedCaptionSpis[i]);
                if (closedCaptionSpis[i].canReadInput(file)) {
                    ClosedCaptionReader reader = closedCaptionSpis[i].createReaderInstance(file);
Debug.println("reader: " + reader.getClass());
                    return reader;
                }
            }

            throw new NoSuchElementException(file + " is not supported type");
        }

        /** */
        public static ClosedCaptionWriter getWriter(File file, String type) throws IOException {
            for (int i = 0; i < closedCaptionSpis.length; i++) {
Debug.println("closedCaptionSpi: " + closedCaptionSpis[i]);
                if (closedCaptionSpis[i].canWriteType(type)) {
                    ClosedCaptionWriter writer = closedCaptionSpis[i].createWriterInstance(file);
Debug.println("writer: " + writer.getClass());
                    return writer;
                }
            }

            throw new NoSuchElementException(file + " is not supported type");
        }

        /** */
        private static ClosedCaptionSpi[] closedCaptionSpis;

        /** */
        static {
            final String path = "/META-INF/services/ClosedCaptionSpi";
            final Class<?> clazz = Factory.class;

            try {
                Properties props = new Properties();
                props.load(clazz.getResourceAsStream(path));
    props.list(System.err);
                closedCaptionSpis = new ClosedCaptionSpi[props.size()];
                Enumeration<?> e = props.propertyNames();
                int i = 0;
                while (e.hasMoreElements()) {
                    String className = (String) e.nextElement();
                    closedCaptionSpis[i++] = (ClosedCaptionSpi) Class.forName(className).newInstance();
                }
            } catch (Exception e) {
    Debug.printStackTrace(e);
                System.exit(1);
            }
        }
    }
}

/* */
