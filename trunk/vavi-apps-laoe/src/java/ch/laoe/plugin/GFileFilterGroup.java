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

package ch.laoe.plugin;

import java.io.File;


/**
 * parent file-filter
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 11.07.01 first draft oli4
 */
class GFileFilterGroup extends javax.swing.filechooser.FileFilter {
    public GFileFilterGroup(String ext[]) {
        extension = ext;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < extension.length; i++) {
            sb.append(extension[i]);
            sb.append(" ");
        }
        concatenatedExtensions = new String(sb);
    }

    private GFileFilterGroup() {
    }

    protected String extension[];

    protected String concatenatedExtensions;

    public boolean accept(File file) {
        // show directories...
        if (file.isDirectory()) {
            return true;
        }

        // audio files ?
        String fn = file.getName().toLowerCase();
        for (int i = 0; i < extension.length; i++) {
            if (fn.endsWith(extension[i])) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return concatenatedExtensions;
    }
}
