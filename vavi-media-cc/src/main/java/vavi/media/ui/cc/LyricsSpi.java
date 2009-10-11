/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.File;
import java.io.IOException;


/**
 * Lyrics Service Provider です．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030228 nsano initial version <br>
 */
public class LyricsSpi implements ClosedCaptionSpi {

    /** */
    public static final String TYPE = "lyrics";

    /** ロードできるかどうか調べます． */
    public boolean canReadInput(File file) throws IOException {
        return file.getName().endsWith(".txt");
    }

    /** ローダのインスタンスを作成します。 */
    public ClosedCaptionReader createReaderInstance(File file) throws IOException {
        return new LyricsReader(file);
    }

    /* */
    public boolean canWriteType(String type) {
        throw new UnsupportedOperationException("not implemented yet.");
    }

    /* */
    public ClosedCaptionWriter createWriterInstance(File file) throws IOException {
        throw new UnsupportedOperationException("not implemented yet.");
    }
}

/* */
