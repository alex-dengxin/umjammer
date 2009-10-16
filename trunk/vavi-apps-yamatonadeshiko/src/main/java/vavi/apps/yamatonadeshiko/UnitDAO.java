/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 * Copyright (c) 2009 by KLab Inc., All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko;

import java.io.IOException;


/**
 * UnitDAO1. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @author <a href="mailto:sano-n@klab.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/04/19 nsano initial version <br>
 */
public interface UnitDAO {

    /** ファイルの値を読み込みます。 */
    public abstract String load() throws IOException;

    /**
     * ファイルに値を書き込みます。
     * @param unit use ASCII
     */
    public abstract void save(String unit) throws IOException;

}
/* */
