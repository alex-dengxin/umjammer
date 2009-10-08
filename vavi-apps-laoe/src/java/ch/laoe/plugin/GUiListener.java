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

import java.awt.Graphics2D;
import java.awt.Rectangle;


/**
 * GUiListener
 * 
 * defines what the environnement has to know about the plugin.
 * 
 * @target JDK 1.3
 * @author olivier gäumann, neuchâtel (switzerland)
 * @version 30.09.00 erster Entwurf oli4
 */
public interface GUiListener {
    /**
     * the plugin can draw onto the clip-editor-window (e.g. bezier curves when generating data.
     */
    public void paintOntoClip(Graphics2D g2d, Rectangle rect);

    /**
     * when another clipeditor is focussed, the environnement calls the plugin
     */
    public void reloadAllPluginsAndFocussedClip();
}
