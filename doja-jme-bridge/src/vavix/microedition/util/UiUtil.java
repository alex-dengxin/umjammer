/*
 * Copyright (c) 2004 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.microedition.util;

import java.io.DataOutputStream;

import javax.microedition.io.Connector;

import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;


/**
 * StringUtil.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040208 nsano initial version <br>
 */
public final class UiUtil {

    /**
     * byte ‚©‚ç‰æ‘œ‚ğæ“¾‚µ‚Ü‚·B
     */
    public static MediaImage getMediaImage(byte[] data, String url) {
        MediaImage mediaImage = null;

        try {
            DataOutputStream dos = Connector.openDataOutputStream(url);
            dos.write(data);
            dos.close();
            dos = null;

            mediaImage = MediaManager.getImage(url);
        } catch (Exception e) {
            mediaImage = null;
        }

        return mediaImage;
    }
}

/* */
