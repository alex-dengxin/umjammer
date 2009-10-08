/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.ui;

import java.net.URL;

import javax.swing.ImageIcon;

import ch.laoe.operation.ProgressEvent;
import ch.laoe.operation.ProgressListener;
import ch.oli4.ui.UiProgressViewer;


/**
 * shows progress of a long-time operation.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 28.11.01 adapted to LAoE oli4
 */
public class GProgressViewer extends UiProgressViewer {
    protected GProgressViewer() {
        super();

        // frame
        URL u = getClass().getResource("resources/laoe.gif");
        if (u != null) {
            setIconImage(new ImageIcon(u).getImage());
        }
    }

    /**
     * start a progress visualisation with the given frame-title, with a progress-value of zero.
     */
    public static void start(String title) {
        UiProgressViewer.start(GLanguage.translate(title));
    }

    public static void entrySubProgress(String note) {
        UiProgressViewer.entrySubProgress(GLanguage.translate(note));
    }

    /**
     * only note1 is translated, note2 is simply appended
     */
    public static void entrySubProgress(String note1, String note2) {
        UiProgressViewer.entrySubProgress(GLanguage.translate(note1) + note2);
    }

    public static void setNote(String note) {
        UiProgressViewer.setNote(GLanguage.translate(note));
    }

    // operations
    public static ProgressListener operationProgressListener = new ProgressListener() {
        public void entrySubProgress(ProgressEvent ev) {
            GProgressViewer.entrySubProgress();
        }
        public void setProgress(ProgressEvent ev) {
            GProgressViewer.setProgress((Integer) ev.getArguments()[0]);
        }
        public void exitSubProgress(ProgressEvent ev) {
            GProgressViewer.exitSubProgress();
        }
    };
}

/* */
