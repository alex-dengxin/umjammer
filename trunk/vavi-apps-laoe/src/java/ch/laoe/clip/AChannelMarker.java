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

package ch.laoe.clip;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


/**
 * marker of a channel
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 02.07.2002 first draft oli4
 */
public class AChannelMarker extends AObject {
    /**
     * constructor
     */
    public AChannelMarker(AChannel ch) {
        channel = ch;
        markers = new ArrayList<Integer>();
    }

    public AChannelMarker(AChannelMarker m) {
        channel = m.channel;
        markers = new ArrayList<Integer>();
        for (int i = 0; i < m.markers.size(); i++) {
            markers.add(new Integer(m.getMarkerX(i)));
        }
    }

    private List<Integer> markers;

    private AChannel channel;

    public void setChannel(AChannel ch) {
        channel = ch;
    }

    private int searchIndex(int x) {
        // search from left to right...
        for (int i = 0; i < markers.size(); i++) {
            if (markers.get(i) == x) {
                return i;
            }
        }
        return -1;
    }

    private int searchLeftMarkerIndex(int x) {
        // search from right to left...
        for (int i = markers.size() - 1; i >= 0; i--) {
            if (getMarkerX(i) <= x) {
                return i;
            }
        }
        return -1;
    }

    private int searchRightMarkerIndex(int x) {
        // search from left to right...
        for (int i = 0; i < markers.size(); i++) {
            if (getMarkerX(i) > x) {
                return i;
            }
        }
        return -1;
    }

    public int searchLeftMarker(int x) {
        int i = searchLeftMarkerIndex(x);
        if (i >= 0) {
            return getMarkerX(i);
        } else {
            return 0;
        }
    }

    public int searchRightMarker(int x) {
        int i = searchRightMarkerIndex(x);
        if (i >= 0) {
            return getMarkerX(i);
        } else {
            return channel.getSampleLength() - 1;
        }
    }

    public void addMarker(int x) {
        int i = searchRightMarkerIndex(x);
        if (i >= 0) {
            markers.add(i, x);
        } else {
            markers.add(x);
        }
    }

    public void addMarkerFromSelection(AChannelSelection sel) {
        addMarker(sel.getOffset());
        addMarker(sel.getOffset() + sel.getLength());
    }

    public int getMarkerX(int index) {
        return markers.get(index);
    }

    public int getNumberOfMarkers() {
        return markers.size();
    }

    public void removeMarker(int index) {
        if (index >= 0) {
            markers.remove(index);
        }
    }

    public void moveMarker(int index, int x) {
        int minX = 0;
        int maxX = channel.getSampleLength();

        // range limit...
        if (index > 0) {
            minX = Math.max(minX, getMarkerX(index - 1));
        }
        if (index < getNumberOfMarkers() - 1) {
            maxX = Math.min(maxX, getMarkerX(index + 1));
        }

        // move
        x = Math.max(x, minX);
        x = Math.min(x, maxX);
        markers.set(index, new Integer(x));
    }

    public void clear() {
        markers.clear();
    }

    public int searchNearestIndex(int x, int minimumDistance) {
        int min = Integer.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < markers.size(); i++) {
            int d = Math.abs(x - getMarkerX(i));
            if (d < minimumDistance) {
                if (d < min) {
                    min = d;
                    index = i;
                }
            }
        }
        return index;
    }

    private static int nameCounter;

    /**
     * set the default name
     */
    public void setDefaultName() {
        setDefaultName("marker", nameCounter++);
    }

    public void paintOntoClip(Graphics2D g2d, Rectangle rect) {
        AChannelPlotter chp = channel.getChannelPlotter();

        // graphic settings
        g2d.setClip(rect.x, rect.y, rect.width, rect.height);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));

        for (int i = 0; i < markers.size(); i++) {
            Polygon p = new Polygon();
            int x = chp.sampleToGraphX(getMarkerX(i));
            int y = rect.y - 1;
            p.addPoint(x, y);
            p.addPoint(x - rect.height / 2, y + rect.height);
            p.addPoint(x + rect.height / 2, y + rect.height);

            g2d.setColor(Color.gray);
            g2d.fill(p);
            g2d.setColor(Color.black);
            g2d.draw(p);
        }
    }
}
