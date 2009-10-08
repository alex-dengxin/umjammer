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

import ch.laoe.clip.AClip;


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
 * Class: GControlTextSF @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * UiControlText with units adapted to x-axis of a spectrum view. spectrum view can be of any size, but begins at
 * 0Hz and ends at 0.5 samplerate Hz.
 * 
 * @version 28.04.01 first draft oli4
 * 
 */
public class GControlTextSF extends GControlTextF {
    public GControlTextSF(GMain main, int digits, boolean incrementVisible, boolean unitVisible) {
        super(main, digits, incrementVisible, unitVisible);
    }

    protected void fillUnits() {
        super.fillUnits();
        addUnit("fdHz");
    }

    protected double dataToUnit(double d) {
        switch (unitIndex) {
        case 5:
            AClip c = main.getFocussedClipEditor().getClip();
            return d / (2 * c.getMaxSampleLength()) * c.getSampleRate();
        }
        return super.dataToUnit(d);
    }

    protected double unitToData(double u) {
        switch (unitIndex) {
        case 5:
            AClip c = main.getFocussedClipEditor().getClip();
            return u * 2 * c.getMaxSampleLength() / c.getSampleRate();
        }
        return super.unitToData(u);
    }
}
