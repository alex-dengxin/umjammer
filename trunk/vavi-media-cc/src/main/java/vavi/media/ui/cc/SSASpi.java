/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.media.ui.cc;

import java.io.File;
import java.io.IOException;


/**
 * SubStation Alhpa (Advanced SubStation (*.ass)) Service Provider です．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030228 nsano initial version <br>
 */
public class SSASpi implements ClosedCaptionSpi {

    /** */
    public static final String TYPE = "SSA";

    /** ロードできるかどうか調べます． */
    public boolean canReadInput(File file) throws IOException {
        return file.getName().endsWith(".ssa");
    }

    /** ローダのインスタンスを作成します。 */
    public ClosedCaptionReader createReaderInstance(File file) throws IOException {
        return new SSAReader(file);
    }

    /* */
    public boolean canWriteType(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

    /* */
    public ClosedCaptionWriter createWriterInstance(File file) throws IOException {
        throw new UnsupportedOperationException("not implemented yet.");
    }
}

/* */
