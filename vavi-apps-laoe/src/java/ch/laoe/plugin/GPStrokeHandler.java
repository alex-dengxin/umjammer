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

import java.util.HashMap;
import java.util.Map;

import ch.laoe.ui.Debug;
import ch.oli4.ui.UiStrokeEvent;
import ch.oli4.ui.UiStrokeListener;


/**
 * Class: GPStrokeHandler @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * stroke handler.
 * 
 * @version 17.06.01 first draft oli4
 */
public class GPStrokeHandler implements UiStrokeListener {
    public GPStrokeHandler() {
        connection = new HashMap<Integer, GPlugin>();
    }

    private Map<Integer, GPlugin> connection;

    /**
     * connects a sroke to a plugin.
     */
    public void add(GPlugin p, int strokeSymbol) {
        connection.put(strokeSymbol, p);
    }

    // stroke event

    public void onStroke(UiStrokeEvent e) {
        GPlugin pl = null;
        pl = connection.get(e.getStroke());
        if (pl != null) {
            Debug.println(5, "stroke recognized: plugin " + pl.getName() + " started");
            pl.start();
        } else {
            Debug.println(5, "stroke NOT recognized");
        }
    }

}
