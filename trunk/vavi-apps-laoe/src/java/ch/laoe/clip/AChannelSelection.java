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

import java.util.ArrayList;
import java.util.List;

import ch.laoe.operation.AOToolkit;
import ch.laoe.operation.AOperation;
import ch.laoe.ui.GProgressViewer;


/**
 * a selection defines a continuous set of samples inside a channel.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 25.07.00 erster Entwurf oli4 <br>
 *          03.08.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          16.05.01 add start/endOperation oli4 <br>
 *          11.01.02 introduce intensity-points oli4 <br>
 */
public class AChannelSelection extends ASelection {
    /**
     * constructor
     */
    public AChannelSelection(AChannel ch) {
        super(ch);
        plotter = new ASelectionPlotter(this);
        initIntensityPoints();
    }

    /**
     * null-constructor
     */
    public AChannelSelection() {
        super(null);
        plotter = new ASelectionPlotter(this);
        initIntensityPoints();
        length = 0;
    }

    /**
     * easy constructor
     */
    public AChannelSelection(AChannel ch, int offset, int length) {
        this(ch);
        setOffset(offset);
        setLength(length);
    }

    /**
     * copy-constructor
     */
    public AChannelSelection(AChannelSelection s) {
        this((AChannel) s.model);
        this.name = s.name;
        this.offset = s.offset;
        this.length = s.length;
        this.plotter = s.plotter;

        intensityPoints = new ArrayList<Point>();
        for (int i = 0; i < s.getIntensityPoints().size(); i++) {
            intensityPoints.add(new Point(s.getIntensityPoints().get(i)));
        }
        intensityUsed = s.intensityUsed;
        intensityScale = AChannelSelection.intensityScale;
    }

    public AChannel getChannel() {
        return (AChannel) model;
    }

    public void setChannel(AChannel ch) {
        model = ch;
    }

    private static int nameCounter;

    /**
     * set the default name of the layer
     */
    public void setDefaultName() {
        setDefaultName("channelSelection", nameCounter++);
    }

    private ASelectionPlotter plotter;

    public ASelectionPlotter getPlotter() {
        return plotter;
    }

    // offset
    private int offset;

    /**
     * set offset
     */
    public void setOffset(int o) {
        // range check
        if (o > getChannel().sample.length)
            offset = getChannel().sample.length;
        else if (o < 0)
            offset = 0;
        else
            offset = o;
    }

    /**
     * get offset
     */
    public int getOffset() {
        return offset;
    }

    // length
    private int length;

    /**
     * set length
     */
    public void setLength(int l) {
        // range check
        if (l > getChannel().sample.length - offset)
            length = getChannel().sample.length - offset;
        else if (l < 0)
            length = 0;
        else
            length = l;
    }

    /**
     * get length
     */
    public int getLength() {
        return length;
    }

    /**
     * returns true if x is inside the selectioned indexes
     */
    public boolean isSelected(int x) {
        return (x >= offset) && (x < (offset + length));
    }

    /**
     * returns true if anything is selected
     */
    public boolean isSelected() {
        return length > 0;
    }

    // selection intensity

    private List<Point> intensityPoints;

    private boolean intensityUsed = false;

    /**
     * returns the arraylist of all intensity-points of this selection
     */
    public List<Point> getIntensityPoints() {
        intensityChanged = true; // probable, not for sure...
        return intensityPoints;
    }

    /**
     * returns the index of the next left intensity-point from x, where x is the normalized horizontal position, in the range of 0
     * to 1.
     */
    public int searchLeftIntensityPointIndex(float x) {
        for (int i = 0; i < intensityPoints.size(); i++) {
            if (x < intensityPoints.get(i).x) {
                return i;
            }
        }
        return 0;
    }

    /**
     * returns the index of the nearest intensity-point from x, where x is the normalized horizontal position, in the range of 0
     * to 1.
     */
    public int searchNearestIntensityPointIndex(float x) {
        float d = Float.MAX_VALUE;
        int nearestIndex = 0;

        for (int i = 0; i < intensityPoints.size(); i++) {
            float newD = Math.abs(x - intensityPoints.get(i).x);
            if (newD < d) {
                d = newD;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    /**
     * add a new intensity-point at the right place in the arraylist if x corresponds to an existing point, then just change its
     * y-value
     */
    public void addIntensityPoint(float x, float y) {
        // x exists ?
        for (int i = 0; i < intensityPoints.size(); i++) {
            if (x == intensityPoints.get(i).x) {
                modifyIntensityPoint(i, x, y);
                return;
            }
        }

        // new point...
        if ((x > 0) && (x < 1)) {
            int i = searchLeftIntensityPointIndex(x);
            // System.out.println("add point i="+i+"x="+x+" y="+y);
            // add only middle points...
            if ((i > 0) && (i < (intensityPoints.size()))) {
                intensityPoints.add(i, new AChannelSelection.Point(x, y));
                intensityChanged = true;
                intensityUsed = true;
            }
        }
    }

    /**
     * modify an existing intensity-point
     */
    public void modifyIntensityPoint(int index, float x, float y) {
        Point p = intensityPoints.get(index);
        p.y = y;
        // x-modify only middle points...
        if ((index > 0) && (index < (intensityPoints.size() - 1))) {
            // range limited by left/right neighbour point...
            float xLeft = intensityPoints.get(index - 1).x;
            float xRight = intensityPoints.get(index + 1).x;
            if (x < xLeft)
                p.x = xLeft + .001f;
            else if (x > xRight)
                p.x = xRight - .001f;
            else
                p.x = x;
        }
        intensityChanged = true;
        intensityUsed = true;
    }

    /**
     * remove a intensity-point from the arraylist
     */
    public void removeIntensityPoint(float x) {
        int i = searchNearestIntensityPointIndex(x);
        // System.out.println("remove point i="+i+"x="+x);

        // modify only middle points...
        if ((i > 0) && (i < (intensityPoints.size() - 1))) {
            intensityPoints.remove(i);
            intensityChanged = true;
            intensityUsed = true;
        }
    }

    /**
     * clear the intensity-points to default value (all 1)
     */
    public void clearIntensity() {
        intensityPoints.clear();
        intensityPoints.add(new AChannelSelection.Point(0.f, 1.f));
        intensityPoints.add(new AChannelSelection.Point(1.f, 1.f));
        intensityChanged = true;
        intensityUsed = false;
    }

    private int activeIntensityPointIndex = -1;

    public void setActiveIntensityPoint(float x) {
        if ((x > 0) && (x < 1)) {
            activeIntensityPointIndex = searchNearestIntensityPointIndex(x);
        } else {
            activeIntensityPointIndex = -1; // no active point...
        }
    }

    public int getActiveIntensityPointIndex() {
        return activeIntensityPointIndex;
    }

    public static final int LINEAR_INTENSITY_SCALE = 1;

    public static final int SQUARE_INTENSITY_SCALE = 2;

    public static final int CUBIC_INTENSITY_SCALE = 3;

    public static final int SQUARE_ROOT_INTENSITY_SCALE = -1;

    private static int intensityScale = LINEAR_INTENSITY_SCALE;

    /**
     * scale
     */
    public static void setIntensityScale(int s) {
        intensityScale = s;
    }

    private float px[], py[];

    private boolean intensityChanged;

    /**
     * returns the (interpolated) intensity at the given channel-index x
     */
    public float getIntensity(int x) {
        // optimize speed...
        if (!intensityUsed) {
            return 1.f;
        }

        // memory-saving...
        if (intensityChanged) {
            px = new float[intensityPoints.size()];
            py = new float[intensityPoints.size()];

            for (int i = 0; i < intensityPoints.size(); i++) {
                px[i] = intensityPoints.get(i).x;
                py[i] = intensityPoints.get(i).y;
            }
            intensityChanged = false;
        }

        // calculate intensity...
        float i = AOToolkit.interpolate1(px, py, (((float) x) - getOffset()) / getLength());
        switch (intensityScale) {
        case SQUARE_INTENSITY_SCALE:
            return i * i;

        case CUBIC_INTENSITY_SCALE:
            return i * i * i;

        case SQUARE_ROOT_INTENSITY_SCALE:
            return (float) Math.sqrt(i);

        default:
            return i;
        }
    }

    /**
     * mixes directly original and modified sample in function of the current intensity-value.
     */
    public float mixIntensity(int index, float original, float modified) {
        float intensity = getIntensity(index);
        return modified * intensity + original * (1.f - intensity);
    }

    private void initIntensityPoints() {
        if (intensityPoints == null) {
            intensityPoints = new ArrayList<Point>();
            clearIntensity();
        }
    }

    /**
     * this class represents one point of intensity of this selection
     */
    public class Point {
        public float x, y;

        public Point(Point p) {
            x = p.x;
            y = p.y;
        }

        public Point(float x, float y) {
            setPoint(x, y);
        }

        public void setPoint(float x, float y) {
            if (x > 1)
                this.x = 1;
            else if (x < 0)
                this.x = 0;
            else
                this.x = x;

            if (y > 1)
                this.y = 1;
            else if (y < 0)
                this.y = 0;
            else
                this.y = y;
        }
    }

    /**
     * operate this channel
     */
    public void operateChannel(AOperation o) {
        // TODO consider placement
        o.addEditorListener(GProgressViewer.operationProgressListener);
        o.startOperation();
        if (isSelected()) {
            o.operate(this);
        }
        o.endOperation();
        System.gc();
    }
}
