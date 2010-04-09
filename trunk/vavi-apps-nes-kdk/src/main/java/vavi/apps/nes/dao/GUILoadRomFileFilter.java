/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.dao;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Class for the NES ROM File Filter required by the GUI.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUILoadRomFileFilter implements FilenameFilter {
    /**
     * Whether the given file is accepted by this filter.
     */
    public final boolean accept(File dirname, String filename) {
        // Accept Files with NES Extension
        if (filename.toString().toUpperCase().endsWith(".NES"))
            return true;
        // Accept Files with NES.GZ Extension
        if (filename.toString().toUpperCase().endsWith(".NES.GZ"))
            return true;
        // Accept Files with ZIP Extension
        if (filename.toString().toUpperCase().endsWith(".ZIP"))
            return true;
        // Don't Accept anything Else
        return false;
    }
}
