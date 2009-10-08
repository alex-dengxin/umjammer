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

import ch.oli4.ui.UiControlText;


/*********************************************************************************************************************************
 * 
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with LAoE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * Class: GControlTextX @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * UiControlText with units adapted to x-axis of a clip.
 * 
 * @version 10.04.01 first draft oli4
 * 
 */
public class GControlTextX extends UiControlText {
    public GControlTextX(GMain main, int digits, boolean incrementVisible, boolean unitVisible) {
        super(digits, incrementVisible, unitVisible);
        this.main = main;
        setUnit(" ");
    }

    private GMain main;

    protected void fillUnits() {
        addUnit(" ");
        addUnit("s");
        addUnit("ms");
        addUnit("%");
    }

    protected double dataToUnit(double d) {
        switch (unitIndex) {
        case 0:
            return d;

        case 1:
            return d / main.getFocussedClipEditor().getClip().getSampleRate();

        case 2:
            return d / main.getFocussedClipEditor().getClip().getSampleRate() * 1000;

        case 3:
            return d / main.getFocussedClipEditor().getClip().getMaxSampleLength() * 100;
        }
        return d;
    }

    protected double unitToData(double u) {
        switch (unitIndex) {
        case 0:
            return u;

        case 1:
            return u * main.getFocussedClipEditor().getClip().getSampleRate();

        case 2:
            return u * main.getFocussedClipEditor().getClip().getSampleRate() / 1000;

        case 3:
            return u * main.getFocussedClipEditor().getClip().getMaxSampleLength() / 100;
        }
        return u;
    }
}
